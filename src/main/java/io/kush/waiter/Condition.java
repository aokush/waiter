package io.kush.waiter;

@FunctionalInterface
public interface Condition<T> {
    T check();
}
