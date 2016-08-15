package smartthings.ratpack.sqs.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;

/**
 * Interface to define AWS configuration mechanisms for the SQS service.
 */
public interface SqsConfigService {
    AWSCredentialsProvider getAwsCredentialsProvider();

    default ClientConfiguration getClientConfiguration() {
        return null;
    }

    default AmazonSQSAsync getAmazonSQSAsync(Regions region) {
        AWSCredentialsProvider provider = getAwsCredentialsProvider();
        ClientConfiguration clientConfig = getClientConfiguration();
        AmazonSQSClient client;
        if (clientConfig == null) {
            client = new AmazonSQSAsyncClient(provider);
        } else {
            client = new AmazonSQSAsyncClient(provider, clientConfig);
        }
        client.configureRegion(region);
        return new AmazonSQSBufferedAsyncClient((AmazonSQSAsync) client);
    }
}
