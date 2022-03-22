package smartthings.ratpack.sqs;

import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/**
 * Interface for creating an SQS Consumer.  Implementers will have their consume method called as messages become
 * available.
 */
public interface Consumer {

    /**
     * Invoked upon message receipt from SQS.
     * @param message The SQS message to consume
     * @throws Exception When something goes wrong
     */
    void consume(Message message) throws Exception;

    /**
     * Override to provide defaults to the sqs message request.
     * @return a ReceiveMessageRequest with a default wait time
     */
    default ReceiveMessageRequest getReceiveMessageRequest() {
        return ReceiveMessageRequest.builder()
            .waitTimeSeconds(20)
            .build();
    }
}
