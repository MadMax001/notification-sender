package ru.opfr.notification.service;

import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;

public interface SenderServiceFacade {
    Response send(Request request) throws CreationNotificationException, SendNotificationException;
    Response sendNotificationWorkflow(Request request);
}
