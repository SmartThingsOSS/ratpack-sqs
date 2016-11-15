package smartthings.ratpack.sqs;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.model.*;
import com.google.inject.Inject;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.service.StartEvent;
import ratpack.service.StopEvent;
import smartthings.ratpack.sqs.config.SqsConfigService;

import java.util.Optional;

/**
 * Default implementation for communicating with AWS SQS.
 */
public class DefaultSqsService implements SqsService {

    private final SqsModule.Config config;
    private final SqsConfigService sqsConfigService;
    private AmazonSQSAsync sqs;

    @Inject
    public DefaultSqsService(
        SqsModule.Config config,
        SqsConfigService sqsConfigService
    ) {
        this.config = config;
        this.sqsConfigService = sqsConfigService;
    }

    @Override
    public void onStart(StartEvent event) throws Exception {
        if (config.isEnabled()) {
            sqs = sqsConfigService.getAmazonSQSAsync(config.getRegion());
            Optional.ofNullable(config.getEndpoint()).ifPresent(sqs::setEndpoint);
        }
    }

    @Override
    public void onStop(StopEvent event) throws Exception {
        if (sqs != null) {
            sqs.shutdown();
        }
    }

    @Override
    public Promise<DeleteMessageResult> deleteMessage(DeleteMessageRequest request) {
        if (sqs == null) {
            return Promise.error(new IllegalStateException("SQS is not currently enabled."));
        }
        return Blocking.get(() -> sqs.deleteMessageAsync(request).get());
    }

    @Override
    public Promise<SendMessageResult> sendMessage(SendMessageRequest request) {
        if (sqs == null) {
            return Promise.error(new IllegalStateException("SQS is not currently enabled."));
        }
        return Blocking.get(() -> sqs.sendMessageAsync(request).get());
    }

    @Override
    public Promise<ReceiveMessageResult> receiveMessage(ReceiveMessageRequest request) {
        if (sqs == null) {
            return Promise.error(new IllegalStateException("SQS is not currently enabled."));
        }
        return Blocking.get(() -> sqs.receiveMessageAsync(request).get());
    }

    @Override
    public Promise<GetQueueUrlResult> getQueueUrl(String queueName) {
        if (sqs == null) {
            return Promise.error(new IllegalStateException("SQS is not currently enabled."));
        }
        return Blocking.get(() -> sqs.getQueueUrlAsync(queueName).get());
    }

}
