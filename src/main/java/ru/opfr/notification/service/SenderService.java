package ru.opfr.notification.service;

import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

public interface SenderService {
    NotificationTypeDictionary getType();
    boolean sendNotificationWorkflow(Notification notification);
    boolean send(Notification notification) throws SendNotificationException;
    void afterSending(Notification notification, boolean result) throws SendNotificationException;
    String getSendingResultMessage();
}
