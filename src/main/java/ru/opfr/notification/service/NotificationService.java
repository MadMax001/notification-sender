package ru.opfr.notification.service;

import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;


public interface NotificationService {
    Notification save(Notification notification) throws CreationNotificationException;
    Notification addStageAndSave(NotificationProcessStageDictionary stage, Notification notification) throws CreationNotificationException;

    void deleteAllAttachments(Notification notification);
}
