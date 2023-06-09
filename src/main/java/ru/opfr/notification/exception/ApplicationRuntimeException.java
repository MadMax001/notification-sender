package ru.opfr.notification.exception;

public class ApplicationRuntimeException extends RuntimeException {
    public ApplicationRuntimeException(Throwable cause) {
        super(cause);
    }

    public ApplicationRuntimeException(String message) {
        super(message);
    }
}
