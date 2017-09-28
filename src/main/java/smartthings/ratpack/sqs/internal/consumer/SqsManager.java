package smartthings.ratpack.sqs.internal.consumer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.service.Service;
import ratpack.service.StartEvent;
import smartthings.ratpack.sqs.DefaultSqsService;
import smartthings.ratpack.sqs.SqsModule;
import smartthings.ratpack.sqs.SqsService;
import smartthings.ratpack.sqs.AmazonSQSProvider;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SqsManager implements Service {

    private static final Logger LOG = LoggerFactory.getLogger(SqsManager.class);

    private final Map<String, SqsService> sqsMap = new ConcurrentHashMap<>();
    private final SqsModule.Config config;
    private final AmazonSQSProvider sqsProvider;

    @Inject
    public SqsManager(SqsModule.Config config, AmazonSQSProvider sqsProvider) {
        this.config = config;
        this.sqsProvider = sqsProvider;
    }

    @Override
    public void onStart(StartEvent event) throws Exception {
        if (config.isEnabled()) {
            config.getConsumers().stream()
                .filter(SqsModule.ConsumerConfig::isEnabled)
                .map(SqsModule.ConsumerConfig::getEndpoints)
                .flatMap(Collection::stream)
                .forEach(this::create);
        }
    }

    public SqsService get(SqsModule.EndpointConfig config) {
        SqsService sqs = sqsMap.get(getCacheKey(config));
        if (sqs == null) {
            LOG.error(
                "No SQS client exists for region={} endpoint={}",
                config.getRegionName(), config.endpoint().orElse("none")
            );
            throw new IllegalStateException("Unable to resolve SQS client for Endpoint");
        }
        return sqs;
    }

    private SqsService create(SqsModule.EndpointConfig config) {
        if (config.getRegionName() == null) {
            throw new IllegalArgumentException("Consumer endpoint config requires a valid configured AWS Region.");
        }
        String cacheKey = getCacheKey(config);
        if (sqsMap.containsKey(cacheKey)) {
            return sqsMap.get(cacheKey);
        }
        SqsService sqsService = new DefaultSqsService(sqsProvider.get(config));
        sqsMap.put(cacheKey, sqsService);
        return sqsService;
    }

    private String getCacheKey(SqsModule.EndpointConfig config) {
        return config.getRegionName() + ":" + config.endpoint().orElse("none");
    }

}
