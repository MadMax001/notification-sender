package ru.opfr.notification.service;

import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;

public interface SendNotificationFacade {
    Response sendNotificationByRequest(Request request) ;
}
