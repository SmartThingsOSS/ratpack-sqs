package smartthings.ratpack.sns.internal.providers;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import smartthings.ratpack.sns.AmazonSNSProvider;
import smartthings.ratpack.sns.SnsModule;

@Singleton
public class DefaultAmazonSNSProvider implements AmazonSNSProvider {

    private final AWSCredentialsProvider credentialsProvider;

    @Inject
    public DefaultAmazonSNSProvider(
        AWSCredentialsProvider credentialsProvider
    ) {
        this.credentialsProvider = credentialsProvider;
    }

    @Override
    public AmazonSNS get(SnsModule.EndpointConfig config) {
        AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard();
        builder.withCredentials(credentialsProvider);
        if (config.endpoint().isPresent()) {
            builder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(config.getEndpoint(), config.getRegionName())
            );
        } else {
            builder.withRegion(config.getRegionName());
        }
        return builder.build();
    }
}
