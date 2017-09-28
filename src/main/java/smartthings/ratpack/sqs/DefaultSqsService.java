package smartthings.ratpack.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.service.Service;
import ratpack.service.StopEvent;

/**
 * Default implementation for communicating with AWS SQS.
 */
@Singleton
public class DefaultSqsService implements SqsService, Service {

    private final AmazonSQS sqs;

    @Inject
    public DefaultSqsService(AmazonSQS sqs) {
        this.sqs = sqs;
    }

    @Override
    public void onStop(StopEvent event) throws Exception {
        sqs.shutdown();
    }

    @Override
    public Promise<DeleteMessageResult> deleteMessage(DeleteMessageRequest request) {
        return Blocking.get(() -> sqs.deleteMessage(request));
    }

    @Override
    public Promise<SendMessageResult> sendMessage(SendMessageRequest request) {
        return Blocking.get(() -> sqs.sendMessage(request));
    }

    @Override
    public Promise<ReceiveMessageResult> receiveMessage(ReceiveMessageRequest request) {
        return Blocking.get(() -> sqs.receiveMessage(request));
    }

    @Override
    public Promise<GetQueueUrlResult> getQueueUrl(String queueName) {
        return Blocking.get(() -> sqs.getQueueUrl(queueName));
    }
}
