package ru.opfr.notification.service;

import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;


public interface NotificationService {
    Notification save(Notification notification) throws CreationNotificationException;

    void deleteAllAttachments(Notification notification);
}
