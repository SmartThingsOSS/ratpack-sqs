package smartthings.ratpack.sns;

import com.google.inject.multibindings.OptionalBinder;
import ratpack.guice.ConfigurableModule;
import smartthings.ratpack.sns.internal.DefaultSnsService;
import smartthings.ratpack.sns.internal.providers.DefaultAmazonSNSProvider;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

public class SnsModule extends ConfigurableModule<SnsModule.Config> {

    @Override
    protected void configure() {
        OptionalBinder.newOptionalBinder(binder(), AmazonSNSProvider.class)
            .setDefault()
            .to(DefaultAmazonSNSProvider.class);

        OptionalBinder.newOptionalBinder(binder(), SnsService.class)
            .setDefault()
            .to(DefaultSnsService.class);
    }

    public static class Config {
        private boolean enabled;
        private List<EndpointConfig> endpoints = Collections.emptyList();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<EndpointConfig> getEndpoints() {
            return unmodifiableList(endpoints);
        }

        public void setEndpoints(List<EndpointConfig> endpoints) {
            this.endpoints = endpoints;
        }
    }

    public static class EndpointConfig {
        private String regionName;
        private String endpoint;

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }

        public Optional<String> regionName() {
            return Optional.ofNullable(regionName);
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Optional<String> endpoint() {
            return Optional.ofNullable(endpoint);
        }
    }
}
