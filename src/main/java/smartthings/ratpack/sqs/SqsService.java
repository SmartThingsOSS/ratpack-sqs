package smartthings.ratpack.sqs;

import ratpack.exec.Promise;
import software.amazon.awssdk.services.sqs.model.*;

/**
 * Supported AWS SQS operations.
 */
public interface SqsService {

    Promise<DeleteMessageResponse> deleteMessage(DeleteMessageRequest request);

    Promise<SendMessageResponse> sendMessage(SendMessageRequest request);

    Promise<ReceiveMessageResponse> receiveMessage(ReceiveMessageRequest request);

    Promise<GetQueueUrlResponse> getQueueUrl(String queueName);
}
