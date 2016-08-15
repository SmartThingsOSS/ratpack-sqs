package smartthings.ratpack.sqs.event

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class SqsConsumerEventSpec extends Specification {

    @Unroll
    void 'it should determine object equality'() {

        when:
        boolean bool = object1.equals(object2)

        then:
        assert bool == result

        where:
        object1                           |  object2                      | result
        SqsConsumerEvent.RESUMED_EVENT  |  SqsConsumerEvent.STOPPED_EVENT | false
        SqsConsumerEvent.RESUMED_EVENT  |  SqsConsumerEvent.RESUMED_EVENT | true
        SqsConsumerEvent.RESUMED_EVENT  |  StandardCharsets.UTF_8         | false
        SqsConsumerEvent.RESUMED_EVENT  |  null                           | false
    }

    @Unroll
    void 'it should retrieve an event name'() {
        when:
        String name = event.getName()

        then:
        name == eventName

        where:
        event                              | eventName
        SqsConsumerEvent.STARTED_EVENT   | SqsConsumerEvent.STARTED_EVENT.getName()
        SqsConsumerEvent.STOPPED_EVENT   | SqsConsumerEvent.STOPPED_EVENT.getName()
        SqsConsumerEvent.SUSPENDED_EVENT | SqsConsumerEvent.SUSPENDED_EVENT.getName()
        SqsConsumerEvent.RESUMED_EVENT   | SqsConsumerEvent.RESUMED_EVENT.getName()
    }
}
