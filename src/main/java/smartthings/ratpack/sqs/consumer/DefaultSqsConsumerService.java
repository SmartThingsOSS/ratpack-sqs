package smartthings.ratpack.sqs.consumer;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ratpack.exec.Execution;
import ratpack.service.DependsOn;
import ratpack.service.Service;
import ratpack.service.StartEvent;
import ratpack.service.StopEvent;
import smartthings.ratpack.sqs.SqsModule;
import smartthings.ratpack.sqs.SqsService;
import smartthings.ratpack.sqs.circuitbreaker.CircuitBreaker;
import smartthings.ratpack.sqs.circuitbreaker.CircuitBreakerListener;
import smartthings.ratpack.sqs.event.SqsConsumerEvent;
import smartthings.ratpack.sqs.event.SqsConsumerEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Service responsible for managing the lifecycle of all SQS Consumer implementations defined within Registry.
 */
@DependsOn(SqsService.class)
public class DefaultSqsConsumerService implements SqsConsumerService {

    private final SqsModule.Config config;
    private final SqsService sqs;
    private CircuitBreaker circuitBreaker;
    private List<ConsumerAction> actions = new ArrayList<>();
    private List<SqsConsumerEventListener> listeners = new ArrayList<>();

    @Inject
    public DefaultSqsConsumerService(SqsModule.Config config, SqsService sqs) {
        this.config = config;
        this.sqs = sqs;
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
        notifyEventListeners(SqsConsumerEvent.STOPPED_EVENT);
    }

    /**
     * Register an event listener.  Alternatively, override event handler on Consumer.
     * @param listener
     */
    public void registerEventListener(SqsConsumerEventListener listener) {
        listeners.add(listener);
    }

    private void notifyEventListeners(SqsConsumerEvent event) {
        listeners.forEach((listener) -> listener.eventNotification(event));
    }

    private void init(StartEvent event) {
        Iterator<? extends Consumer> it = event.getRegistry().getAll(TypeToken.of(Consumer.class)).iterator();
        circuitBreaker = event.getRegistry().get(CircuitBreaker.class);
        circuitBreaker.init(new CircuitBreakerListener() {
            @Override
            public void opened() {
                notifyEventListeners(SqsConsumerEvent.SUSPENDED_EVENT);
            }

            @Override
            public void closed() {
                notifyEventListeners(SqsConsumerEvent.RESUMED_EVENT);
            }
        });

        // Populate our actions in accordance with the desired concurrency level.
        if (it != null && it.hasNext()){
            it.forEachRemaining(consumer -> {
                int concurrency = consumer.getConcurrencyLevel();
                registerEventListener((e) -> consumer.eventHandler(e));

                IntStream
                        .rangeClosed(1, concurrency)
                        .forEach((action) -> actions.add(new ConsumerAction(sqs, consumer, circuitBreaker)));
            });
        }

        // Kick off a new execution for each defined consumer.
        if (!actions.isEmpty()) {
            actions.forEach((action) -> Execution.fork().start(action));
        }

        notifyEventListeners(SqsConsumerEvent.STARTED_EVENT);
    }
}
