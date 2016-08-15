package smartthings.ratpack.sqs.event;

/**
 * Functional interface for event notification.
 */
public interface SqsConsumerEventListener {
    void eventNotification(SqsConsumerEvent event);
}
