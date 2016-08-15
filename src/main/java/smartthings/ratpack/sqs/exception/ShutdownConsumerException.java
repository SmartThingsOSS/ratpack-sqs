package smartthings.ratpack.sqs.exception;

/**
 * Error raised to trigger a shutdown of SQS consumer polling.
 */
public class ShutdownConsumerException extends RuntimeException {

    public ShutdownConsumerException() {
        super("SQS consumer shutdown has been triggered.");
    }
}
