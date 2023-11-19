[![Java CI with Maven](https://github.com/aokush/waiter/actions/workflows/maven.yml/badge.svg)](https://github.com/aokush/waiter/actions/workflows/maven.yml)

# Waiter

Have you ever been in a situation where you needed to check for a certain condition in a remote process before you can execute a dependent step? Look no further, Waiter is just what you need. It is designed to check a condition of your choice at regular interval for a duration until the condition specified becomes valid or the wait time elapses (times out).

Waiter requires an intance of [io.vertx.core.Vertx](https://vertx.io/) 4.x.x to operate; therefore, may be more application for projects where Vertx is used.

# Use

1. Create an instance of `io.kush.waiter.Condition` that returns Vertx `Future<Boolean>`

```
Condition<Future<Boolean>> condition = () -> Future.<Boolean>future(prms -> {

                    // For example, check database table contains a record
                    if (record exists) {
                        prms.complete(true);
                    }

                });
```

2. Create an instance of `io.kush.waiter.Waiter` with a Vertx instance and the Condition instance. A Waiter instance can be configured as needed to control the intervals of the condition evaluation and the max time to wait before timing out. See examples below. <br>
   Creates an instance of Waiter with default interval (2 secs) and max wait time (60 secs). This condition is evaluated every 2 secs until it returns `true` or until 60 secs after which the operation times out.

   ```
   Waiter.instance(vertx)
      .condition(condition)
      .fire()
   ```

   <br>

   Creates an instance of Waiter with default interval (5 secs) and max wait time (60 secs). This condition is evaluated every 2 secs until it returns `true` or until 60 secs after which the operation times out.

   ```
   Waiter.instance(vertx)
      .interval(5, TimeUnit.SECONDS)
      .condition(condition)
      .fire()
   ```

   <br>
   Creates an instance of Waiter with custom interval (5 secs) and max wait time (5 mins). This condition is evaluated every 5 secs until it returns ```true``` or until 5 minutes after which the operation times out.

   ```
   Waiter.instance(vertx)
      .interval(5, TimeUnit.SECONDS)
      .maxWait(5, TimeUnit.MINUTES)
      .condition(condition)
      .fire()
   ```
