package smartthings.ratpack.sqs.consumer;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import smartthings.ratpack.sqs.event.SqsConsumerEvent;


/**
 * Interface for creating an SQS Consumer.  Implementers will have their consume method called as messages become
 * available.
 */
public interface Consumer {

    /**
     * Invoked upon message receipt from SQS.
     * @param message
     * @throws Exception
     */
    void consume(Message message) throws Exception;

    /**
     * SQS queue name to listen on.
     * @return
     */
    String getQueueName();

    /**
     * Override to provide defaults to the sqs message request.
     * @return
     */
    default ReceiveMessageRequest getReceiveMessageRequest() {
        return new ReceiveMessageRequest();
    }

    /**
     * Determines how many consumers will be spun up on application start.
     * @return
     */
    default Integer getConcurrencyLevel() {
        return 1;
    }

    /**
     * Invoked on changes to SQS polling/consumption.
     * @param event
     */
    default void eventHandler(SqsConsumerEvent event) {}

}
