package smartthings.ratpack.sns;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.sns.model.*;
import ratpack.exec.Promise;
import ratpack.service.Service;

@SuppressWarnings("checkstyle:linelength")
public interface SnsService extends Service {

    Promise<AddPermissionResult> addPermission(AddPermissionRequest addPermissionRequest);

    Promise<AddPermissionResult> addPermission(String topicArn, String label, java.util.List<String> aWSAccountIds, java.util.List<String> actionNames);

    Promise<CheckIfPhoneNumberIsOptedOutResult> checkIfPhoneNumberIsOptedOut(CheckIfPhoneNumberIsOptedOutRequest checkIfPhoneNumberIsOptedOutRequest);

    Promise<ConfirmSubscriptionResult> confirmSubscription(ConfirmSubscriptionRequest confirmSubscriptionRequest);

    Promise<ConfirmSubscriptionResult> confirmSubscription(String topicArn, String token, String authenticateOnUnsubscribe);

    Promise<ConfirmSubscriptionResult> confirmSubscription(String topicArn, String token);

    Promise<CreatePlatformApplicationResult> createPlatformApplication(CreatePlatformApplicationRequest createPlatformApplicationRequest);

    Promise<CreatePlatformEndpointResult> createPlatformEndpoint(CreatePlatformEndpointRequest createPlatformEndpointRequest);

    Promise<CreateTopicResult> createTopic(CreateTopicRequest createTopicRequest);

    Promise<CreateTopicResult> createTopic(String name);

    Promise<DeleteEndpointResult> deleteEndpoint(DeleteEndpointRequest deleteEndpointRequest);

    Promise<DeletePlatformApplicationResult> deletePlatformApplication(DeletePlatformApplicationRequest deletePlatformApplicationRequest);

    Promise<DeleteTopicResult> deleteTopic(DeleteTopicRequest deleteTopicRequest);

    Promise<DeleteTopicResult> deleteTopic(String topicArn);

    Promise<GetEndpointAttributesResult> getEndpointAttributes(GetEndpointAttributesRequest getEndpointAttributesRequest);

    Promise<GetPlatformApplicationAttributesResult> getPlatformApplicationAttributes(GetPlatformApplicationAttributesRequest getPlatformApplicationAttributesRequest);

    Promise<GetSMSAttributesResult> getSMSAttributes(GetSMSAttributesRequest getSMSAttributesRequest);

    Promise<GetSubscriptionAttributesResult> getSubscriptionAttributes(GetSubscriptionAttributesRequest getSubscriptionAttributesRequest);

    Promise<GetSubscriptionAttributesResult> getSubscriptionAttributes(String subscriptionArn);

    Promise<GetTopicAttributesResult> getTopicAttributes(GetTopicAttributesRequest getTopicAttributesRequest);

    Promise<GetTopicAttributesResult> getTopicAttributes(String topicArn);

    Promise<ListEndpointsByPlatformApplicationResult> listEndpointsByPlatformApplication(
        ListEndpointsByPlatformApplicationRequest listEndpointsByPlatformApplicationRequest
    );

    Promise<ListPhoneNumbersOptedOutResult> listPhoneNumbersOptedOut(ListPhoneNumbersOptedOutRequest listPhoneNumbersOptedOutRequest);

    Promise<ListPlatformApplicationsResult> listPlatformApplications(ListPlatformApplicationsRequest listPlatformApplicationsRequest);

    Promise<ListPlatformApplicationsResult> listPlatformApplications();

    Promise<ListSubscriptionsResult> listSubscriptions(ListSubscriptionsRequest listSubscriptionsRequest);

    Promise<ListSubscriptionsResult> listSubscriptions();

    Promise<ListSubscriptionsResult> listSubscriptions(String nextToken);

    Promise<ListSubscriptionsByTopicResult> listSubscriptionsByTopic(ListSubscriptionsByTopicRequest listSubscriptionsByTopicRequest);

    Promise<ListSubscriptionsByTopicResult> listSubscriptionsByTopic(String topicArn);

    Promise<ListSubscriptionsByTopicResult> listSubscriptionsByTopic(String topicArn, String nextToken);

    Promise<ListTopicsResult> listTopics(ListTopicsRequest listTopicsRequest);

    Promise<ListTopicsResult> listTopics();

    Promise<ListTopicsResult> listTopics(String nextToken);

    Promise<OptInPhoneNumberResult> optInPhoneNumber(OptInPhoneNumberRequest optInPhoneNumberRequest);

    Promise<PublishResult> publish(PublishRequest publishRequest);

    Promise<PublishResult> publish(String topicArn, String message);

    Promise<PublishResult> publish(String topicArn, String message, String subject);

    Promise<RemovePermissionResult> removePermission(RemovePermissionRequest removePermissionRequest);

    Promise<RemovePermissionResult> removePermission(String topicArn, String label);

    Promise<SetEndpointAttributesResult> setEndpointAttributes(SetEndpointAttributesRequest setEndpointAttributesRequest);

    Promise<SetPlatformApplicationAttributesResult> setPlatformApplicationAttributes(SetPlatformApplicationAttributesRequest setPlatformApplicationAttributesRequest);

    Promise<SetSMSAttributesResult> setSMSAttributes(SetSMSAttributesRequest setSMSAttributesRequest);

    Promise<SetSubscriptionAttributesResult> setSubscriptionAttributes(SetSubscriptionAttributesRequest setSubscriptionAttributesRequest);

    Promise<SetSubscriptionAttributesResult> setSubscriptionAttributes(String subscriptionArn, String attributeName, String attributeValue);

    Promise<SetTopicAttributesResult> setTopicAttributes(SetTopicAttributesRequest setTopicAttributesRequest);

    Promise<SetTopicAttributesResult> setTopicAttributes(String topicArn, String attributeName, String attributeValue);

    Promise<SubscribeResult> subscribe(SubscribeRequest subscribeRequest);

    Promise<SubscribeResult> subscribe(String topicArn, String protocol, String endpoint);

    Promise<UnsubscribeResult> unsubscribe(UnsubscribeRequest unsubscribeRequest);

    Promise<UnsubscribeResult> unsubscribe(String subscriptionArn);

    Promise<ResponseMetadata> getCachedResponseMetadata(AmazonWebServiceRequest request);

    void shutdown();

    void triggerFailover();
}
