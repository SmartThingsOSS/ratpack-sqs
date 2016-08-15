package smartthings.ratpack.sqs.consumer

import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness
import smartthings.ratpack.sqs.SqsService
import smartthings.ratpack.sqs.circuitbreaker.CircuitBreaker
import spock.lang.Specification

class ConsumerActionSpec extends Specification {

    SqsService sqsService = Mock()
    Consumer consumer = Mock()
    CircuitBreaker circuitBreaker = Mock()

    void 'it should poll sqs and gracefully shutdown after consumption'() {
        given:
        String queueName = 'viking-1'
        String queueUrl = "http://badurl/${queueName}"
        GetQueueUrlResult getQueueUrlResult = new GetQueueUrlResult().withQueueUrl(queueUrl)
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withMaxNumberOfMessages(20)
        Message message1 = new Message().withBody('Hi There').withReceiptHandle('msg-1')
        Message message2 = new Message().withBody('You Guys').withReceiptHandle('msg-2')
        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult().withMessages(message1, message2)

        ConsumerAction action = new ConsumerAction(sqsService, consumer, circuitBreaker)

        when:
        ExecHarness.runSingle({ e ->
            action.execute(e)
        })

        then:
        1 * circuitBreaker.blockIfOpen() >> Blocking.op({
            // Allow 1 poll to occur
            action.shutdown()
        })
        1 * consumer.queueName >> queueName
        1 * sqsService.getQueueUrl(queueName) >> Promise.value(getQueueUrlResult)
        1 * consumer.receiveMessageRequest >> receiveMessageRequest
        1 * sqsService.receiveMessage({ request ->
            return request.queueUrl == queueUrl &&
                   request.maxNumberOfMessages == receiveMessageRequest.maxNumberOfMessages
        } as ReceiveMessageRequest) >> Promise.value(receiveMessageResult)
        1 * consumer.consume(message1)
        1 * consumer.consume(message2)
        1 * sqsService.deleteMessage({ request ->
            return request.queueUrl == queueUrl &&
                   request.receiptHandle == message1.receiptHandle
        } as DeleteMessageRequest) >> Promise.value(null)
        1 * sqsService.deleteMessage({ request ->
            return request.queueUrl == queueUrl &&
                request.receiptHandle == message2.receiptHandle
        } as DeleteMessageRequest) >> Promise.value(null)
        0 * _
    }

    void 'it should continue polling on a failure to process a message'() {
        given:
        String queueName = 'viking-1'
        String queueUrl = "http://badurl/${queueName}"
        GetQueueUrlResult getQueueUrlResult = new GetQueueUrlResult().withQueueUrl(queueUrl)
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withMaxNumberOfMessages(20)
        Message message = new Message().withBody('Hi There').withReceiptHandle('msg-1')
        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult().withMessages(message)
        int callCount = 1

        ConsumerAction action = new ConsumerAction(sqsService, consumer, circuitBreaker)

        when:
        ExecHarness.runSingle({ e ->
            action.execute(e)
        })

        then:
        2 * circuitBreaker.blockIfOpen() >> Blocking.op({})
        1 * consumer.queueName >> queueName
        1 * sqsService.getQueueUrl(queueName) >> Promise.value(getQueueUrlResult)
        2 * consumer.receiveMessageRequest >> receiveMessageRequest
        2 * sqsService.receiveMessage({ request ->
            return request.queueUrl == queueUrl &&
                request.maxNumberOfMessages == receiveMessageRequest.maxNumberOfMessages
        } as ReceiveMessageRequest) >> Promise.value(receiveMessageResult)
        2 * consumer.consume(message) >> {
            if (callCount == 1) {
                callCount++
                throw new RuntimeException('Oops')
            } else {
                action.shutdown()
            }
        }
        2 * sqsService.deleteMessage({ request ->
            return request.queueUrl == queueUrl &&
                request.receiptHandle == message.receiptHandle
        } as DeleteMessageRequest) >> Promise.value(null)
        0 * _
    }
}
