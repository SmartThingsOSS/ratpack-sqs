package smartthings.ratpack.sqs.circuitbreaker;


import ratpack.exec.Operation;

/**
 * CircuitBreaker interface.
 */
public interface CircuitBreaker {

    void init(CircuitBreakerListener listener);

    Operation blockIfOpen();

    boolean isOpen();

    void open();

    void close();

    void destroy();

}
