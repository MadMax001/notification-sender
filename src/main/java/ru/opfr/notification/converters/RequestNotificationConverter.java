package ru.opfr.notification.converters;

import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.dto.Request;


public interface RequestNotificationConverter {
    Notification convert(Request request) throws CreationNotificationException;
}
