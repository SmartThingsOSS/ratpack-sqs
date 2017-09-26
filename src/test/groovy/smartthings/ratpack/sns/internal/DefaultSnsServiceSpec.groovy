package smartthings.ratpack.sns.internal

import com.amazonaws.AmazonServiceException
import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.ResponseMetadata
import com.amazonaws.services.sns.*
import com.amazonaws.services.sns.model.*
import ratpack.test.exec.ExecHarness
import smartthings.ratpack.sns.AmazonSNSProvider
import smartthings.ratpack.sns.SnsModule
import smartthings.ratpack.sns.SnsService
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

class DefaultSnsServiceSpec extends Specification {

    @AutoCleanup
    ExecHarness harness = ExecHarness.harness()

    SnsModule.Config config = new SnsModule.Config(
        enabled: true,
        endpoints: [
            new SnsModule.EndpointConfig(
                regionName: 'us-east-1',
                endpoint: 'http://localhost:4001'
            ),
            new SnsModule.EndpointConfig(
                regionName: 'us-east-2',
                endpoint: 'http://localhost:4002'
            ),
            new SnsModule.EndpointConfig(
                regionName: 'us-east-2',
                endpoint: 'http://localhost:4003'
            )
        ]
    )
    AmazonSNS client1 = Mock(AmazonSNS)
    AmazonSNS client2 = Mock(AmazonSNS)
    AmazonSNS client3 = Mock(AmazonSNS)
    AmazonSNSProvider provider = Mock(AmazonSNSProvider) {
        1 * get(config.endpoints.get(0)) >> client1
        1 * get(config.endpoints.get(1)) >> client2
        1 * get(config.endpoints.get(2)) >> client3
    }
    SnsService service

    void setup() {
        service = new DefaultSnsService(config, provider)
    }

    void 'it should create a topic'() {
        given:
        def request = new CreateTopicRequest()
        def result = new CreateTopicResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.createTopic(request)
        }.value

        then:
        1 * client1.createTopic(request) >> result
        0 * _

