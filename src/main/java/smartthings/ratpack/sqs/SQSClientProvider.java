package smartthings.ratpack.sqs;

import software.amazon.awssdk.services.sqs.SqsClient;

public interface SQSClientProvider {
    SqsClient get(SqsModule.EndpointConfig config);
}
