package smartthings.ratpack.aws.internal.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import smartthings.ratpack.aws.AwsModule;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DefaultAWSCredentialsProvider implements Provider<AwsCredentialsProvider> {

    private final AwsModule.Config config;

    @Inject
    public DefaultAWSCredentialsProvider(AwsModule.Config config) {
        this.config = config;
    }

    @Override
    public AwsCredentialsProvider get() {

        List<AwsCredentialsProvider> providers = new ArrayList<>();
        if (!isNullOrEmpty(config.getAwsAccessKey()) && !isNullOrEmpty(config.getAwsSecretKey())) {
            providers.add(new BasicAWSCredentialsProvider(config.getAwsAccessKey(), config.getAwsSecretKey()));
        }
        providers.add(DefaultCredentialsProvider.create());

        AwsCredentialsProviderChain providerChain = AwsCredentialsProviderChain.of(
            providers.toArray(new AwsCredentialsProvider[0])
        );

        if (!isNullOrEmpty(config.getStsRoleArn())) {
            final StsClient sts = getStsClient(providerChain);

            AssumeRoleRequest request = AssumeRoleRequest.builder()
                .roleArn(config.getStsRoleArn())
                .roleSessionName("ratpack-sqs")
                .build();

            return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(sts)
                .refreshRequest(request)
                .build();
        }

        return providerChain;
    }

    private StsClient getStsClient(AwsCredentialsProvider credentialsProvider) {
         StsClientBuilder builder = StsClient.builder()
             .credentialsProvider(credentialsProvider);

        if (config.stsEndpoint().isPresent()) {
            builder.endpointOverride(URI.create(config.getStsEndpoint()));
        }
        if (config.stsRegionName().isPresent()) {
            builder.region(Region.of(config.getStsRegionName()));
        }

        return builder.build();
    }

    private static boolean isNullOrEmpty(String value) {
        return (value == null || value.equals(""));
    }

    private static class BasicAWSCredentialsProvider implements AwsCredentialsProvider {

        private final String accessKey;
        private final String secretKey;

        BasicAWSCredentialsProvider(String accessKey, String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }

        @Override
        public AwsCredentials resolveCredentials() {
            return AwsBasicCredentials.create(accessKey, secretKey);
        }
    }
}
