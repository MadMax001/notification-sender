package ru.opfr.notification.aspects.logging.service;

import ru.opfr.notification.aspects.logging.LogType;

public interface LogService {
    default void trace(String info) {}
    default void debug(String info) {}
    void info(String info);
    void info(String info, LogType logType);
    default void warn(String info) {}
    void error(String info);
    void error(String message, Throwable exception);
}
