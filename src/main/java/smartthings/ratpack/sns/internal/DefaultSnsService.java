package smartthings.ratpack.sns.internal;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.github.resilience4j.ratpack.circuitbreaker.CircuitBreakerTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.service.StopEvent;
import smartthings.ratpack.sns.AmazonSNSProvider;
import smartthings.ratpack.sns.SnsModule;
import smartthings.ratpack.sns.SnsService;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Singleton
public class DefaultSnsService implements SnsService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSnsService.class);

    private final SnsModule.Config config;
    private final List<AmazonSNS> clients;
    private final AtomicReference<AmazonSNS> activeClient = new AtomicReference<>();
    private final CircuitBreakerTransformer breaker;
    private final LongAdder pos = new LongAdder();

    @Inject
    public DefaultSnsService(SnsModule.Config config, AmazonSNSProvider provider) {
        this.clients = config.isEnabled() ?
            config.getEndpoints()
                .stream()
                .map(provider::get)
                .collect(Collectors.toList()) : Collections.emptyList();
        this.config = config;
        this.breaker = buildCircuitBreaker();
        if (config.isEnabled()) {
            if (this.clients.isEmpty()) {
                throw new IllegalArgumentException("SNS must have at least 1 endpoint configured when enabled.");
            }
            this.activeClient.set(this.clients.get(0));
        }
    }

    @Override
    public void onStop(StopEvent event) throws Exception {
        LOG.info("Shutting down SnsService...");
        this.shutdown();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CreateTopicResult> createTopic(CreateTopicRequest request) {
        LOG.trace("creating sns topic request={}", request);
        return Blocking.get(() -> sns().createTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SubscribeResult> subscribe(SubscribeRequest request) {
        LOG.trace("subscribing to sns topic request={}", request);
        return Blocking.get(() -> sns().subscribe(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<PublishResult> publish(PublishRequest request) {
        LOG.trace("publishing to sns topic request={}", request);
        return Blocking.get(() -> sns().publish(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<DeleteTopicResult> deleteTopic(DeleteTopicRequest request) {
        LOG.debug("deleting sns topic request={}", request);
        return Blocking.get(() -> sns().deleteTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<AddPermissionResult> addPermission(AddPermissionRequest request) {
        return Blocking.get(() -> sns().addPermission(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<AddPermissionResult> addPermission(
        String topicArn,
        String label,
        List<String> aWSAccountIds,
        List<String> actionNames
    ) {
        return Blocking.get(() -> sns().addPermission(topicArn, label, actionNames, actionNames))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CheckIfPhoneNumberIsOptedOutResult> checkIfPhoneNumberIsOptedOut(
        CheckIfPhoneNumberIsOptedOutRequest request
    ) {
        return Blocking.get(() -> sns().checkIfPhoneNumberIsOptedOut(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ConfirmSubscriptionResult> confirmSubscription(ConfirmSubscriptionRequest request) {
        return Blocking.get(() -> sns().confirmSubscription(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ConfirmSubscriptionResult> confirmSubscription(
        String topicArn,
        String token,
        String authenticateOnUnsubscribe
    ) {
        return Blocking.get(() -> sns().confirmSubscription(topicArn, token, authenticateOnUnsubscribe))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ConfirmSubscriptionResult> confirmSubscription(String topicArn, String token) {
        return Blocking.get(() -> sns().confirmSubscription(topicArn, token))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CreatePlatformApplicationResult> createPlatformApplication(
        CreatePlatformApplicationRequest request
    ) {
        return Blocking.get(() -> sns().createPlatformApplication(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CreatePlatformEndpointResult> createPlatformEndpoint(CreatePlatformEndpointRequest request) {
        return Blocking.get(() -> sns().createPlatformEndpoint(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CreateTopicResult> createTopic(String name) {
        return Blocking.get(() -> sns().createTopic(name))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<DeleteEndpointResult> deleteEndpoint(DeleteEndpointRequest request) {
        return Blocking.get(() -> sns().deleteEndpoint(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<DeletePlatformApplicationResult> deletePlatformApplication(
        DeletePlatformApplicationRequest request
    ) {
        return Blocking.get(() -> sns().deletePlatformApplication(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<DeleteTopicResult> deleteTopic(String topicArn) {
        return Blocking.get(() -> sns().deleteTopic(topicArn))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetEndpointAttributesResult> getEndpointAttributes(GetEndpointAttributesRequest request) {
        return Blocking.get(() -> sns().getEndpointAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetPlatformApplicationAttributesResult> getPlatformApplicationAttributes(
        GetPlatformApplicationAttributesRequest request
    ) {
        return Blocking.get(() -> sns().getPlatformApplicationAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetSMSAttributesResult> getSMSAttributes(GetSMSAttributesRequest request) {
        return Blocking.get(() -> sns().getSMSAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetSubscriptionAttributesResult> getSubscriptionAttributes(
        GetSubscriptionAttributesRequest request
    ) {
        return Blocking.get(() -> sns().getSubscriptionAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetSubscriptionAttributesResult> getSubscriptionAttributes(String subscriptionArn) {
        return Blocking.get(() -> sns().getSubscriptionAttributes(subscriptionArn))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetTopicAttributesResult> getTopicAttributes(GetTopicAttributesRequest request) {
        return Blocking.get(() -> sns().getTopicAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetTopicAttributesResult> getTopicAttributes(String topicArn) {
        return Blocking.get(() -> sns().getTopicAttributes(topicArn))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListEndpointsByPlatformApplicationResult> listEndpointsByPlatformApplication(
        ListEndpointsByPlatformApplicationRequest request
    ) {
        return Blocking.get(() -> sns().listEndpointsByPlatformApplication(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListPhoneNumbersOptedOutResult> listPhoneNumbersOptedOut(ListPhoneNumbersOptedOutRequest request) {
        return Blocking.get(() -> sns().listPhoneNumbersOptedOut(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListPlatformApplicationsResult> listPlatformApplications(ListPlatformApplicationsRequest request) {
        return Blocking.get(() -> sns().listPlatformApplications(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListPlatformApplicationsResult> listPlatformApplications() {
        return Blocking.get(() -> sns().listPlatformApplications())
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsResult> listSubscriptions(ListSubscriptionsRequest request) {
        return Blocking.get(() -> sns().listSubscriptions(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsResult> listSubscriptions() {
        return Blocking.get(() -> sns().listSubscriptions())
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsResult> listSubscriptions(String nextToken) {
        return Blocking.get(() -> sns().listSubscriptions(nextToken))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsByTopicResult> listSubscriptionsByTopic(ListSubscriptionsByTopicRequest request) {
        return Blocking.get(() -> sns().listSubscriptionsByTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsByTopicResult> listSubscriptionsByTopic(String topicArn) {
        return Blocking.get(() -> sns().listSubscriptionsByTopic(topicArn))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsByTopicResult> listSubscriptionsByTopic(String topicArn, String nextToken) {
        return Blocking.get(() -> sns().listSubscriptionsByTopic(topicArn, nextToken))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListTopicsResult> listTopics(ListTopicsRequest request) {
        return Blocking.get(() -> sns().listTopics(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListTopicsResult> listTopics() {
        return Blocking.get(() -> sns().listTopics())
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListTopicsResult> listTopics(String nextToken) {
        return Blocking.get(() -> sns().listTopics(nextToken))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<OptInPhoneNumberResult> optInPhoneNumber(OptInPhoneNumberRequest request) {
        return Blocking.get(() -> sns().optInPhoneNumber(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<PublishResult> publish(String topicArn, String message) {
        return Blocking.get(() -> sns().publish(topicArn, message))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<PublishResult> publish(String topicArn, String message, String subject) {
        return Blocking.get(() -> sns().publish(topicArn, message, subject))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<RemovePermissionResult> removePermission(RemovePermissionRequest request) {
        return Blocking.get(() -> sns().removePermission(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<RemovePermissionResult> removePermission(String topicArn, String label) {
        return Blocking.get(() -> sns().removePermission(topicArn, label))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetEndpointAttributesResult> setEndpointAttributes(SetEndpointAttributesRequest request) {
        return Blocking.get(() -> sns().setEndpointAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetPlatformApplicationAttributesResult> setPlatformApplicationAttributes(
        SetPlatformApplicationAttributesRequest request
    ) {
        return Blocking.get(() -> sns().setPlatformApplicationAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetSMSAttributesResult> setSMSAttributes(SetSMSAttributesRequest request) {
        return Blocking.get(() -> sns().setSMSAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetSubscriptionAttributesResult> setSubscriptionAttributes(
        SetSubscriptionAttributesRequest request
    ) {
        return Blocking.get(() -> sns().setSubscriptionAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetSubscriptionAttributesResult> setSubscriptionAttributes(
        String subscriptionArn,
        String attributeName,
        String attributeValue
    ) {
        return Blocking.get(() -> sns().setSubscriptionAttributes(subscriptionArn, attributeName, attributeValue))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetTopicAttributesResult> setTopicAttributes(SetTopicAttributesRequest request) {
        return Blocking.get(() -> sns().setTopicAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetTopicAttributesResult> setTopicAttributes(
        String topicArn,
        String attributeName,
        String attributeValue
    ) {
        return Blocking.get(() -> sns().setTopicAttributes(topicArn, attributeName, attributeValue))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SubscribeResult> subscribe(String topicArn, String protocol, String endpoint) {
        return Blocking.get(() -> sns().subscribe(topicArn, protocol, endpoint))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<UnsubscribeResult> unsubscribe(UnsubscribeRequest request) {
        return Blocking.get(() -> sns().unsubscribe(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<UnsubscribeResult> unsubscribe(String subscriptionArn) {
        return Blocking.get(() -> sns().unsubscribe(subscriptionArn))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ResponseMetadata> getCachedResponseMetadata(AmazonWebServiceRequest request) {
        return Blocking.get(() -> sns().getCachedResponseMetadata(request))
            .transform(breaker);
    }

    @Override
    public void triggerFailover() {
        int size = this.clients.size();

        if (size <= 1) {
            // No to additional clients to support failover.
            return;
        }

        pos.increment();
        int curr = this.pos.intValue();
        int next = curr >= size ? 0 : curr;

        if (next == 0) {
            pos.reset();
        }

        this.activeClient.set(this.clients.get(next));
    }

    @Override
    public void shutdown() {
        this.clients.forEach(AmazonSNS::shutdown);
    }

    private AmazonSNS sns() {
        if (!config.isEnabled()) {
            throw new IllegalStateException("Unable to execute SNS API when module is disabled.");
        }
        return activeClient.get();
    }

    private boolean isAwsServiceError(Throwable t) {
        if (t instanceof AmazonServiceException) {
            int status = ((AmazonServiceException) t).getStatusCode();
            return status >= 500 && status <= 599;
        }
        return false;
    }

    private void onStateChange(CircuitBreakerOnStateTransitionEvent event) {
        if (event.getStateTransition() == CircuitBreaker.StateTransition.CLOSED_TO_OPEN) {
            triggerFailover();
        }
    }

    private CircuitBreakerTransformer buildCircuitBreaker() {
        CircuitBreaker breaker = CircuitBreaker.of("sns", () ->
            CircuitBreakerConfig.custom()
                .recordFailure(this::isAwsServiceError)
                .build()
        );
        breaker.getEventPublisher().onStateTransition(this::onStateChange);
        return CircuitBreakerTransformer.of(breaker);
    }
}
