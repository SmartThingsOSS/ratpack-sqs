package smartthings.ratpack.sns;

import software.amazon.awssdk.services.sns.SnsClient;

public interface AmazonSNSProvider {
    SnsClient get(SnsModule.EndpointConfig config);
}
