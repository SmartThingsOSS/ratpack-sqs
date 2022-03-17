package smartthings.ratpack.sqs;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.service.Service;
import ratpack.service.StopEvent;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

/**
 * Default implementation for communicating with AWS SQS.
 */
@Singleton
public class DefaultSqsService implements SqsService, Service {

    private final SqsClient sqs;

    @Inject
    public DefaultSqsService(SqsClient sqs) {
        this.sqs = sqs;
    }

    @Override
    public void onStop(StopEvent event) throws Exception {
        sqs.close();
    }

    @Override
    public Promise<DeleteMessageResponse> deleteMessage(DeleteMessageRequest request) {
        return Blocking.get(() -> sqs.deleteMessage(request));
    }

    @Override
    public Promise<SendMessageResponse> sendMessage(SendMessageRequest request) {
        return Blocking.get(() -> sqs.sendMessage(request));
    }

    @Override
    public Promise<ReceiveMessageResponse> receiveMessage(ReceiveMessageRequest request) {
        return Blocking.get(() -> sqs.receiveMessage(request));
    }

    @Override
    public Promise<GetQueueUrlResponse> getQueueUrl(String queueName) {
        GetQueueUrlRequest request = GetQueueUrlRequest.builder()
            .queueName(queueName)
            .build();
        return Blocking.get(() -> sqs.getQueueUrl(request));
    }
}
