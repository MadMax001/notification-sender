package ru.opfr.notification.aspects.service;

public interface LogService {
    default void trace(String info) {}
    default void debug(String info) {}
    void info(String info);
    default void warn(String info) {}
    void error(String info);
    void error(String message, Throwable exception);
}
