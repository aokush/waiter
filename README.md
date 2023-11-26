# Waiter

[![Java CI with Maven](https://github.com/aokush/waiter/actions/workflows/maven.yml/badge.svg)](https://github.com/aokush/waiter/actions/workflows/maven.yml)

Have you ever been in a situation where you needed to check for a certain condition in a remote process before you can execute a dependent step? Look no further, Waiter is just what you need. It is designed to check a condition of your choice at a regular interval for a duration until the condition specified becomes valid or the duration elapses (times out).

Waiter requires an intance of [io.vertx.core.Vertx](https://vertx.io/) version 4.x.x to operate; therefore, may be more application for projects where Vertx is used.

## Use

1. Create an instance of `io.kush.waiter.Condition` that returns Vertx `Future<Boolean>`

   ```java
    Condition<Future<Boolean>> condition = () -> Future.<Boolean>future(prms -> {

                    // For example, check database table contains a record
                    Object dbRec = <Find record in DB>;
                    prms.complete(Objects.nonNull(dbRec));
                });
   ```

2. Create an instance of `io.kush.waiter.Waiter` with a Vertx and Condition instances as parameters. A Waiter instance can be configured to control the intervals of the condition evaluation and the max time to wait before the operation terminates with a `io.kush.waiter.TimeoutException`. See examples below.
   &nbsp;
   **Example 1**

   > ```java
   >        Waiter.instance(vertx)
   >          .condition(condition)
   >          .fire()
   >
   > ```
   >
   > Creates an instance of Waiter with default interval (2 secs) and max wait time (60 secs). This condition is evaluated every 2 secs until either its future completes with true or for 60 secs after which the operation terminates.

   &nbsp;
   **Example 2**

   > ```java
   >   Waiter.instance(vertx)
   >     .interval(5, TimeUnit.SECONDS)
   >     .condition(condition)
   >     .fire()
   > ```
   >
   > Creates an instance of Waiter with default interval (5 secs) and max wait time (60 secs). This condition is evaluated every 2 secs until either its future completes with true or for 60 secs after which the operation terminates.

   &nbsp;
   **Example 3**

   > ```java
   > Waiter.instance(vertx)
   >    .interval(5, TimeUnit.SECONDS)
   >    .maxWait(5, TimeUnit.MINUTES)
   >    .condition(condition)
   >    .fire()
   > ```
   >
   > Creates an instance of Waiter with custom interval (5 secs) and max wait time (5 mins). This condition is evaluated every 5 secs until either its future completes with true or for 5 minutes after which the operation terminates.
