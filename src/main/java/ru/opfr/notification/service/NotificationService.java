package ru.opfr.notification.service;

import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;

import javax.validation.constraints.NotNull;
import java.util.List;


public interface NotificationService {
    Notification save(@NotNull Notification notification) throws CreationNotificationException;
    Notification addStageWithMessageAndSave(NotificationProcessStageDictionary stage, String message,
                                            @NotNull Notification notification) throws CreationNotificationException;
    void deleteAllAttachments(@NotNull Notification notification);
    List<Notification> getIncompleteNotifications();
}
