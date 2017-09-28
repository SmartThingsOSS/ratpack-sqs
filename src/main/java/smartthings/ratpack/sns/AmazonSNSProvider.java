package smartthings.ratpack.sns;

import com.amazonaws.services.sns.AmazonSNS;

public interface AmazonSNSProvider {
    AmazonSNS get(SnsModule.EndpointConfig config);
}
