package ru.opfr.notification.service;

import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;

public interface SenderServiceFacadeSafeWrapper {
    Response safeSend(Request request);
}
