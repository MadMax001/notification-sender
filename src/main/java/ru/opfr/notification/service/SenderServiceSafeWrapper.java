package ru.opfr.notification.service;

import ru.opfr.notification.model.Notification;

import javax.validation.constraints.NotNull;

public interface SenderServiceSafeWrapper {

    boolean safeSend(@NotNull Notification notification);
}
