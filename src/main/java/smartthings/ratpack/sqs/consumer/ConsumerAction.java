package smartthings.ratpack.sqs.consumer;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Execution;
import ratpack.exec.Operation;
import ratpack.exec.Promise;
import ratpack.func.Action;
import smartthings.ratpack.sqs.SqsService;
import smartthings.ratpack.sqs.circuitbreaker.CircuitBreaker;
import smartthings.ratpack.sqs.exception.ShutdownConsumerException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Action definition for continuous polling of SQS messages.
 */
public class ConsumerAction implements Action<Execution> {

    private static final Logger log = LoggerFactory.getLogger(ConsumerAction.class);

    private final SqsService sqs;
    private final Consumer consumer;
    private final CircuitBreaker circuitBreaker;
    private String sqsQueueUrl;
    private AtomicBoolean shutdown = new AtomicBoolean(false);


    public ConsumerAction(SqsService sqs, Consumer consumer, CircuitBreaker circuitBreaker) {
        this.sqs = sqs;
        this.consumer = consumer;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void execute(Execution execution) throws Exception {
        poll()
            .result(r -> {
                Throwable error = r.getThrowable();
                if (error instanceof ShutdownConsumerException) {
                    log.warn("SQS consumer is shutting down " + toString());
                } else {
                    log.error("Unexpected exception polling SQS", error);
                }
            });
    }

    public void shutdown() {
        this.shutdown.set(true);
    }

    private Promise<?> poll() {
        return circuitBreaker.blockIfOpen()
            .flatMap(this::getReceiveMessageRequest)
            .flatMap(sqs::receiveMessage)
            .flatMap(this::consume)
            .flatMap((v) -> this.maybeTriggerShutdown())
            .flatMap((v) -> this.poll());
    }

    private Promise<String> getQueueUrl() {
        if (sqsQueueUrl != null) {
            return Promise.value(sqsQueueUrl);
        }

        String queueName = consumer.getQueueName();
        if (queueName == null || queueName.isEmpty()) {
            throw new IllegalArgumentException("An SQS Consumer must define a queue in which to poll.");
        }

        return sqs.getQueueUrl(queueName)
            .map((result) -> {
                sqsQueueUrl = result.getQueueUrl();
                return sqsQueueUrl;
            });
    }

    private Promise<Void> consume(ReceiveMessageResult result) {
        return Operation.of(() -> {
            result.getMessages().forEach(this::consume);
        })
        .promise();
    }

    private void consume(Message message) {
        Operation.of(() -> consumer.consume(message))
            .promise()
            .result((r) -> {
                if (r.isError()) {
                    log.error("Unable to process message [message: " + message.toString() + "]", r.getThrowable());
                    // TODO: Add Retry Strategy.
                }
                deleteMessage(message);
            });

    }

    private void deleteMessage(Message message) {
        getQueueUrl()
            .map(url -> new DeleteMessageRequest(url, message.getReceiptHandle()))
            .flatMap(sqs::deleteMessage)
            .result(r -> {
                if (r.isError()) {
                    log.error("Unexpected error deleting SQS message.", r.getThrowable());
                    throw new RuntimeException("Unexpected error deleting SQS message.", r.getThrowable());
                }
                log.debug("Successfully deleted message [message = " + message.toString() + "]");
            });
    }

    private Promise<ReceiveMessageRequest> getReceiveMessageRequest() {
        ReceiveMessageRequest request = consumer.getReceiveMessageRequest();
        if (request.getQueueUrl() == null || request.getQueueUrl().isEmpty()) {
            return getQueueUrl()
                .map((url) -> {
                   request.withQueueUrl(url);
                   return request;
                });
        }
        return Promise.value(request);
    }

    private Promise<Void> maybeTriggerShutdown() {
        if (this.shutdown.get()) {
            return Promise.error(new ShutdownConsumerException());
        }
        return Promise.value(null);
    }
}
