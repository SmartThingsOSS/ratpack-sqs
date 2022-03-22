package smartthings.ratpack.aws;

import com.google.inject.multibindings.OptionalBinder;
import ratpack.guice.ConfigurableModule;
import smartthings.ratpack.aws.internal.providers.DefaultAWSCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.Optional;

/**
 * A base module for configuring communication with AWS.
 */
public class AwsModule extends ConfigurableModule<AwsModule.Config> {

    @Override
    protected void configure() {
        OptionalBinder.newOptionalBinder(binder(), AwsCredentialsProvider.class)
            .setDefault()
            .toProvider(DefaultAWSCredentialsProvider.class);
    }

    /**
     * AwsModule Config.  All properties are optional.  When not  present module will attempt
     * to resolve via AWS DefaultAWSCredentialsProviderChain.
     */
    public static class Config {
        private String awsSecretKey;
        private String awsAccessKey;
        private String stsRoleArn;
        private String stsRegionName;
        private String stsEndpoint;

        public String getAwsSecretKey() {
            return awsSecretKey;
        }

        public void setAwsSecretKey(String awsSecretKey) {
            this.awsSecretKey = awsSecretKey;
        }

        public String getAwsAccessKey() {
            return awsAccessKey;
        }

        public void setAwsAccessKey(String awsAccessKey) {
            this.awsAccessKey = awsAccessKey;
        }

        public String getStsRoleArn() {
            return stsRoleArn;
        }

        public void setStsRoleArn(String stsRoleArn) {
            this.stsRoleArn = stsRoleArn;
        }

        public String getStsRegionName() {
            return stsRegionName;
        }

        public Optional<String> stsRegionName() {
            return Optional.ofNullable(stsRegionName);
        }

        public void setStsRegionName(String stsRegionName) {
            this.stsRegionName = stsRegionName;
        }

        public String getStsEndpoint() {
            return stsEndpoint;
        }

        public Optional<String> stsEndpoint() {
            return Optional.ofNullable(stsEndpoint);
        }

        public void setStsEndpoint(String stsEndpoint) {
            this.stsEndpoint = stsEndpoint;
        }
    }
}
