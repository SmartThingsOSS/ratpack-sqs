package smartthings.ratpack.sqs

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.DeleteMessageResult
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.amazonaws.services.sqs.model.SendMessageResult
import ratpack.service.StartEvent
import ratpack.test.exec.ExecHarness
import smartthings.ratpack.sqs.config.SqsConfigService
import spock.lang.AutoCleanup
import spock.lang.Specification
import java.util.concurrent.CompletableFuture

class DefaultSqsServiceSpec extends Specification {

    AmazonSQSAsync sqs = Mock(AmazonSQSAsync)
    SqsConfigService sqsConfigService = Mock(SqsConfigService, {
        getAmazonSQSAsync(_) >> sqs
    })

    @AutoCleanup
    ExecHarness harness = ExecHarness.harness()

    void 'it should initialize on startup when enabled'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = true

        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        when:
        service.onStart(startEvent)

        then:
        1 * sqsConfigService.getAmazonSQSAsync(config.getRegion()) >> sqs
        0 * _

        and:
        0 * sqs.setEndpoint(config.getEndpoint())
    }

    void 'it should not initialize on startup when disabled'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = false

        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        when:
        service.onStart(startEvent)

        then:
        0 * _
    }

    void 'it should initialize with provided sqs endpoint'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.region = 'us-east-1'
        config.endpoint = 'http://localhost:9000'
        config.enabled = true

        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        when:
        service.onStart(startEvent)

        then:
        1 * sqs.setEndpoint(config.endpoint)
    }

    void 'it should delete a message'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = true
        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        and:
        DeleteMessageRequest request = new DeleteMessageRequest()
        DeleteMessageResult response = new DeleteMessageResult()

        when:
        service.onStart(startEvent)

        then:
        1 * sqsConfigService.getAmazonSQSAsync(config.getRegion()) >> sqs
        0 * _

        when:
        def result = harness.yieldSingle{ e ->
            service.deleteMessage(request)
        }.value

        then:
        1 * sqs.deleteMessageAsync(request) >> CompletableFuture.completedFuture(response)
        0 * _

        and:
        assert result == response
    }

    void 'it should send a message'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = true
        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        and:
        SendMessageRequest request = new SendMessageRequest()
        SendMessageResult response = new SendMessageResult()

        when:
        service.onStart(startEvent)

        then:
        1 * sqsConfigService.getAmazonSQSAsync(config.getRegion()) >> sqs
        0 * _

        when:
        def result = harness.yieldSingle{ e ->
            service.sendMessage(request)
        }.value

        then:
        1 * sqs.sendMessageAsync(request) >> CompletableFuture.completedFuture(response)
        0 * _

        and:
        assert result == response
    }

    void 'it should receive a message'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = true
        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        and:
        ReceiveMessageRequest request = new ReceiveMessageRequest()
        ReceiveMessageResult response = new ReceiveMessageResult()

        when:
        service.onStart(startEvent)

        then:
        1 * sqsConfigService.getAmazonSQSAsync(config.getRegion()) >> sqs
        0 * _

        when:
        def result = harness.yieldSingle{ e ->
            service.receiveMessage(request)
        }.value

        then:
        1 * sqs.receiveMessageAsync(request) >> CompletableFuture.completedFuture(response)
        0 * _

        and:
        assert result == response
    }

    void 'it should get a queue url'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = true
        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        and:
        String queueName = 'mars-10'
        GetQueueUrlResult response = new GetQueueUrlResult()

        when:
        service.onStart(startEvent)

        then:
        1 * sqsConfigService.getAmazonSQSAsync(config.getRegion()) >> sqs
        0 * _

        when:
        def result = harness.yieldSingle{ e ->
            service.getQueueUrl(queueName)
        }.value

        then:
        1 * sqs.getQueueUrlAsync(queueName) >> CompletableFuture.completedFuture(response)
        0 * _

        and:
        assert result == response
    }

    void 'it should throw an exception when sending message on a disabled instance'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = false

        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        when:
        service.onStart(startEvent)

        then:
        0 * _

        when:
        def result = harness.yieldSingle{ e ->
            service.sendMessage(new SendMessageRequest())
        }

        then:
        assert result.error
        assert result.throwable instanceof IllegalStateException
    }

    void 'it should throw an exception when deleting a message on a disabled instance'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = false

        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        when:
        service.onStart(startEvent)

        then:
        0 * _

        when:
        def result = harness.yieldSingle{ e ->
            service.deleteMessage(new DeleteMessageRequest())
        }

        then:
        assert result.error
        assert result.throwable instanceof IllegalStateException
    }

    void 'it should throw an exception when receiving a message on a disabled instance'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = false

        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        when:
        service.onStart(startEvent)

        then:
        0 * _

        when:
        def result = harness.yieldSingle{ e ->
            service.receiveMessage(new ReceiveMessageRequest())
        }

        then:
        assert result.error
        assert result.throwable instanceof IllegalStateException
    }

    void 'it should throw an exception when getting a queue url on a disabled instance'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'
        config.setRegion('us-east-1')
        config.enabled = false

        DefaultSqsService service = new DefaultSqsService(config, sqsConfigService)

        when:
        service.onStart(startEvent)

        then:
        0 * _

        when:
        def result = harness.yieldSingle{ e ->
            service.getQueueUrl('queue-name')
        }

        then:
        assert result.error
        assert result.throwable instanceof IllegalStateException
    }
}