        assert response == result
    }

    void 'it should subscribe'() {
        given:
        def request = new SubscribeRequest()
        def result = new SubscribeResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.subscribe(request)
        }.value

        then:
        1 * client1.subscribe(request) >> result
        0 * _

        assert response == result
    }

    void 'it should publish'() {
        given:
        def request = new PublishRequest()
        def result = new PublishResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.publish(request)
        }.value

        then:
        1 * client1.publish(request) >> result
        0 * _

        assert response == result
    }

    void 'it should delete a topic'() {
        given:
        def request = new DeleteTopicRequest()
        def result = new DeleteTopicResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.deleteTopic(request)
        }.value

        then:
        1 * client1.deleteTopic(request) >> result
        0 * _

        assert response == result
    }

    void 'it should add permission'() {
        given:
        def request = new AddPermissionRequest()
        def result = new AddPermissionResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.addPermission(request)
        }.value

        then:
        1 * client1.addPermission(request) >> result
        0 * _

        assert response == result
    }

    void 'it should add permission simplified'() {
        given:
        String topicArn = 'arn'
        String label = 'label'
        List<String> accountIds = []
        List<String> actionNames = []
        def result = new AddPermissionResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.addPermission(topicArn, label, accountIds, actionNames)
        }.value

        then:
        1 * client1.addPermission(topicArn, label, accountIds, actionNames) >> result
        0 * _

        assert response == result
    }

    void 'it should check if phone number is opted out'() {
        given:
        def request = new CheckIfPhoneNumberIsOptedOutRequest()
        def result = new CheckIfPhoneNumberIsOptedOutResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.checkIfPhoneNumberIsOptedOut(request)
        }.value

        then:
        1 * client1.checkIfPhoneNumberIsOptedOut(request) >> result
        0 * _

        assert response == result
    }

    void 'it should confirm subscription'() {
        given:
        def request = new ConfirmSubscriptionRequest()
        def result = new ConfirmSubscriptionResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.confirmSubscription(request)
        }.value

        then:
        1 * client1.confirmSubscription(request) >> result
        0 * _

        assert response == result
    }

    void 'it should confirm subscription simplified #1'() {
        given:
        String topicArn = 'arn'
        String token = 'token'
        String authenticateOnUnsubscribe = 'auth'
        def result = new ConfirmSubscriptionResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.confirmSubscription(topicArn, token, authenticateOnUnsubscribe)
        }.value

        then:
        1 * client1.confirmSubscription(topicArn, token, authenticateOnUnsubscribe) >> result
        0 * _

        assert response == result
    }

    void 'it should confirm subscription simplified #2'() {
        given:
        String topicArn = 'arn'
        String token = 'token'
        def result = new ConfirmSubscriptionResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.confirmSubscription(topicArn, token)
        }.value

        then:
        1 * client1.confirmSubscription(topicArn, token) >> result
        0 * _

        assert response == result
    }

    void 'it should create platform application'() {
        given:
        def request = new CreatePlatformApplicationRequest()
        def result = new CreatePlatformApplicationResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.createPlatformApplication(request)
        }.value

        then:
        1 * client1.createPlatformApplication(request) >> result
        0 * _

        assert response == result
    }

    void 'it should create platform endpoint'() {
        given:
        def request = new CreatePlatformEndpointRequest()
        def result = new CreatePlatformEndpointResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.createPlatformEndpoint(request)
        }.value

        then:
        1 * client1.createPlatformEndpoint(request) >> result
        0 * _

        assert response == result
    }

    void 'it should create topic'() {
        given:
        String name = 'name'
        def result = new CreateTopicResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.createTopic(name)
        }.value

        then:
        1 * client1.createTopic(name) >> result
        0 * _

        assert response == result
    }

    void 'it should delete an endpoint'() {
        given:
        def request = new DeleteEndpointRequest()
        def result = new DeleteEndpointResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.deleteEndpoint(request)
        }.value

        then:
        1 * client1.deleteEndpoint(request) >> result
        0 * _

        assert response == result
    }

    void 'it should delete a platform application'() {
        given:
        def request = new DeletePlatformApplicationRequest()
        def result = new DeletePlatformApplicationResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.deletePlatformApplication(request)
        }.value

        then:
        1 * client1.deletePlatformApplication(request) >> result
        0 * _

        assert response == result
    }

    void 'it should delete a topic simplified'() {
        given:
        String topicArn = 'arn'
        def result = new DeleteTopicResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.deleteTopic(topicArn)
        }.value

        then:
        1 * client1.deleteTopic(topicArn) >> result
        0 * _

        assert response == result
    }

    void 'it should get endpoint attributes'() {
        given:
        def request = new GetEndpointAttributesRequest()
        def result = new GetEndpointAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.getEndpointAttributes(request)
        }.value

        then:
        1 * client1.getEndpointAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should get platform application attributes'() {
        given:
        def request = new GetPlatformApplicationAttributesRequest()
        def result = new GetPlatformApplicationAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.getPlatformApplicationAttributes(request)
        }.value

        then:
        1 * client1.getPlatformApplicationAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should get sms attributes'() {
        given:
        def request = new GetSMSAttributesRequest()
        def result = new GetSMSAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.getSMSAttributes(request)
        }.value

        then:
        1 * client1.getSMSAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should get subscription attributes'() {
        given:
        def request = new GetSubscriptionAttributesRequest()
        def result = new GetSubscriptionAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.getSubscriptionAttributes(request)
        }.value

        then:
        1 * client1.getSubscriptionAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should get subscription attributes simplified'() {
        given:
        String subscriptionArn = 'arn'
        def result = new GetSubscriptionAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.getSubscriptionAttributes(subscriptionArn)
        }.value

        then:
        1 * client1.getSubscriptionAttributes(subscriptionArn) >> result
        0 * _

        assert response == result
    }

    void 'it should get topic attributes'() {
        given:
        def request = new GetTopicAttributesRequest()
        def result = new GetTopicAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.getTopicAttributes(request)
        }.value

        then:
        1 * client1.getTopicAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should get topic attributes simplified'() {
        given:
        String topicArn = 'arn'
        def result = new GetTopicAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.getTopicAttributes(topicArn)
        }.value

        then:
        1 * client1.getTopicAttributes(topicArn) >> result
        0 * _

        assert response == result
    }

    void 'it should list endpoints by platform application'() {
        given:
        def request = new ListEndpointsByPlatformApplicationRequest()
        def result = new ListEndpointsByPlatformApplicationResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listEndpointsByPlatformApplication(request)
        }.value

        then:
        1 * client1.listEndpointsByPlatformApplication(request) >> result
        0 * _

        assert response == result
    }

    void 'it should list phone number opted out'() {
        given:
        def request = new ListPhoneNumbersOptedOutRequest()
        def result = new ListPhoneNumbersOptedOutResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listPhoneNumbersOptedOut(request)
        }.value

        then:
        1 * client1.listPhoneNumbersOptedOut(request) >> result
        0 * _

        assert response == result
    }

    void 'it should list platform applications'() {
        given:
        def request = new ListPlatformApplicationsRequest()
        def result = new ListPlatformApplicationsResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listPlatformApplications(request)
        }.value

        then:
        1 * client1.listPlatformApplications(request) >> result
        0 * _

        assert response == result
    }

    void 'it should list platform applications simplified'() {
        given:
        def result = new ListPlatformApplicationsResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listPlatformApplications()
        }.value

        then:
        1 * client1.listPlatformApplications() >> result
        0 * _

        assert response == result
    }

    void 'it should list subscriptions'() {
        given:
        def request = new ListSubscriptionsRequest()
        def result = new ListSubscriptionsResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptions(request)
        }.value

        then:
        1 * client1.listSubscriptions(request) >> result
        0 * _

        assert response == result
    }

    void 'it should list subscriptions simplified #1'() {
        given:
        def result = new ListSubscriptionsResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptions()
        }.value

        then:
        1 * client1.listSubscriptions() >> result
        0 * _

        assert response == result
    }

    void 'it should list subscriptions simplified #2'() {
        given:
        String nextToken = 'next'
        def result = new ListSubscriptionsResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptions(nextToken)
        }.value

        then:
        1 * client1.listSubscriptions(nextToken) >> result
        0 * _

        assert response == result
    }

    void 'it should list subscriptions by topic'() {
        given:
        def request = new ListSubscriptionsByTopicRequest()
        def result = new ListSubscriptionsByTopicResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptionsByTopic(request)
        }.value

        then:
        1 * client1.listSubscriptionsByTopic(request) >> result
        0 * _

        assert response == result
    }

    void 'it should list subscriptions by topic simplified #1'() {
        given:
        String topicArn = 'arn'
        def result = new ListSubscriptionsByTopicResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptionsByTopic(topicArn)
        }.value

        then:
        1 * client1.listSubscriptionsByTopic(topicArn) >> result
        0 * _

        assert response == result
    }

    void 'it should list subscriptions by topic simplified #2'() {
        given:
        String topicArn = 'arn'
        String nextToken = 'next'
        def result = new ListSubscriptionsByTopicResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptionsByTopic(topicArn, nextToken)
        }.value

        then:
        1 * client1.listSubscriptionsByTopic(topicArn, nextToken) >> result
        0 * _

        assert response == result
    }

    void 'it should list topics'() {
        given:
        def request = new ListTopicsRequest()
        def result = new ListTopicsResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listTopics(request)
        }.value

        then:
        1 * client1.listTopics(request) >> result
        0 * _

        assert response == result
    }

    void 'it should list topics simplified #1'() {
        given:
        def result = new ListTopicsResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listTopics()
        }.value

        then:
        1 * client1.listTopics() >> result
        0 * _

        assert response == result
    }

    void 'it should list topics simplified #2'() {
        given:
        String nextToken = 'next'
        def result = new ListTopicsResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.listTopics(nextToken)
        }.value

        then:
        1 * client1.listTopics(nextToken) >> result
        0 * _

        assert response == result
    }

    void 'it should set opt in phone number'() {
        given:
        def request = new OptInPhoneNumberRequest()
        def result = new OptInPhoneNumberResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.optInPhoneNumber(request)
        }.value

        then:
        1 * client1.optInPhoneNumber(request) >> result
        0 * _

        assert response == result
    }

    void 'it should publish simplified #1'() {
        given:
        String topicArn = 'arn'
        String message = 'message'
        def result = new PublishResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.publish(topicArn, message)
        }.value

        then:
        1 * client1.publish(topicArn, message) >> result
        0 * _

        assert response == result
    }

    void 'it should publish simplified #2'() {
        given:
        String topicArn = 'arn'
        String message = 'message'
        String subject = 'subject'
        def result = new PublishResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.publish(topicArn, message, subject)
        }.value

        then:
        1 * client1.publish(topicArn, message, subject) >> result
        0 * _

        assert response == result
    }

    void 'it should remove permission'() {
        given:
        def request = new RemovePermissionRequest()
        def result = new RemovePermissionResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.removePermission(request)
        }.value

        then:
        1 * client1.removePermission(request) >> result
        0 * _

        assert response == result
    }

    void 'it should remove permission simplified #1'() {
        given:
        String topicArn = 'arn'
        String label = 'label'
        def result = new RemovePermissionResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.removePermission(topicArn, label)
        }.value

        then:
        1 * client1.removePermission(topicArn, label) >> result
        0 * _

        assert response == result
    }

    void 'it should set endpoint attributes'() {
        given:
        def request = new SetEndpointAttributesRequest()
        def result = new SetEndpointAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.setEndpointAttributes(request)
        }.value

        then:
        1 * client1.setEndpointAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should set platform application attributes'() {
        given:
        def request = new SetPlatformApplicationAttributesRequest()
        def result = new SetPlatformApplicationAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.setPlatformApplicationAttributes(request)
        }.value

        then:
        1 * client1.setPlatformApplicationAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should set sms attributes'() {
        given:
        def request = new SetSMSAttributesRequest()
        def result = new SetSMSAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.setSMSAttributes(request)
        }.value

        then:
        1 * client1.setSMSAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should set subscription attributes'() {
        given:
        def request = new SetSubscriptionAttributesRequest()
        def result = new SetSubscriptionAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.setSubscriptionAttributes(request)
        }.value

        then:
        1 * client1.setSubscriptionAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should set subscription attributes simplified #1'() {
        given:
        String subscriptionArn = 'arn'
        String attributeName = 'attr'
        String attributeValue = 'value'
        def result = new SetSubscriptionAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.setSubscriptionAttributes(subscriptionArn, attributeName, attributeValue)
        }.value

        then:
        1 * client1.setSubscriptionAttributes(subscriptionArn, attributeName, attributeValue) >> result
        0 * _

        assert response == result
    }

    void 'it should set topic attributes'() {
        given:
        def request = new SetTopicAttributesRequest()
        def result = new SetTopicAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.setTopicAttributes(request)
        }.value

        then:
        1 * client1.setTopicAttributes(request) >> result
        0 * _

        assert response == result
    }

    void 'it should set topic attributes simplified #1'() {
        given:
        String topicArn = 'arn'
        String attributeName = 'attr'
        String attributeValue = 'value'
        def result = new SetTopicAttributesResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.setTopicAttributes(topicArn, attributeName, attributeValue)
        }.value

        then:
        1 * client1.setTopicAttributes(topicArn, attributeName, attributeValue) >> result
        0 * _

        assert response == result
    }

    void 'it should subscribe simplified #1'() {
        given:
        String topicArn = 'arn'
        String protocol = 'protocol'
        String endpoint = 'endpoint'
        def result = new SubscribeResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.subscribe(topicArn, protocol, endpoint)
        }.value

        then:
        1 * client1.subscribe(topicArn, protocol, endpoint) >> result
        0 * _

        assert response == result
    }

    void 'it should unsubscribe'() {
        given:
        def request = new UnsubscribeRequest()
        def result = new UnsubscribeResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.unsubscribe(request)
        }.value

        then:
        1 * client1.unsubscribe(request) >> result
        0 * _

        assert response == result
    }

    void 'it should unsubscribe simplified #1'() {
        given:
        String subscriptionArn = 'arn'
        def result = new UnsubscribeResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.unsubscribe(subscriptionArn)
        }.value

        then:
        1 * client1.unsubscribe(subscriptionArn) >> result
        0 * _

        assert response == result
    }

    void 'it should get cached response metadata'() {
        given:
        def request = AmazonWebServiceRequest.NOOP
        def result = new ResponseMetadata([:])

        when:
        def response = harness.yieldSingle{ e ->
            service.getCachedResponseMetadata(request)
        }.value

        then:
        1 * client1.getCachedResponseMetadata(request) >> result
        0 * _

        assert response == result
    }

    void 'it should failover and publish'() {
        given:
        def request = new PublishRequest()
        def result = new PublishResult()

        when:
        def response = harness.yieldSingle{ e ->
            service.publish(request)
        }.value

        then:
        1 * client1.publish(request) >> result
        0 * _

        assert response == result

        when:
        service.triggerFailover()

        response = harness.yieldSingle{ e ->
            service.publish(request)
        }.value

        then:
        1 * client2.publish(request) >> result
        0 * _

        assert response == result
    }

    void 'it should support triggering a failover'() {
        expect:
        assert service.sns() == client1

        when:
        service.triggerFailover()

        then:
        assert service.sns() == client2

        when:
        service.triggerFailover()

        then:
        assert service.sns() == client3

        when:
        service.triggerFailover()

        then:
        assert service.sns() == client1
    }

    void 'it should skip failover when only 1 client'() {
        setup:
        AmazonSNS testClient = Mock(AmazonSNS)
        SnsModule.Config testConfig = new SnsModule.Config(
            enabled: true,
            endpoints: [
                new SnsModule.EndpointConfig(
                    regionName: 'us-east-1',
                    endpoint: 'http://localhost:5555'
                )
            ]
        )
        AmazonSNSProvider testProvider = Mock(AmazonSNSProvider) {
            1 * get(testConfig.endpoints.get(0)) >> testClient
        }
        SnsService snsService = new DefaultSnsService(testConfig, testProvider)

        expect:
        assert snsService.sns() == testClient

        when:
        snsService.triggerFailover()

        then:
        assert snsService.sns() == testClient
    }

    void 'it should throw an error if attempting to use SNS when disabled'() {
        given:
        service = new DefaultSnsService(new SnsModule.Config(enabled: false), provider)

        when:
        service.sns()

        then:
        thrown(IllegalStateException)
    }

    @Unroll
    void 'it should detect a #statusCode from AWS service'() {
        given:
        AmazonServiceException ase = new AmazonServiceException('oops')
        ase.statusCode = statusCode

        when:
        boolean result = service.isAwsServiceError(ase)

        then:
        assert result == isError

        where:
        statusCode  | isError
        200         | false
        201         | false
        204         | false
        422         | false
        500         | true
        501         | true
        504         | true
        599         | true
        600         | false
    }

    void 'it should support shutdown of SNS clients'() {
        when:
        service.shutdown()

        then:
        1 * client1.shutdown()
        1 * client2.shutdown()
        1 * client3.shutdown()
        0 * _
    }
}
