package io.kush.waiter;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class Waiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Waiter.class);
    private Vertx vertx;
    private static Waiter instance;
    private long initialDelay = 1;
    private long maxWait = 60000;
    private long interval = 2000;
    private Condition<Future<Boolean>> condition;

    private Waiter(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Creates an instance.
     *
     * @param vertx An intance of Vertx
     * @return
     */
    public static Waiter instance(Vertx vertx) {
        instance = new Waiter(vertx);
        return instance;
    }

    /**
     * Sets the condition to evaluate.
     *
     * @param condition The operation to execute to determine success ot failure
     * @return The current instance of this class
     */
    public Waiter condition(Condition<Future<Boolean>> condition) {
        instance.condition = condition;
        return instance;
    }

    /**
     * How long to wait before starting to check condition status
     *
     * @param val      Positive whole numder
     * @param timeUnit Unit of time specified
     * @return The current instance of this class
     */
    public Waiter initialDelay(int val, TimeUnit timeUnit) {
        initialDelay = toMillis(val, timeUnit);
        LOGGER.debug("initial Delay => {} ms", initialDelay);
        return instance;
    }

    /**
     * Maximum time to wait before timing out.
     *
     * @param val      Positive whole numder
     * @param timeUnit Unit of time specified
     * @return The current instance of this class
     */
    public Waiter maxWait(int val, TimeUnit timeUnit) {
        maxWait = toMillis(val, timeUnit);
        LOGGER.debug("Max wait => {} ms", maxWait);
        return instance;
    }

    /**
     * How often should the condition be checked
     *
     * @param val      Positive whole numder
     * @param timeUnit Unit of time specified
     * @return The current instance of this class
     */
    public Waiter interval(int val, TimeUnit timeUnit) {
        interval = toMillis(val, timeUnit);
        LOGGER.debug("Interval => {} ms", interval);
        return instance;
    }

    private long toMillis(int val, TimeUnit timeUnit) {

        long inMillis = 0;

        switch (timeUnit) {
            case DAYS:
                inMillis = Duration.ofDays(val).toMillis();
                break;
            case HOURS:
                inMillis = Duration.ofHours(val).toMillis();
                break;
            case MINUTES:
                inMillis = Duration.ofMinutes(val).toMillis();
                break;
            case SECONDS:
                inMillis = Duration.ofSeconds(val).toMillis();
                break;
            case MILLISECONDS:
                inMillis = Duration.ofMillis(val).toMillis();
                break;
            default:
                throw new ConfigException("Invalid time unit");

        }
        return inMillis;
    }

    /**
     * Triggers evaluation for this waiter.
     * Should be the last operation invoked.
     *
     * @return A Future of Boolean that ios failed if condition
     *         does not succeeded before max wait time
     */
    public Future<Boolean> fire() {
        checkConfig();
        AtomicBoolean isDone = new AtomicBoolean();
        return Future.<Boolean>future(prms -> {

            long timerId = vertx.setPeriodic(initialDelay, interval, id -> {
                LOGGER.debug("Checking condition");
                condition.check().onSuccess(res -> {
                    if (res.booleanValue()) {
                        LOGGER.debug("Condition succeeded");
                        vertx.cancelTimer(id);
                        isDone.set(res);
                        prms.complete(res);
                    } else {
                        LOGGER.debug("Condition failed");
                    }
                });

            });

            vertx.setTimer(maxWait, id -> {
                // only cancel timer if condition is not successful yet
                if (!isDone.get()) {
                    LOGGER.debug("Cancelling condition check timer");
                    vertx.cancelTimer(timerId);
                    prms.fail(new TimeoutException(maxWait + "ms threshold exceeded"));
                }
            });
        });

    }

    private void checkConfig() {

        if (maxWait <= initialDelay || maxWait <= interval) {
            throw new ConfigException("initial delay and interval must be less than max wait");
        }

        if (Objects.isNull(condition)) {
            throw new ConfigException("Instance of " + Condition.class.getName() + " is required");
        }

    }

}
