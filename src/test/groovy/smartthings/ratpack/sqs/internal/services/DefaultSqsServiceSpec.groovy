package smartthings.ratpack.sqs.internal.services

import ratpack.test.exec.ExecHarness
import smartthings.ratpack.sqs.DefaultSqsService
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*
import spock.lang.AutoCleanup
import spock.lang.Specification

class DefaultSqsServiceSpec extends Specification {

    SqsClient sqs = Mock(SqsClient)

    @AutoCleanup
    ExecHarness harness = ExecHarness.harness()

    void 'it should delete a message'() {
        given:
        DefaultSqsService service = new DefaultSqsService(sqs)
        DeleteMessageRequest request = DeleteMessageRequest.builder().build()
        DeleteMessageResponse response = DeleteMessageResponse.builder().build()

        when:
        def result = harness.yieldSingle{ e ->
            service.deleteMessage(request)
        }.value

        then:
        1 * sqs.deleteMessage(request) >> response
        0 * _

        and:
        assert result == response
    }

    void 'it should send a message'() {
        given:
        DefaultSqsService service = new DefaultSqsService(sqs)
        SendMessageRequest request = SendMessageRequest.builder().build()
        SendMessageResponse response = SendMessageResponse.builder().build()

        when:
        def result = harness.yieldSingle{ e ->
            service.sendMessage(request)
        }.value

        then:
        1 * sqs.sendMessage(request) >> response
        0 * _

        and:
        assert result == response
    }

    void 'it should receive a message'() {
        given:
        DefaultSqsService service = new DefaultSqsService(sqs)
        ReceiveMessageRequest request = ReceiveMessageRequest.builder().build()
        ReceiveMessageResponse response = ReceiveMessageResponse.builder().build()

        when:
        def result = harness.yieldSingle{ e ->
            service.receiveMessage(request)
        }.value

        then:
        1 * sqs.receiveMessage(request) >> response
        0 * _

        and:
        assert result == response
    }

    void 'it should get a queue url'() {
        given:
        DefaultSqsService service = new DefaultSqsService(sqs)
        String queueName = 'mars-10'
        GetQueueUrlResponse response = GetQueueUrlResponse.builder().build()

        when:
        def result = harness.yieldSingle{ e ->
            service.getQueueUrl(queueName)
        }.value

        then:
        1 * sqs.getQueueUrl({request ->
            request.queueName() == queueName
        } as GetQueueUrlRequest) >> response
        0 * _

        and:
        assert result == response
    }
}
