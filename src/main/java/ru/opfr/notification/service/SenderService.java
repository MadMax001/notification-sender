package ru.opfr.notification.service;

import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import javax.validation.constraints.NotNull;

public interface SenderService {
    NotificationTypeDictionary getType();
    boolean send(@NotNull Notification notification) throws SendNotificationException;
    void afterSending(@NotNull Notification notification, boolean result) throws SendNotificationException;
    String getSendingResultMessage();
    void setErrorMessage(Throwable e);
}
