package smartthings.ratpack.aws.internal.providers;

import com.amazonaws.auth.*;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import smartthings.ratpack.aws.AwsModule;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DefaultAWSCredentialsProvider implements Provider<AWSCredentialsProvider> {

    private final AwsModule.Config config;

    @Inject
    public DefaultAWSCredentialsProvider(AwsModule.Config config) {
        this.config = config;
    }

    @Override
    public AWSCredentialsProvider get() {

        List<AWSCredentialsProvider> providers = new ArrayList<>();
        if (!isNullOrEmpty(config.getAwsAccessKey()) && !isNullOrEmpty(config.getAwsSecretKey())) {
            providers.add(new BasicAWSCredentialsProvider(config.getAwsAccessKey(), config.getAwsSecretKey()));
        }
        providers.add(new DefaultAWSCredentialsProviderChain());

        if (!isNullOrEmpty(config.getStsRoleArn())) {
            final AWSSecurityTokenService sts = securityTokenService(new AWSCredentialsProviderChain(providers));

            return new STSAssumeRoleSessionCredentialsProvider.Builder(config.getStsRoleArn(), "ratpack-sqs")
                .withStsClient(sts)
                .build();
        }

        return new AWSCredentialsProviderChain(
            providers.toArray(new AWSCredentialsProvider[providers.size()])
        );
    }

    private AWSSecurityTokenService securityTokenService(AWSCredentialsProvider credentialsProvider) {
        AWSSecurityTokenServiceClientBuilder builder = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(credentialsProvider);

        if (config.stsEndpoint().isPresent()) {
            builder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(config.getStsEndpoint(), config.getStsRegionName())
            );
        } else {
            builder.withRegion(config.getStsRegionName());
        }

        return builder.build();
    }

    private static boolean isNullOrEmpty(String value) {
        return (value == null || value.equals(""));
    }

    private static class BasicAWSCredentialsProvider implements AWSCredentialsProvider {

        private String accessKey;
        private String secretKey;

        BasicAWSCredentialsProvider(String accessKey, String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }

        @Override
        public AWSCredentials getCredentials() {
            return new BasicAWSCredentials(accessKey, secretKey);
        }

        @Override
        public void refresh() {

        }
    }
}
