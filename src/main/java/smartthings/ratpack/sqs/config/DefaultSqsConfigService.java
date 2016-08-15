package smartthings.ratpack.sqs.config;

import com.amazonaws.auth.*;
import com.google.inject.Inject;
import smartthings.ratpack.sqs.SqsModule;

/**
 * Default AWS Sqs Configuration implementation.
 */
public class DefaultSqsConfigService implements SqsConfigService {

    private final SqsModule.Config config;

    @Inject
    public DefaultSqsConfigService(SqsModule.Config config) {
        this.config = config;
    }

    @Override
    public AWSCredentialsProvider getAwsCredentialsProvider() {
        return new AWSCredentialsProviderChain(
            new BasicAWSCredentialsProvider(config.getAwsAccessKey(), config.getAwsSecretKey()),
            new EnvironmentVariableCredentialsProvider(),
            new InstanceProfileCredentialsProvider()
        );
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
