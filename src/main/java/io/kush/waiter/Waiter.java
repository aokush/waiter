package io.kush.waiter;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class Waiter {
    private static final Logger LOGGER = Logger.getLogger(Waiter.class.getName());

    private Vertx vertx;
    private static Waiter instance;
    private long initialDelay = 1;
    private long maxWait = 60000;
    private long interval = 2000;
    private Condition<Future<Boolean>> condition;

    private Waiter(Vertx vertx) {
        this.vertx = vertx;
    }

    public static Waiter instance(Vertx vertx) {
        instance = new Waiter(vertx);
        return instance;
    }

    /**
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
     * @param timeUnit Unit of the interval time
     * @return The current instance of this class
     */
    public Waiter initialDelay(int val, TimeUnit timeUnit) {
        initialDelay = toMillis(val, timeUnit);
        LOGGER.log(Level.FINEST, "initial lDelay => {0} ms", initialDelay);
        return instance;
    }

    public Waiter maxWait(int val, TimeUnit timeUnit) {
        maxWait = toMillis(val, timeUnit);
        LOGGER.log(Level.FINEST, "Max wait => {0} ms", maxWait);
        return instance;
    }

    /**
     * How often should the condition be checked
     * 
     * @param val      Positive whole numder
     * @param timeUnit Unit of the interval time
     * @return The current instance of this class
     */
    public Waiter interval(int val, TimeUnit timeUnit) {
        interval = toMillis(val, timeUnit);
        LOGGER.log(Level.FINEST, "Interval => {0} ms", interval);
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
                throw new RuntimeException("Invalid time unit");

        }
        return inMillis;
    }

    /**
     * Triggers evuation of this waiter.
     * Should be the last operation invoked.
     * 
     * @return
     */
    public Future<Boolean> fire() {
        assert initialDelay < maxWait && interval < maxWait : "initial delay and interval must be less than max wait";
        assert Objects.nonNull(condition) : "Instance of " + Condition.class.getName() + " is required";
        AtomicBoolean isDone = new AtomicBoolean();
        return Future.<Boolean>future(prms -> {

            long timerId = vertx.setPeriodic(initialDelay, interval, id -> {
                LOGGER.log(Level.FINEST, "checking condition");
                condition.check().onSuccess(res -> {
                    if (res.booleanValue()) {
                        LOGGER.log(Level.FINEST, "Condition succeeded");
                        vertx.cancelTimer(id);
                        isDone.set(res);
                        prms.complete(res);
                    } else {
                        LOGGER.log(Level.FINEST, "Condition failed");
                    }
                });

            });

            vertx.setTimer(maxWait, id -> {
                // only cancel timer if condition is not successful yet
                if (!isDone.get()) {
                    LOGGER.log(Level.FINEST, "Cancelling condition check timer");
                    vertx.cancelTimer(timerId);
                    prms.fail(new TimeoutException(maxWait + "ms threshold exceeded"));
                }
            });
        });

    }

}
