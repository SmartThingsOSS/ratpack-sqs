package smartthings.ratpack.sqs.circuitbreaker

import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

class SimpleCircuitBreakerSpec extends Specification {

    @Shared
    SimpleCircuitBreaker circuitBreaker = new SimpleCircuitBreaker()

    void 'it should know when closed'() {
        when:
        boolean open = circuitBreaker.isOpen()

        then:
        assert !open
    }

    void 'it should know when open'() {
        when:
        circuitBreaker.open()
        boolean open = circuitBreaker.isOpen()

        then:
        assert open
    }

    void 'it should notify on state changes'() {
        given:
        BlockingVariable<Boolean> openDone = new BlockingVariable<>(3)
        BlockingVariable<Boolean> closeDone = new BlockingVariable<>(3)
        CircuitBreakerListener listener = new CircuitBreakerListener() {
            @Override
            void opened() {
                openDone.set(true)
            }

            @Override
            void closed() {
                closeDone.set(true)
            }
        }

        and:
        circuitBreaker.init(listener)

        when:
        circuitBreaker.close()

        then:
        assert closeDone.get()

        when:
        circuitBreaker.open()

        then:
        assert openDone.get()
    }

    void 'it should destroy'() {
        when:
        circuitBreaker.destroy()

        then:
        noExceptionThrown()
    }
}
