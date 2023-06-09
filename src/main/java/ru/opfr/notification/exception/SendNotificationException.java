package ru.opfr.notification.exception;

public class SendNotificationException extends Exception {
    public SendNotificationException(String message) {
        super(message);
    }

    public SendNotificationException(Throwable e) {
        super(e);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getMessage();
    }

}
