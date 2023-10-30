# Waiter

Have you ever been in a situation where you need to check for a certain condition in a remote process before you can execute a dependent step? Look no further, Waiter is just what you need. It is designed to check a condition of your choice at regular interval for a period until the condition specified becomes valid or the wait time elapses (times out).

Waiter requires an intance of Vertx to operate; therefore, may be more application for projects where Vertx is used.

# Use

1. Create an instance of Condition that returns Vertx Future<Boolean>

```
Condition<Future<Boolean>> condition = () -> Future.<Boolean>future(prms -> {

                    // For example, check database table contains a record
                    if (record exists) {
                        prms.complete(true);
                    }

                });
```

2. Create an instance of Waiter with a Vertx instance and the Condition instance. A Waiter instance can be configured as needed to control the intervals of the condition evaluation and the max time to wait beofre timing out. See examples below.

   - `Waiter.instance(vertx).condition(condition).fire()`
     Creates an instance of Waiter with default interval (2 secs) and max wait time (60 secs). The condition is evaluated every 2 secs until it returns true or until 60 secs after which the operation times out.

   - `Waiter.instance(vertx).interval(5, TimeUnit.SECONDS).condition(condition).fire()`
     Creates an instance of Waiter with custom interval (5 secs) and default max wait time (60 secs). The condition is evaluated every 5 secs until it returns true or until 60 secs after which the operation times out.

   - `Waiter.instance(vertx).interval(5, TimeUnit.SECONDS).maxWait(5, TimeUnit.MINUTES).condition(condition).fire()`
     Creates an instance of Waiter with custom interval (5 secs) and max wait time (60 mins). The condition is evaluated every 5 secs until it returns true or until 60 minutes after which the operation times out.
