package ru.opfr.notification.service;

import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

public interface SenderService {
    NotificationTypeDictionary getType();
    boolean send(Notification notification) throws SendNotificationException;

    String getSendingResultMessage();
}
