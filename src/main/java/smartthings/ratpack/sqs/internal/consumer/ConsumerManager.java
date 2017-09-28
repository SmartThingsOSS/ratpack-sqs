package smartthings.ratpack.sqs.internal.consumer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import ratpack.exec.Execution;
import ratpack.service.DependsOn;
import ratpack.service.Service;
import ratpack.service.StartEvent;
import ratpack.service.StopEvent;
import smartthings.ratpack.sqs.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service responsible for managing the lifecycle of all SQS Consumer implementations defined within Registry.
 */
@Singleton
@DependsOn({SqsManager.class})
public class ConsumerManager implements Service {

    private final SqsModule.Config config;
    private final SqsManager sqsManager;
    private List<ConsumerAction> actions = new ArrayList<>();
    private List<CircuitBreaker> breakers = new ArrayList<>();

    @Inject
    public ConsumerManager(SqsModule.Config config, SqsManager sqsManager) {
        this.config = config;
        this.sqsManager = sqsManager;
    }

    @Override
    public void onStart(StartEvent event) throws Exception {
        if (config.isEnabled()) {
            init(event);
        }
    }

    @Override
    public void onStop(StopEvent event) throws Exception {
        actions.forEach(ConsumerAction::shutdown);
    }

    public void pause() {
        this.breakers.forEach(CircuitBreaker::transitionToOpenState);
    }

    public void resume() {
        this.breakers.forEach(CircuitBreaker::transitionToClosedState);
    }

    private void init(StartEvent event) {
        this.actions = config.getConsumers().stream()
            .flatMap(c -> buildConsumerActions(c, event))
            .collect(Collectors.toList());

        // Kick off a new execution for each defined consumer.
        if (!this.actions.isEmpty()) {
            this.actions.forEach((action) -> Execution.fork().start(action));
        }
    }

    private Stream<ConsumerAction> buildConsumerActions(SqsModule.ConsumerConfig config, StartEvent event) {
        Consumer consumer = event.getRegistry().get(config.getConsumer());
        return IntStream
            .rangeClosed(1, config.getConcurrency())
            .mapToObj(i -> config.getEndpoints())
            .flatMap(endpoints ->
                config.getEndpoints().stream()
                    .map(endpointConfig -> {
                        CircuitBreaker breaker = CircuitBreaker.ofDefaults(
                            String.format("sqs-%s", endpointConfig.getQueueName())
                        );
                        breakers.add(breaker);
                        return new ConsumerAction(
                            sqsManager.get(endpointConfig),
                            consumer,
                            breaker,
                            endpointConfig
                        );
                    })
            );
    }
}
