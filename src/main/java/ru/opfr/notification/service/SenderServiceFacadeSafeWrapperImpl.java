package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SenderServiceFacadeSafeWrapperImpl implements SenderServiceFacadeSafeWrapper {
    private final SenderServiceFacade senderServiceFacade;
    @Override
    public Response safeSend(Request request) {
        try {
            return senderServiceFacade.send(request);
        } catch (CreationNotificationException e) {
            return failByThrowable(request, e);
        }
    }

    private Response failByThrowable(Request request, Throwable e) {
        return Response.builder()
                .remoteId(Optional.ofNullable(request).map(request1 -> request1.id).orElse(null))
                .operationId(null)
                .success(false)
                .message(e.getClass().getSimpleName() + ": " + e.getMessage())
                .build();
    }
}
