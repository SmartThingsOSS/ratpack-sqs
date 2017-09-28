package smartthings.ratpack.sqs;

import com.amazonaws.services.sqs.model.*;
import ratpack.exec.Promise;
import ratpack.service.Service;

/**
 * Supported AWS SQS operations.
 */
public interface SqsService {

    Promise<DeleteMessageResult> deleteMessage(DeleteMessageRequest request);

    Promise<SendMessageResult> sendMessage(SendMessageRequest request);

    Promise<ReceiveMessageResult> receiveMessage(ReceiveMessageRequest request);

    Promise<GetQueueUrlResult> getQueueUrl(String queueName);
}
