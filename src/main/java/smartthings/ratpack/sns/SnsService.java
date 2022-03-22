package smartthings.ratpack.sns;

import ratpack.exec.Promise;
import ratpack.service.Service;
import software.amazon.awssdk.services.sns.model.*;

@SuppressWarnings("checkstyle:linelength")
public interface SnsService extends Service {

    Promise<AddPermissionResponse> addPermission(AddPermissionRequest addPermissionRequest);

    Promise<AddPermissionResponse> addPermission(String topicArn, String label, java.util.List<String> aWSAccountIds, java.util.List<String> actionNames);

    Promise<CheckIfPhoneNumberIsOptedOutResponse> checkIfPhoneNumberIsOptedOut(CheckIfPhoneNumberIsOptedOutRequest checkIfPhoneNumberIsOptedOutRequest);

    Promise<ConfirmSubscriptionResponse> confirmSubscription(ConfirmSubscriptionRequest confirmSubscriptionRequest);

    Promise<ConfirmSubscriptionResponse> confirmSubscription(String topicArn, String token, String authenticateOnUnsubscribe);

    Promise<ConfirmSubscriptionResponse> confirmSubscription(String topicArn, String token);

    Promise<CreatePlatformApplicationResponse> createPlatformApplication(CreatePlatformApplicationRequest createPlatformApplicationRequest);

    Promise<CreatePlatformEndpointResponse> createPlatformEndpoint(CreatePlatformEndpointRequest createPlatformEndpointRequest);

    Promise<CreateTopicResponse> createTopic(CreateTopicRequest createTopicRequest);

    Promise<CreateTopicResponse> createTopic(String name);

    Promise<DeleteEndpointResponse> deleteEndpoint(DeleteEndpointRequest deleteEndpointRequest);

    Promise<DeletePlatformApplicationResponse> deletePlatformApplication(DeletePlatformApplicationRequest deletePlatformApplicationRequest);

    Promise<DeleteTopicResponse> deleteTopic(DeleteTopicRequest deleteTopicRequest);

    Promise<DeleteTopicResponse> deleteTopic(String topicArn);

    Promise<GetEndpointAttributesResponse> getEndpointAttributes(GetEndpointAttributesRequest getEndpointAttributesRequest);

    Promise<GetPlatformApplicationAttributesResponse> getPlatformApplicationAttributes(GetPlatformApplicationAttributesRequest getPlatformApplicationAttributesRequest);

    Promise<GetSmsAttributesResponse> getSMSAttributes(GetSmsAttributesRequest getSMSAttributesRequest);

    Promise<GetSubscriptionAttributesResponse> getSubscriptionAttributes(GetSubscriptionAttributesRequest getSubscriptionAttributesRequest);

    Promise<GetSubscriptionAttributesResponse> getSubscriptionAttributes(String subscriptionArn);

    Promise<GetTopicAttributesResponse> getTopicAttributes(GetTopicAttributesRequest getTopicAttributesRequest);

    Promise<GetTopicAttributesResponse> getTopicAttributes(String topicArn);

    Promise<ListEndpointsByPlatformApplicationResponse> listEndpointsByPlatformApplication(
        ListEndpointsByPlatformApplicationRequest listEndpointsByPlatformApplicationRequest
    );

    Promise<ListPhoneNumbersOptedOutResponse> listPhoneNumbersOptedOut(ListPhoneNumbersOptedOutRequest listPhoneNumbersOptedOutRequest);

    Promise<ListPlatformApplicationsResponse> listPlatformApplications(ListPlatformApplicationsRequest listPlatformApplicationsRequest);

    Promise<ListPlatformApplicationsResponse> listPlatformApplications();

    Promise<ListSubscriptionsResponse> listSubscriptions(ListSubscriptionsRequest listSubscriptionsRequest);

    Promise<ListSubscriptionsResponse> listSubscriptions();

    Promise<ListSubscriptionsResponse> listSubscriptions(String nextToken);

    Promise<ListSubscriptionsByTopicResponse> listSubscriptionsByTopic(ListSubscriptionsByTopicRequest listSubscriptionsByTopicRequest);

    Promise<ListSubscriptionsByTopicResponse> listSubscriptionsByTopic(String topicArn);

    Promise<ListSubscriptionsByTopicResponse> listSubscriptionsByTopic(String topicArn, String nextToken);

    Promise<ListTopicsResponse> listTopics(ListTopicsRequest listTopicsRequest);

    Promise<ListTopicsResponse> listTopics();

    Promise<ListTopicsResponse> listTopics(String nextToken);

    Promise<OptInPhoneNumberResponse> optInPhoneNumber(OptInPhoneNumberRequest optInPhoneNumberRequest);

    Promise<PublishResponse> publish(PublishRequest publishRequest);

    Promise<PublishResponse> publish(String topicArn, String message);

    Promise<PublishResponse> publish(String topicArn, String message, String subject);

    Promise<RemovePermissionResponse> removePermission(RemovePermissionRequest removePermissionRequest);

    Promise<RemovePermissionResponse> removePermission(String topicArn, String label);

    Promise<SetEndpointAttributesResponse> setEndpointAttributes(SetEndpointAttributesRequest setEndpointAttributesRequest);

    Promise<SetPlatformApplicationAttributesResponse> setPlatformApplicationAttributes(SetPlatformApplicationAttributesRequest setPlatformApplicationAttributesRequest);

    Promise<SetSmsAttributesResponse> setSMSAttributes(SetSmsAttributesRequest setSMSAttributesRequest);

    Promise<SetSubscriptionAttributesResponse> setSubscriptionAttributes(SetSubscriptionAttributesRequest setSubscriptionAttributesRequest);

    Promise<SetSubscriptionAttributesResponse> setSubscriptionAttributes(String subscriptionArn, String attributeName, String attributeValue);

    Promise<SetTopicAttributesResponse> setTopicAttributes(SetTopicAttributesRequest setTopicAttributesRequest);

    Promise<SetTopicAttributesResponse> setTopicAttributes(String topicArn, String attributeName, String attributeValue);

    Promise<SubscribeResponse> subscribe(SubscribeRequest subscribeRequest);

    Promise<SubscribeResponse> subscribe(String topicArn, String protocol, String endpoint);

    Promise<UnsubscribeResponse> unsubscribe(UnsubscribeRequest unsubscribeRequest);

    Promise<UnsubscribeResponse> unsubscribe(String subscriptionArn);

    void shutdown();

    void triggerFailover();
}
