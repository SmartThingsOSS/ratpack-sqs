package smartthings.ratpack.sqs.consumer

import com.google.common.reflect.TypeToken
import ratpack.exec.Blocking
import ratpack.exec.Operation
import ratpack.registry.Registry
import ratpack.service.StartEvent
import ratpack.service.StopEvent
import ratpack.test.exec.ExecHarness
import smartthings.ratpack.sqs.SqsModule
import smartthings.ratpack.sqs.SqsService
import smartthings.ratpack.sqs.circuitbreaker.CircuitBreaker
import smartthings.ratpack.sqs.circuitbreaker.CircuitBreakerListener
import smartthings.ratpack.sqs.event.SqsConsumerEvent
import spock.lang.Specification

class DefaultSqsConsumerServiceSpec extends Specification {

    SqsService sqsService = Mock()

    void 'it should handle a service startup and shutdown'() {
        given:
        StartEvent startEvent = Mock()
        StopEvent stopEvent = Mock()
        Registry registry = Mock()
        Consumer consumer1 = Mock()
        Consumer consumer2 = Mock()
        CircuitBreaker circuitBreaker = Mock()
        SqsModule.Config config = buildConfig(true)
        DefaultSqsConsumerService defaultSqsConsumerService = new DefaultSqsConsumerService(config, sqsService)

        when:
        ExecHarness.runSingle({ e ->
            defaultSqsConsumerService.onStart(startEvent)
        })

        then:
        _ * startEvent.registry >> registry
        1 * registry.getAll(TypeToken.of(Consumer)) >> [consumer1, consumer2]
        1 * registry.get(CircuitBreaker) >> circuitBreaker
        1 * circuitBreaker.init(_ as CircuitBreakerListener)
        1 * consumer1.concurrencyLevel >> 1
        1 * consumer2.concurrencyLevel >> 1
        1 * consumer1.eventHandler(SqsConsumerEvent.STARTED_EVENT)
        1 * consumer2.eventHandler(SqsConsumerEvent.STARTED_EVENT)
        2 * circuitBreaker.blockIfOpen() >> Operation.of({
            // Prevents consumers from beginning poll loop.
            Blocking.get({
                while(true) {}
            })
        })

        when:
        ExecHarness.runSingle({ e ->
            defaultSqsConsumerService.onStop(stopEvent)
        })

        then:
        1 * consumer1.eventHandler(SqsConsumerEvent.STOPPED_EVENT)
        1 * consumer2.eventHandler(SqsConsumerEvent.STOPPED_EVENT)
        0 * _
    }

    void 'it should not initialize when sqs is disabled'() {
        given:
        StartEvent startEvent = Mock()
        SqsModule.Config config = buildConfig(false)
        DefaultSqsConsumerService defaultSqsConsumerService = new DefaultSqsConsumerService(config, sqsService)

        when:
        ExecHarness.runSingle({ e ->
            defaultSqsConsumerService.onStart(startEvent)
        })

        then:
        0 * _
    }

    SqsModule.Config buildConfig(boolean enabled) {
        SqsModule.Config config = new SqsModule.Config()
        config.enabled = enabled
        return config
    }
}
