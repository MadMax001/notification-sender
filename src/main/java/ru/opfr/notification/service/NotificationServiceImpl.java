package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.reporitory.NotificationRepository;


@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
    private final NotificationRepository notificationRepository;
    private final NotificationStageService notificationStageService;

    public Notification save(Notification notification) throws CreationNotificationException {
        return notificationRepository.save(notification);
    }

    @Override
    public Notification addStageAndSave(NotificationProcessStageDictionary stage, Notification notification) throws CreationNotificationException {
        notification.addStage(notificationStageService.createdStageByDictionary(stage));
        return save(notification);
    }

    @Override
    public void deleteAllAttachments(Notification notification) {
        notification.clearAttachments();
        try {
            save(notification);
        } catch (CreationNotificationException e) {
            throw new ApplicationRuntimeException(e);
        }

    }

}
