package smartthings.ratpack.sqs.internal.consumer;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratpack.circuitbreaker.CircuitBreakerTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.exec.Execution;
import ratpack.exec.Operation;
import ratpack.exec.Promise;
import ratpack.exec.util.SerialBatch;
import ratpack.func.Action;
import smartthings.ratpack.aws.internal.backoff.ExponentialBackoff;
import smartthings.ratpack.sqs.Consumer;
import smartthings.ratpack.sqs.SqsModule;
import smartthings.ratpack.sqs.SqsService;
import smartthings.ratpack.sqs.internal.exception.ShutdownConsumerException;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Action definition for continuous polling of SQS messages.
 */
public class ConsumerAction implements Action<Execution> {

    private static final Logger log = LoggerFactory.getLogger(ConsumerAction.class);

    private final SqsService sqs;
    private final Consumer consumer;
    private final SqsModule.EndpointConfig config;
    private String sqsQueueUrl;
    private final CircuitBreaker breaker;
    private final CircuitBreakerTransformer transformer;
    private final ExponentialBackoff backoff = new ExponentialBackoff();
    private final Object mutex = new Object();
    private AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private AtomicBoolean shutdownComplete = new AtomicBoolean(false);

    public ConsumerAction(
        SqsService sqs,
        Consumer consumer,
        CircuitBreaker breaker,
        SqsModule.EndpointConfig config
    ) {
        this.sqs = sqs;
        this.consumer = consumer;
        this.config = config;
        this.breaker = breaker;
        this.transformer = CircuitBreakerTransformer.of(this.breaker);
        this.breaker.getEventPublisher().onStateTransition(event -> {
            if (!isCircuitOpen()) {
                backoff.reset();
            }
        });
    }

    @Override
    public void execute(Execution execution) throws Exception {
        start()
        .result(r -> {
            Throwable error = r.getThrowable();
            if (error == null) {
                Execution.fork().start(this);
            } else {
                if (error instanceof ShutdownConsumerException) {
                    log.warn("SQS consumer={} is shutting down.", config.getQueueName());
                } else {
                    log.error("Unexpected exception consumer={} terminated.", config.getQueueName(), error);
                }
                notifyShutdown();
            }
        });
    }

    public void shutdown() {
        this.shuttingDown.set(true);
        awaitShutdown()
            .then(() ->
                log.warn("SQS consumer={} shutdown complete.", config.getQueueName())
            );
    }

    private Promise<Void> start() {
        return poll()
            .flatMapError(e -> {
                if (e instanceof ShutdownConsumerException) {
                    throw (ShutdownConsumerException) e;
                }
                log.error("Unexpected exception polling SQS", e);
                return start();
            });
    }

    private Promise<Void> poll() {
        return this.maybeBackoff()
            .flatMap(v -> this.getReceiveMessageRequest())
            .flatMap(this::receiveMessage)
            .flatMap(this::consume)
            .flatMap(v -> this.maybeTriggerShutdown());
    }

    private Promise<String> getQueueUrl() {
        if (sqsQueueUrl != null) {
            return Promise.value(sqsQueueUrl);
        }

        String queueName = config.getQueueName();
        if (queueName == null || queueName.isEmpty()) {
            throw new IllegalArgumentException("An SQS Consumer must define a queue in which to poll.");
        }

        return sqs.getQueueUrl(queueName)
            .map(GetQueueUrlResponse::queueUrl);
    }

    private Promise<Void> consume(ReceiveMessageResponse result) {
        List<Promise<Void>> promises = result.messages().stream()
            .map(this::consume)
            .collect(Collectors.toList());

        return SerialBatch.of(promises)
            .yieldAll()
            .flatMap(v -> Promise.value(null));
    }

    private Promise<Void> consume(Message message) {
        return Operation.of(() -> consumer.consume(message))
            .promise()
            .mapError(e -> {
                log.error("Failed to consume message. message={}, exception={}", message, e.getMessage());
                throw new RuntimeException(e);
            })
            .flatMap(v -> this.deleteMessage(message));
    }

    @SuppressWarnings("unchecked")
    private Promise<ReceiveMessageResponse> receiveMessage(ReceiveMessageRequest request) {
        log.debug("Execute poll for SQS queue={}", config.getQueueName());
        return sqs.receiveMessage(request)
            .transform(transformer.recover(t -> ReceiveMessageResponse.builder().build()));
    }

    @SuppressWarnings("unchecked")
    private Promise<Void> deleteMessage(Message message) {
        return getQueueUrl()
            .map(url -> DeleteMessageRequest.builder()
                .queueUrl(url)
                .receiptHandle(message.receiptHandle())
                .build())
            .flatMap(sqs::deleteMessage)
            .transform(transformer.recover(t -> DeleteMessageResponse.builder().build()));
    }

    private Promise<ReceiveMessageRequest> getReceiveMessageRequest() {
        ReceiveMessageRequest request = consumer.getReceiveMessageRequest();

        if (request.queueUrl() == null || request.queueUrl().isEmpty()) {
            return getQueueUrl()
                .map((url) -> request.toBuilder().queueUrl(url).build());
        }
        return Promise.value(request);
    }

    private Promise<Void> maybeTriggerShutdown() {
        if (this.shuttingDown.get()) {
            return Promise.error(new ShutdownConsumerException());
        }
        return Promise.value(null);
    }

    private Promise<Void> maybeBackoff() {
        if (isCircuitOpen()) {
            return backoff.backoff();
        }
        return Promise.value(null);
    }

    private boolean isCircuitOpen() {
        return CircuitBreaker.State.OPEN == this.breaker.getState();
    }

    private Operation awaitShutdown() {
        return Blocking.op(() -> {
            synchronized (mutex) {
                try {
                    while (!shutdownComplete.get()) {
                        mutex.wait(500);
                    }
                } catch (InterruptedException e) {
                    //Intentionally left blank
                }
            }
        });
    }

    private void notifyShutdown() {
        shutdownComplete.set(true);
        synchronized (mutex) {
            mutex.notifyAll();
        }
    }
}
