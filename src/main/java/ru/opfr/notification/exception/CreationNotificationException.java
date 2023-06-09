package ru.opfr.notification.exception;

public class CreationNotificationException extends Exception {
    public CreationNotificationException(String s) {
        super(s);
    }

    public CreationNotificationException(Throwable e) {
        super(e);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getMessage();
    }
}
