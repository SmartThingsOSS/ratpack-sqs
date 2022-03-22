package smartthings.ratpack.aws.internal.backoff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

/**
 * Provides a mechanism for backoff inside a Ratpack promise chain.
 */
public class ExponentialBackoff {

    private static final Logger log = LoggerFactory.getLogger(ExponentialBackoff.class);
    private static final long MAX_WAIT = 60000;
    private int attempts = 0;

    public void reset() {
        attempts = 0;
    }

    public Promise<Void> backoff() {
        return Blocking.op(() -> {
            log.debug("Circuit is OPEN.  Waiting...");
            Thread.sleep(waitTime());
            mark();
        }).promise();
    }

    private void mark() {
        attempts++;
    }

    private long waitTime() {
        final long wait = Math.round(Math.pow(2, attempts)) * 1000;
        return Math.min(wait, MAX_WAIT);
    }
}
