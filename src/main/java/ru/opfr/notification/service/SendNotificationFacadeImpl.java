package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import ru.opfr.notification.transformers.RequestNotificationTransformerImpl;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static ru.opfr.notification.model.NotificationProcessStageDictionary.*;

@Service
@RequiredArgsConstructor
public class SendNotificationFacadeImpl implements SendNotificationFacade {

    private final NotificationService notificationService;
    private final NotificationStageService notificationStageService;
    private final Map<NotificationTypeDictionary, SenderService> sendersMap;
    private final RequestNotificationTransformerImpl requestNotificationTransformer;

    @Override
    public Response sendNotificationByRequest(Request request) {
        try {
            return sendNotificationWorkflow(request);
        } catch (SendNotificationException | CreationNotificationException | IOException e) {
            return failResponseByThrowable(e, request);
        }
    }

    private Response sendNotificationWorkflow(Request request) throws CreationNotificationException, SendNotificationException, IOException {
        Notification notification = saveNotificationByRequest(request);
        SenderService service = sendersMap.get(notification.getType());
        boolean success = service.send(notification);
        saveNotificationWithNewStageAdding(notification, success ? PROCESSED: FAILED);
        service.afterSending(notification, success);

        return Response.builder()
                .remoteId(request.id)
                .operationId(notification.getId())
                .success(success)
                .message(service.getSendingResultMessage())
                .build();
    }

    private Notification saveNotificationByRequest(Request request) throws CreationNotificationException, IOException {
        Notification notification = requestNotificationTransformer.transform(request);
        saveNotificationWithNewStageAdding(notification, RECEIVED);
        return notification;
    }

    private void saveNotificationWithNewStageAdding(Notification notification, NotificationProcessStageDictionary stage) throws CreationNotificationException {
        notification.addStage(notificationStageService.createdStageByDictionary(stage));
        notificationService.save(notification);

    }

    private Response failResponseByThrowable(Exception e, Request request) {
        return Response.builder()
                .remoteId(Objects.nonNull(request) ? request.id : null)
                .operationId(null)
                .success(false)
                .message(e.getClass().getSimpleName() + ": " + e.getMessage())
                .build();
    }


}
