package smartthings.ratpack.sqs.internal.services

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.DeleteMessageResult
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.amazonaws.services.sqs.model.SendMessageResult
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

class DefaultSqsServiceSpec extends Specification {

    AmazonSQS sqs = Mock(AmazonSQS)

    @AutoCleanup
    ExecHarness harness = ExecHarness.harness()

    void 'it should delete a message'() {
        given:
        smartthings.ratpack.sqs.DefaultSqsService service = new smartthings.ratpack.sqs.DefaultSqsService(sqs)
        DeleteMessageRequest request = new DeleteMessageRequest()
        DeleteMessageResult response = new DeleteMessageResult()

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
        smartthings.ratpack.sqs.DefaultSqsService service = new smartthings.ratpack.sqs.DefaultSqsService(sqs)
        SendMessageRequest request = new SendMessageRequest()
        SendMessageResult response = new SendMessageResult()

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
        smartthings.ratpack.sqs.DefaultSqsService service = new smartthings.ratpack.sqs.DefaultSqsService(sqs)
        ReceiveMessageRequest request = new ReceiveMessageRequest()
        ReceiveMessageResult response = new ReceiveMessageResult()

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
        smartthings.ratpack.sqs.DefaultSqsService service = new smartthings.ratpack.sqs.DefaultSqsService(sqs)
        String queueName = 'mars-10'
        GetQueueUrlResult response = new GetQueueUrlResult()

        when:
        def result = harness.yieldSingle{ e ->
            service.getQueueUrl(queueName)
        }.value

        then:
        1 * sqs.getQueueUrl(queueName) >> response
        0 * _

        and:
        assert result == response
    }
}
