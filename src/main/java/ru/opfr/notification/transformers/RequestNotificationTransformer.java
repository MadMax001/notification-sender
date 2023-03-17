package ru.opfr.notification.transformers;

import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.dto.Request;

import java.io.IOException;

public interface RequestNotificationTransformer {
    Notification transform(Request request) throws CreationNotificationException, IOException;
}
