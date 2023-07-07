package io.kush.waiter;

public class TimeoutException extends Exception {

    public TimeoutException(String msg) {
        super(msg);
    }

    public TimeoutException(Throwable t) {
        super(t);
    }
}
