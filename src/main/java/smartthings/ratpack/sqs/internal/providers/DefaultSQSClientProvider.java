package smartthings.ratpack.sqs.internal.providers;

import com.google.inject.Singleton;
import smartthings.ratpack.sqs.SQSClientProvider;
import smartthings.ratpack.sqs.SqsModule;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Singleton
public class DefaultSQSClientProvider implements SQSClientProvider {

    @Override
    public SqsClient get(SqsModule.EndpointConfig config) {
        SqsClientBuilder builder = SqsClient.builder();
        if (config.endpoint().isPresent()) {
            builder.endpointOverride(URI.create(config.getEndpoint()));
        }
        if (config.regionName().isPresent()) {
            builder.region(Region.of(config.getRegionName()));
        }
        return builder.build();
    }
}
