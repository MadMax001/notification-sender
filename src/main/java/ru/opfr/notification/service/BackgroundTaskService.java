package ru.opfr.notification.service;

import ru.opfr.notification.exception.CreationNotificationException;

public interface BackgroundTaskService {
    void resendIncompleteNotifications() throws CreationNotificationException;
}
