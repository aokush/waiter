package io.kush.waiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
@Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
public class WaiterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WaiterTest.class);

    private final long CONDITION_DELAY = 1000;
    private AtomicBoolean check = new AtomicBoolean();

    @BeforeEach
    void setup(Vertx vertx) {

        vertx.setTimer(CONDITION_DELAY, timerId -> {
            check.set(!check.get());
        });
    }

    @Test
    public void testCondition_success(Vertx vertx, VertxTestContext testCtxt) {

        createWaiter(vertx).condition(createCondition(vertx, true, CONDITION_DELAY)).fire()
                .onComplete(testCtxt.succeeding(res -> testCtxt.verify(() -> {
                    assertTrue(res);
                    testCtxt.completeNow();
                })));

    }

    @Test
    public void testCondition_failure(Vertx vertx, VertxTestContext testCtxt) {

        createWaiter(vertx).condition(createCondition(vertx, false, CONDITION_DELAY)).fire()
                .onComplete(testCtxt.failing(t -> testCtxt.verify(() -> {
                    assertEquals(TimeoutException.class, t.getClass());
                    testCtxt.completeNow();
                })));

    }

    @Test
    public void testInterval_invalid(Vertx vertx) {

        assertThrows(ConfigException.class,
                () -> {
                    createWaiter(vertx).condition(createCondition(vertx, true, CONDITION_DELAY))
                            .maxWait(1, TimeUnit.SECONDS)
                            .interval(2, TimeUnit.SECONDS).fire();
                });

    }

    @Test
    public void testInitialDelay_invalid(Vertx vertx) {

        assertThrows(ConfigException.class,
                () -> {
                    createWaiter(vertx).condition(createCondition(vertx, true, CONDITION_DELAY))
                            .maxWait(1, TimeUnit.SECONDS)
                            .initialDelay(2, TimeUnit.SECONDS).fire();
                });

    }

    @Test
    public void testCondition_missing(Vertx vertx) {

        assertThrows(ConfigException.class,
                () -> {
                    createWaiter(vertx).fire();
                });

    }

    @Test
    public void testTimeunit_allowed(Vertx vertx, VertxTestContext testCtxt) {

        List<Future<?>> waiterFuts = new ArrayList<>();
        waiterFuts.add(timeUnitHelper(1, TimeUnit.DAYS, vertx));
        waiterFuts.add(timeUnitHelper(1, TimeUnit.HOURS, vertx));
        waiterFuts.add(timeUnitHelper(1, TimeUnit.HOURS, vertx));
        waiterFuts.add(timeUnitHelper(1, TimeUnit.MINUTES, vertx));
        waiterFuts.add(timeUnitHelper(60, TimeUnit.SECONDS, vertx));
        waiterFuts.add(timeUnitHelper(6000, TimeUnit.MILLISECONDS, vertx));

        Future.all(waiterFuts).onComplete(testCtxt.succeedingThenComplete());

    }

    @Test
    public void testTimeunit_not_allowed_nanos(Vertx vertx) {

        assertThrows(ConfigException.class,
                () -> {
                    createWaiter(vertx).initialDelay(1, TimeUnit.NANOSECONDS).fire();
                });

    }

    @Test
    public void testTimeunit_not_allowed_micros(Vertx vertx) {

        assertThrows(ConfigException.class,
                () -> {
                    createWaiter(vertx).initialDelay(1, TimeUnit.MICROSECONDS).fire();
                });

    }

    private Future<Boolean> timeUnitHelper(int val, TimeUnit timeUnit, Vertx vertx) {

        return createWaiter(vertx).condition(createCondition(vertx, true, CONDITION_DELAY))
                .maxWait(val, timeUnit).fire();

    }

    private Waiter createWaiter(Vertx vertx) {
        return Waiter.instance(vertx).maxWait(5, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS);
    }

    private Condition<Future<Boolean>> createCondition(Vertx vertx, boolean successful, long delayInMs) {

        check.set(!successful);

        Condition<Future<Boolean>> condition = () -> Future
                .<Boolean>future(prms -> {

                    LOGGER.debug("check: {}", check.get());
                    if (check.get() == successful) {
                        LOGGER.debug("Completing");
                        prms.complete(successful);
                    }

                });

        return condition;

    }

};