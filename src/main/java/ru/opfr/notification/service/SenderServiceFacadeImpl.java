package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.opfr.notification.aspects.LogError;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import ru.opfr.notification.converters.RequestNotificationConverterImpl;

import java.util.Map;

import static ru.opfr.notification.model.NotificationProcessStageDictionary.*;

@Service
@RequiredArgsConstructor
public class SenderServiceFacadeImpl implements SenderServiceFacade {
    protected final NotificationService notificationService;
    protected final SenderServiceSafeWrapper senderServiceSafeWrapper;
    protected final Map<NotificationTypeDictionary, SenderService> sendersMap;
    protected final RequestNotificationConverterImpl requestNotificationTransformer;

    @Override
    @LogError(values = {CreationNotificationException.class, RuntimeException.class})
    @Transactional(propagation = Propagation.NEVER)
    public Response send(Request request) throws CreationNotificationException {
        Notification notification = saveNotificationByRequest(request);
        boolean success = senderServiceSafeWrapper.safeSend(notification);
        SenderService service = sendersMap.get(notification.getType());
        notificationService.addStageWithMessageAndSave(success ? PROCESSED: FAILED, service.getSendingResultMessage(), notification);

        return Response.builder()
                .remoteId(request.id)
                .operationId(notification.getId())
                .success(success)
                .message(service.getSendingResultMessage())
                .build();

    }

    private Notification saveNotificationByRequest(Request request) throws CreationNotificationException {
        Notification notification = requestNotificationTransformer.convert(request);
        return notificationService.addStageWithMessageAndSave(RECEIVED, null, notification);
    }


}
