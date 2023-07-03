package io.kush.waiter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class WaitUtility {
    private static final Logger LOGGER = Logger.getLogger(WaitUtility.class.getName());

    private Vertx vertx;
    private static WaitUtility instance;
    private long initialDelay = 1;
    private long maxWait = 60000;
    private long interval = 2000;
    private Condition<Future<Boolean>> condition;

    private WaitUtility(Vertx vertx) {
        this.vertx = vertx;
    }

    public static WaitUtility instance(Vertx vertx) {
        instance = new WaitUtility(vertx);
        return instance;
    }

    public WaitUtility condition(Condition<Future<Boolean>> condition) {
        instance.condition = condition;
        return instance;
    }

    public WaitUtility initialDelay(int val, TimeUnit timeUnit) {

        switch (timeUnit) {
            case DAYS:
                initialDelay = Duration.ofDays(val).toMillis();
                break;
            case HOURS:
                initialDelay = Duration.ofHours(val).toMillis();
                break;
            case MINUTES:
                initialDelay = Duration.ofMinutes(val).toMillis();
                break;
            case SECONDS:
                initialDelay = Duration.ofSeconds(val).toMillis();
                break;
            case MILLISECONDS:
                initialDelay = Duration.ofMillis(val).toMillis();

        }

        LOGGER.log(Level.FINEST, "initial lDelay => {0} ms", initialDelay);
        return instance;
    }

    public WaitUtility maxWait(int val, TimeUnit timeUnit) {

        switch (timeUnit) {
            case DAYS:
                maxWait = Duration.ofDays(val).toMillis();
                break;
            case HOURS:
                maxWait = Duration.ofHours(val).toMillis();
                break;
            case MINUTES:
                maxWait = Duration.ofMinutes(val).toMillis();
                break;
            case SECONDS:
                maxWait = Duration.ofSeconds(val).toMillis();
                break;
            case MILLISECONDS:
                maxWait = Duration.ofMillis(val).toMillis();

        }

        LOGGER.log(Level.FINEST, "Max wait => {0} ms", maxWait);
        return instance;
    }

    public WaitUtility interval(int val, TimeUnit timeUnit) {

        switch (timeUnit) {
            case DAYS:
                interval = Duration.ofDays(val).toMillis();
                break;
            case HOURS:
                interval = Duration.ofHours(val).toMillis();
                break;
            case MINUTES:
                interval = Duration.ofMinutes(val).toMillis();
                break;
            case SECONDS:
                interval = Duration.ofSeconds(val).toMillis();
                break;
            case MILLISECONDS:
                interval = Duration.ofMillis(val).toMillis();
        }

        LOGGER.log(Level.FINEST, "Interval => {0} ms", interval);
        return instance;
    }

    public Future<Boolean> fire() {
        assert initialDelay < maxWait && interval < maxWait : "initial delay and interval must be less than max wait";
        AtomicBoolean isDone = new AtomicBoolean();
        return Future.<Boolean>future(prms -> {

            long timerId = vertx.setPeriodic(initialDelay, interval, id -> {
                LOGGER.log(Level.FINEST, "checking condition");
                condition.check().onSuccess(res -> {
                    if (res) {
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
                // only cancel timer is check is not successful yet
                if (!isDone.get()) {
                    LOGGER.log(Level.FINEST, "Cancelling condition check timer");
                    vertx.cancelTimer(timerId);
                    throw new RuntimeException(maxWait + "ms threshold exceeded");
                }
            });
        });

    }

}
