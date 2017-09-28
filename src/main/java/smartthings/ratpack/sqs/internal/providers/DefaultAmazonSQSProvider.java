package smartthings.ratpack.sqs.internal.providers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import smartthings.ratpack.sqs.AmazonSQSProvider;
import smartthings.ratpack.sqs.SqsModule;

@Singleton
public class DefaultAmazonSQSProvider implements AmazonSQSProvider {

    private final AWSCredentialsProvider credentialsProvider;

    @Inject
    public DefaultAmazonSQSProvider(AWSCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    @Override
    public AmazonSQS get(SqsModule.EndpointConfig config) {
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard();
        builder.withCredentials(credentialsProvider);
        if (config.endpoint().isPresent()) {
            builder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(config.getEndpoint(), config.getRegionName())
            );
        } else {
            builder.withRegion(Regions.fromName(config.getRegionName()));
        }
        return builder.build();
    }
}
