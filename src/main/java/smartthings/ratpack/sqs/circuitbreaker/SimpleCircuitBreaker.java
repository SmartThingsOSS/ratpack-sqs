package smartthings.ratpack.sqs.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.exec.Operation;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides a simplistic implementation that allows suspension / resuming of polling operations.
 */
@ThreadSafe
public class SimpleCircuitBreaker implements CircuitBreaker {
    private static final Logger log = LoggerFactory.getLogger(SimpleCircuitBreaker.class);

    private final Object mutex = new Object();

    @GuardedBy("mutex")
    private boolean open = false;

    private CircuitBreakerListener listener;

    @Override
    public void init(CircuitBreakerListener listener) {
        synchronized (mutex) {
            log.debug("setting listener..");
            this.listener = listener;
        }
    }

    @Override
    public void destroy() {
        //do nothing
    }

    @Override
    public Operation blockIfOpen() {
        return Blocking.op(() -> {
            synchronized (mutex) {
                while (open) {
                    try {
                        log.debug("CircuitBreaker.blockIfOpen - calling wait");
                        mutex.wait();
                    } catch (InterruptedException e) {
                        //Intentionally left blank
                    }
                }
            }
        });
    }

    @Override
    public boolean isOpen() {
        synchronized (mutex) {
            return open;
        }
    }

    @Override
    public void open() {
        synchronized (mutex) {
            if (!open) {
                open = true;
                notifyOpened();
                log.info("CircuitBreaker opened");
                return;
            }
            log.info("CircuitBreaker already open");
        }
    }

    @Override
    public void close() {
        synchronized (mutex) {
            if (open) {
                open = false;
                notifyClosed();
                log.info("CircuitBreaker closing");
                mutex.notifyAll();
                return;
            }
            log.info("CircuitBreaker already closed");
        }
    }

    private void notifyOpened() {
        if (listener != null) {
            log.debug("notify open..");
            listener.opened();
        }
    }

    private void notifyClosed() {
        if (listener != null) {
            log.debug("notify close..");
            listener.closed();
        }
    }
}
