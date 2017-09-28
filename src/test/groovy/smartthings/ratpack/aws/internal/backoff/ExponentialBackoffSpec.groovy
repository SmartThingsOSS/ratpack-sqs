package smartthings.ratpack.aws.internal.backoff

import spock.lang.Specification
import spock.lang.Unroll

class ExponentialBackoffSpec extends Specification {

    void setup() {

    }

    @Unroll
    void 'it should wait for #expectedWaitTime ms when marked #markTimes times'() {
        given:
        ExponentialBackoff backoff = new ExponentialBackoff()
        markTimes.times {
            backoff.mark()
        }

        when:
        long result = backoff.waitTime()

        then:
        assert result == expectedWaitTime

        where:
        markTimes | expectedWaitTime
        1         | 2000
        2         | 4000
        3         | 8000
        4         | 16000
        5         | 32000
        6         | 60000
        7         | 60000
        8         | 60000
        9         | 60000
        10        | 60000
    }
}
