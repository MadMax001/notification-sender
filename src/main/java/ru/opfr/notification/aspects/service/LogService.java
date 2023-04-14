package ru.opfr.notification.aspects.service;

import ru.opfr.notification.aspects.LogInfoMode;

public interface LogService {
    default void trace(String info) {}
    default void debug(String info) {}
    void info(String info);
    void info(String info, LogInfoMode logInfoMode);
    default void warn(String info) {}
    void error(String info);
    void error(String message, Throwable exception);
}
