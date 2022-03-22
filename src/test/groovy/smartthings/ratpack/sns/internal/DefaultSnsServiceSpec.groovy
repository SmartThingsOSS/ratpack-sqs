package smartthings.ratpack.sns.internal

import ratpack.test.exec.ExecHarness
import smartthings.ratpack.sns.AmazonSNSProvider
import smartthings.ratpack.sns.SnsModule
import smartthings.ratpack.sns.SnsService
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.*
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DefaultSnsServiceSpec extends Specification {

    @Shared
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
    SnsClient client1 = Mock(SnsClient)
    SnsClient client2 = Mock(SnsClient)
    SnsClient client3 = Mock(SnsClient)
    AmazonSNSProvider provider = Mock(AmazonSNSProvider) {
        1 * get(config.endpoints.get(0)) >> client1
        1 * get(config.endpoints.get(1)) >> client2
        1 * get(config.endpoints.get(2)) >> client3
    }
    DefaultSnsService service

    void setup() {
        service = new DefaultSnsService(config, provider)
    }

    void 'it should create a topic'() {
        given:
        def request = CreateTopicRequest.builder().build()
        def result = CreateTopicResponse.builder().build()

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
        def request = SubscribeRequest.builder().build()
        def result = SubscribeResponse.builder().build()

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
        def request = PublishRequest.builder().build()
        def result = PublishResponse.builder().build()

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
        def request = DeleteTopicRequest.builder().build()
        def result = DeleteTopicResponse.builder().build()

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
        def request = AddPermissionRequest.builder().build()
        def result = AddPermissionResponse.builder().build()

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
        def result = AddPermissionResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.addPermission(topicArn, label, accountIds, actionNames)
        }.value

        then:
        1 * client1.addPermission( { request ->
            request.topicArn() == topicArn
            request.label() == label
            request.awsAccountIds() == accountIds
            request.actionNames() == actionNames
        } as AddPermissionRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should check if phone number is opted out'() {
        given:
        def request = CheckIfPhoneNumberIsOptedOutRequest.builder().build()
        def result = CheckIfPhoneNumberIsOptedOutResponse.builder().build()

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
        def request = ConfirmSubscriptionRequest.builder().build()
        def result = ConfirmSubscriptionResponse.builder().build()

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
        def result = ConfirmSubscriptionResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.confirmSubscription(topicArn, token, authenticateOnUnsubscribe)
        }.value

        then:
        1 * client1.confirmSubscription({ request ->
                request.topicArn() == topicArn
                request.token() == token
                request.authenticateOnUnsubscribe() == authenticateOnUnsubscribe
            } as ConfirmSubscriptionRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should confirm subscription simplified #2'() {
        given:
        String topicArn = 'arn'
        String token = 'token'
        def result = ConfirmSubscriptionResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.confirmSubscription(topicArn, token)
        }.value

        then:
        1 * client1.confirmSubscription({ request ->
            request.topicArn() == topicArn
            request.token() == token
        } as ConfirmSubscriptionRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should create platform application'() {
        given:
        def request = CreatePlatformApplicationRequest.builder().build()
        def result = CreatePlatformApplicationResponse.builder().build()

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
        def request = CreatePlatformEndpointRequest.builder().build()
        def result = CreatePlatformEndpointResponse.builder().build()

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
        def result = CreateTopicResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.createTopic(name)
        }.value

        then:
        1 * client1.createTopic({ request ->
            request.name() == name
        } as CreateTopicRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should delete an endpoint'() {
        given:
        def request = DeleteEndpointRequest.builder().build()
        def result = DeleteEndpointResponse.builder().build()

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
        def request = DeletePlatformApplicationRequest.builder().build()
        def result = DeletePlatformApplicationResponse.builder().build()

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
        def result = DeleteTopicResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.deleteTopic(topicArn)
        }.value

        then:
        1 * client1.deleteTopic({ request ->
            request.topicArn() == topicArn
        } as DeleteTopicRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should get endpoint attributes'() {
        given:
        def request = GetEndpointAttributesRequest.builder().build()
        def result = GetEndpointAttributesResponse.builder().build()

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
        def request = GetPlatformApplicationAttributesRequest.builder().build()
        def result = GetPlatformApplicationAttributesResponse.builder().build()

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
        def request = GetSmsAttributesRequest.builder().build()
        def result = GetSmsAttributesResponse.builder().build()

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
        def request = GetSubscriptionAttributesRequest.builder().build()
        def result = GetSubscriptionAttributesResponse.builder().build()

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
        def result = GetSubscriptionAttributesResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.getSubscriptionAttributes(subscriptionArn)
        }.value

        then:
        1 * client1.getSubscriptionAttributes({ request ->
            request.subscriptionArn() == subscriptionArn
        } as GetSubscriptionAttributesRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should get topic attributes'() {
        given:
        def request = GetTopicAttributesRequest.builder().build()
        def result = GetTopicAttributesResponse.builder().build()

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
        def result = GetTopicAttributesResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.getTopicAttributes(topicArn)
        }.value

        then:
        1 * client1.getTopicAttributes({ request ->
            request.topicArn() == topicArn
        } as GetTopicAttributesRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should list endpoints by platform application'() {
        given:
        def request = ListEndpointsByPlatformApplicationRequest.builder().build()
        def result = ListEndpointsByPlatformApplicationResponse.builder().build()

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
        def request = ListPhoneNumbersOptedOutRequest.builder().build()
        def result = ListPhoneNumbersOptedOutResponse.builder().build()

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
        def request = ListPlatformApplicationsRequest.builder().build()
        def result = ListPlatformApplicationsResponse.builder().build()

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
        def result = ListPlatformApplicationsResponse.builder().build()

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
        def request = ListSubscriptionsRequest.builder().build()
        def result = ListSubscriptionsResponse.builder().build()

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
        def result = ListSubscriptionsResponse.builder().build()

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
        def result = ListSubscriptionsResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptions(nextToken)
        }.value

        then:
        1 * client1.listSubscriptions({ request ->
            request.nextToken() == nextToken
        } as ListSubscriptionsRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should list subscriptions by topic'() {
        given:
        def request = ListSubscriptionsByTopicRequest.builder().build()
        def result = ListSubscriptionsByTopicResponse.builder().build()

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
        def result = ListSubscriptionsByTopicResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptionsByTopic(topicArn)
        }.value

        then:
        1 * client1.listSubscriptionsByTopic({ request ->
            request.topicArn() == topicArn
        } as ListSubscriptionsByTopicRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should list subscriptions by topic simplified #2'() {
        given:
        String topicArn = 'arn'
        String nextToken = 'next'
        def result = ListSubscriptionsByTopicResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.listSubscriptionsByTopic(topicArn, nextToken)
        }.value

        then:
        1 * client1.listSubscriptionsByTopic({ request ->
            request.topicArn() == topicArn
            request.nextToken() == nextToken
        } as ListSubscriptionsByTopicRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should list topics'() {
        given:
        def request = ListTopicsRequest.builder().build()
        def result = ListTopicsResponse.builder().build()

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
        def result = ListTopicsResponse.builder().build()

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
        def result = ListTopicsResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.listTopics(nextToken)
        }.value

        then:
        1 * client1.listTopics({ request ->
            request.nextToken() == nextToken
        } as ListTopicsRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should set opt in phone number'() {
        given:
        def request = OptInPhoneNumberRequest.builder().build()
        def result = OptInPhoneNumberResponse.builder().build()

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
        def result = PublishResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.publish(topicArn, message)
        }.value

        then:
        1 * client1.publish({ request ->
            request.topicArn() == topicArn
            request.message() == message
        } as PublishRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should publish simplified #2'() {
        given:
        String topicArn = 'arn'
        String message = 'message'
        String subject = 'subject'
        def result = PublishResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.publish(topicArn, message, subject)
        }.value

        then:
        1 * client1.publish({ request ->
            request.topicArn() == topicArn
            request.message() == message
            request.subject() == subject
        } as PublishRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should remove permission'() {
        given:
        def request = RemovePermissionRequest.builder().build()
        def result = RemovePermissionResponse.builder().build()

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
        def result = RemovePermissionResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.removePermission(topicArn, label)
        }.value

        then:
        1 * client1.removePermission({ request ->
            request.topicArn() == topicArn
            request.label() == label
        } as RemovePermissionRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should set endpoint attributes'() {
        given:
        def request = SetEndpointAttributesRequest.builder().build()
        def result = SetEndpointAttributesResponse.builder().build()

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
        def request = SetPlatformApplicationAttributesRequest.builder().build()
        def result = SetPlatformApplicationAttributesResponse.builder().build()

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
        def request = SetSmsAttributesRequest.builder().build()
        def result = SetSmsAttributesResponse.builder().build()

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
        def request = SetSubscriptionAttributesRequest.builder().build()
        def result = SetSubscriptionAttributesResponse.builder().build()

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
        def result = SetSubscriptionAttributesResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.setSubscriptionAttributes(subscriptionArn, attributeName, attributeValue)
        }.value

        then:
        1 * client1.setSubscriptionAttributes({ request ->
            request.subscriptionArn() == subscriptionArn
            request.attributeName() == attributeName
            request.attributeValue() == attributeValue
        } as SetSubscriptionAttributesRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should set topic attributes'() {
        given:
        def request = SetTopicAttributesRequest.builder().build()
        def result = SetTopicAttributesResponse.builder().build()

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
        def result = SetTopicAttributesResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.setTopicAttributes(topicArn, attributeName, attributeValue)
        }.value

        then:
        1 * client1.setTopicAttributes({ request ->
            request.topicArn() == topicArn
            request.attributeName() == attributeName
            request.attributeValue() == attributeValue
        } as SetTopicAttributesRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should subscribe simplified #1'() {
        given:
        String topicArn = 'arn'
        String protocol = 'protocol'
        String endpoint = 'endpoint'
        def result = SubscribeResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.subscribe(topicArn, protocol, endpoint)
        }.value

        then:
        1 * client1.subscribe({ request ->
            request.topicArn() == topicArn
            request.protocol() == protocol
            request.endpoint() == endpoint
        } as SubscribeRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should unsubscribe'() {
        given:
        def request = UnsubscribeRequest.builder().build()
        def result = UnsubscribeResponse.builder().build()

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
        def result = UnsubscribeResponse.builder().build()

        when:
        def response = harness.yieldSingle{ e ->
            service.unsubscribe(subscriptionArn)
        }.value

        then:
        1 * client1.unsubscribe({ request ->
            request.subscriptionArn() == subscriptionArn
        } as UnsubscribeRequest) >> result
        0 * _

        assert response == result
    }

    void 'it should failover and publish'() {
        given:
        def request = PublishRequest.builder().build()
        def result = PublishResponse.builder().build()

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
        SnsClient testClient = Mock(SnsClient)
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
        AwsServiceException ase = AwsServiceException.builder()
            .message('oops')
            .statusCode(statusCode)
            .build()

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
        1 * client1.close()
        1 * client2.close()
        1 * client3.close()
        0 * _
    }
}
