package smartthings.ratpack.sns.internal;

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
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Singleton
public class DefaultSnsService implements SnsService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSnsService.class);

    private final SnsModule.Config config;
    private final List<SnsClient> clients;
    private final AtomicReference<SnsClient> activeClient = new AtomicReference<>();
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
    public Promise<CreateTopicResponse> createTopic(CreateTopicRequest request) {
        LOG.trace("creating sns topic request={}", request);
        return Blocking.get(() -> sns().createTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SubscribeResponse> subscribe(SubscribeRequest request) {
        LOG.trace("subscribing to sns topic request={}", request);
        return Blocking.get(() -> sns().subscribe(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<PublishResponse> publish(PublishRequest request) {
        LOG.trace("publishing to sns topic request={}", request);
        return Blocking.get(() -> sns().publish(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<DeleteTopicResponse> deleteTopic(DeleteTopicRequest request) {
        LOG.debug("deleting sns topic request={}", request);
        return Blocking.get(() -> sns().deleteTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<AddPermissionResponse> addPermission(AddPermissionRequest request) {
        return Blocking.get(() -> sns().addPermission(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<AddPermissionResponse> addPermission(
        String topicArn,
        String label,
        List<String> awsAccountIds,
        List<String> actionNames
    ) {
        AddPermissionRequest request = AddPermissionRequest.builder()
            .topicArn(topicArn)
            .label(label)
            .awsAccountIds(awsAccountIds)
            .actionNames(actionNames)
            .build();
        return Blocking.get(() -> sns().addPermission(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CheckIfPhoneNumberIsOptedOutResponse> checkIfPhoneNumberIsOptedOut(
        CheckIfPhoneNumberIsOptedOutRequest request
    ) {
        return Blocking.get(() -> sns().checkIfPhoneNumberIsOptedOut(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ConfirmSubscriptionResponse> confirmSubscription(ConfirmSubscriptionRequest request) {
        return Blocking.get(() -> sns().confirmSubscription(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ConfirmSubscriptionResponse> confirmSubscription(
        String topicArn,
        String token,
        String authenticateOnUnsubscribe
    ) {
        ConfirmSubscriptionRequest request = ConfirmSubscriptionRequest.builder()
            .topicArn(topicArn)
            .token(token)
            .authenticateOnUnsubscribe(authenticateOnUnsubscribe)
            .build();
        return Blocking.get(() -> sns().confirmSubscription(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ConfirmSubscriptionResponse> confirmSubscription(String topicArn, String token) {
        ConfirmSubscriptionRequest request = ConfirmSubscriptionRequest.builder()
            .topicArn(topicArn)
            .token(token)
            .build();
        return Blocking.get(() -> sns().confirmSubscription(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CreatePlatformApplicationResponse> createPlatformApplication(
        CreatePlatformApplicationRequest request
    ) {
        return Blocking.get(() -> sns().createPlatformApplication(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CreatePlatformEndpointResponse> createPlatformEndpoint(CreatePlatformEndpointRequest request) {
        return Blocking.get(() -> sns().createPlatformEndpoint(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<CreateTopicResponse> createTopic(String name) {
        CreateTopicRequest request = CreateTopicRequest.builder()
            .name(name)
            .build();
        return Blocking.get(() -> sns().createTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<DeleteEndpointResponse> deleteEndpoint(DeleteEndpointRequest request) {
        return Blocking.get(() -> sns().deleteEndpoint(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<DeletePlatformApplicationResponse> deletePlatformApplication(
        DeletePlatformApplicationRequest request
    ) {
        return Blocking.get(() -> sns().deletePlatformApplication(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<DeleteTopicResponse> deleteTopic(String topicArn) {
        DeleteTopicRequest request = DeleteTopicRequest.builder()
            .topicArn(topicArn)
            .build();
        return Blocking.get(() -> sns().deleteTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetEndpointAttributesResponse> getEndpointAttributes(GetEndpointAttributesRequest request) {
        return Blocking.get(() -> sns().getEndpointAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetPlatformApplicationAttributesResponse> getPlatformApplicationAttributes(
        GetPlatformApplicationAttributesRequest request
    ) {
        return Blocking.get(() -> sns().getPlatformApplicationAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetSmsAttributesResponse> getSMSAttributes(GetSmsAttributesRequest request) {
        return Blocking.get(() -> sns().getSMSAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetSubscriptionAttributesResponse> getSubscriptionAttributes(
        GetSubscriptionAttributesRequest request
    ) {
        return Blocking.get(() -> sns().getSubscriptionAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetSubscriptionAttributesResponse> getSubscriptionAttributes(String subscriptionArn) {
        GetSubscriptionAttributesRequest request = GetSubscriptionAttributesRequest.builder()
            .subscriptionArn(subscriptionArn)
            .build();
        return Blocking.get(() -> sns().getSubscriptionAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetTopicAttributesResponse> getTopicAttributes(GetTopicAttributesRequest request) {
        return Blocking.get(() -> sns().getTopicAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<GetTopicAttributesResponse> getTopicAttributes(String topicArn) {
        GetTopicAttributesRequest request = GetTopicAttributesRequest.builder()
            .topicArn(topicArn)
            .build();
        return Blocking.get(() -> sns().getTopicAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListEndpointsByPlatformApplicationResponse> listEndpointsByPlatformApplication(
        ListEndpointsByPlatformApplicationRequest request
    ) {
        return Blocking.get(() -> sns().listEndpointsByPlatformApplication(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListPhoneNumbersOptedOutResponse> listPhoneNumbersOptedOut(ListPhoneNumbersOptedOutRequest request) {
        return Blocking.get(() -> sns().listPhoneNumbersOptedOut(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListPlatformApplicationsResponse> listPlatformApplications(ListPlatformApplicationsRequest request) {
        return Blocking.get(() -> sns().listPlatformApplications(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListPlatformApplicationsResponse> listPlatformApplications() {
        return Blocking.get(() -> sns().listPlatformApplications())
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsResponse> listSubscriptions(ListSubscriptionsRequest request) {
        return Blocking.get(() -> sns().listSubscriptions(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsResponse> listSubscriptions() {
        return Blocking.get(() -> sns().listSubscriptions())
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsResponse> listSubscriptions(String nextToken) {
        ListSubscriptionsRequest request = ListSubscriptionsRequest.builder()
            .nextToken(nextToken)
            .build();
        return Blocking.get(() -> sns().listSubscriptions(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsByTopicResponse> listSubscriptionsByTopic(ListSubscriptionsByTopicRequest request) {
        return Blocking.get(() -> sns().listSubscriptionsByTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsByTopicResponse> listSubscriptionsByTopic(String topicArn) {
        ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest.builder()
            .topicArn(topicArn)
            .build();
        return Blocking.get(() -> sns().listSubscriptionsByTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListSubscriptionsByTopicResponse> listSubscriptionsByTopic(String topicArn, String nextToken) {
        ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest.builder()
            .topicArn(topicArn)
            .nextToken(nextToken)
            .build();
        return Blocking.get(() -> sns().listSubscriptionsByTopic(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListTopicsResponse> listTopics(ListTopicsRequest request) {
        return Blocking.get(() -> sns().listTopics(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListTopicsResponse> listTopics() {
        return Blocking.get(() -> sns().listTopics())
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<ListTopicsResponse> listTopics(String nextToken) {
        ListTopicsRequest request = ListTopicsRequest.builder()
            .nextToken(nextToken)
            .build();
        return Blocking.get(() -> sns().listTopics(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<OptInPhoneNumberResponse> optInPhoneNumber(OptInPhoneNumberRequest request) {
        return Blocking.get(() -> sns().optInPhoneNumber(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<PublishResponse> publish(String topicArn, String message) {
        PublishRequest request = PublishRequest.builder()
            .topicArn(topicArn)
            .message(message)
            .build();
        return Blocking.get(() -> sns().publish(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<PublishResponse> publish(String topicArn, String message, String subject) {
        PublishRequest request = PublishRequest.builder()
            .topicArn(topicArn)
            .message(message)
            .subject(subject)
            .build();
        return Blocking.get(() -> sns().publish(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<RemovePermissionResponse> removePermission(RemovePermissionRequest request) {
        return Blocking.get(() -> sns().removePermission(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<RemovePermissionResponse> removePermission(String topicArn, String label) {
        RemovePermissionRequest request = RemovePermissionRequest.builder()
            .topicArn(topicArn)
            .label(label)
            .build();
        return Blocking.get(() -> sns().removePermission(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetEndpointAttributesResponse> setEndpointAttributes(SetEndpointAttributesRequest request) {
        return Blocking.get(() -> sns().setEndpointAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetPlatformApplicationAttributesResponse> setPlatformApplicationAttributes(
        SetPlatformApplicationAttributesRequest request
    ) {
        return Blocking.get(() -> sns().setPlatformApplicationAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetSmsAttributesResponse> setSMSAttributes(SetSmsAttributesRequest request) {
        return Blocking.get(() -> sns().setSMSAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetSubscriptionAttributesResponse> setSubscriptionAttributes(
        SetSubscriptionAttributesRequest request
    ) {
        return Blocking.get(() -> sns().setSubscriptionAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetSubscriptionAttributesResponse> setSubscriptionAttributes(
        String subscriptionArn,
        String attributeName,
        String attributeValue
    ) {
        SetSubscriptionAttributesRequest request = SetSubscriptionAttributesRequest.builder()
            .subscriptionArn(subscriptionArn)
            .attributeName(attributeName)
            .attributeValue(attributeValue)
            .build();
        return Blocking.get(() -> sns().setSubscriptionAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetTopicAttributesResponse> setTopicAttributes(SetTopicAttributesRequest request) {
        return Blocking.get(() -> sns().setTopicAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SetTopicAttributesResponse> setTopicAttributes(
        String topicArn,
        String attributeName,
        String attributeValue
    ) {
        SetTopicAttributesRequest request = SetTopicAttributesRequest.builder()
            .topicArn(topicArn)
            .attributeName(attributeName)
            .attributeValue(attributeValue)
            .build();
        return Blocking.get(() -> sns().setTopicAttributes(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<SubscribeResponse> subscribe(String topicArn, String protocol, String endpoint) {
        SubscribeRequest request = SubscribeRequest.builder()
            .topicArn(topicArn)
            .protocol(protocol)
            .endpoint(endpoint)
            .build();
        return Blocking.get(() -> sns().subscribe(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<UnsubscribeResponse> unsubscribe(UnsubscribeRequest request) {
        return Blocking.get(() -> sns().unsubscribe(request))
            .transform(breaker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<UnsubscribeResponse> unsubscribe(String subscriptionArn) {
        UnsubscribeRequest request = UnsubscribeRequest.builder()
            .subscriptionArn(subscriptionArn)
            .build();
        return Blocking.get(() -> sns().unsubscribe(request))
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
        this.clients.forEach(SnsClient::close);
    }

    SnsClient sns() {
        if (!config.isEnabled()) {
            throw new IllegalStateException("Unable to execute SNS API when module is disabled.");
        }
        return activeClient.get();
    }

    boolean isAwsServiceError(Throwable t) {
        if (t instanceof AwsServiceException) {
            int status = ((AwsServiceException) t).statusCode();
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
