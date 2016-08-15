package smartthings.ratpack.sqs;

import com.amazonaws.regions.Regions;
import com.google.inject.Scopes;
import ratpack.guice.ConfigurableModule;
import smartthings.ratpack.sqs.circuitbreaker.CircuitBreaker;
import smartthings.ratpack.sqs.circuitbreaker.SimpleCircuitBreaker;
import smartthings.ratpack.sqs.config.DefaultSqsConfigService;
import smartthings.ratpack.sqs.config.SqsConfigService;
import smartthings.ratpack.sqs.consumer.DefaultSqsConsumerService;
import smartthings.ratpack.sqs.consumer.SqsConsumerService;

/**
 * Guice module bindings for the RatPack SQS library.
 */
public class SqsModule extends ConfigurableModule<SqsModule.Config> {

    @Override
    protected void configure() {
        bind(CircuitBreaker.class).toProvider(SimpleCircuitBreaker::new);
        bind(SqsService.class).to(DefaultSqsService.class).in(Scopes.SINGLETON);
        bind(SqsConsumerService.class).to(DefaultSqsConsumerService.class).in(Scopes.SINGLETON);
        bind(SqsConfigService.class).to(DefaultSqsConfigService.class).in(Scopes.SINGLETON);
    }

    /**
     * Primary RatPack SQS module configuration.
     */
    public static class Config {
        private boolean enabled;
        private Regions region;
        private String awsSecretKey;
        private String awsAccessKey;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Regions getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = Regions.fromName(region);
        }

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
    }
}
