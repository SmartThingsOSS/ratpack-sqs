package smartthings.ratpack.sns.internal.providers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import smartthings.ratpack.sns.AmazonSNSProvider;
import smartthings.ratpack.sns.SnsModule;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;

import java.net.URI;

@Singleton
public class DefaultAmazonSNSProvider implements AmazonSNSProvider {

    private final AwsCredentialsProvider credentialsProvider;

    @Inject
    public DefaultAmazonSNSProvider(
        AwsCredentialsProvider credentialsProvider
    ) {
        this.credentialsProvider = credentialsProvider;
    }

    @Override
    public SnsClient get(SnsModule.EndpointConfig config) {
        SnsClientBuilder builder = SnsClient.builder();
        builder.credentialsProvider(credentialsProvider);
        if (config.endpoint().isPresent()) {
            builder.endpointOverride(URI.create(config.getEndpoint()));
        }
        if (config.regionName().isPresent()) {
            builder.region(Region.of(config.getRegionName()));
        }
        return builder.build();
    }
}
