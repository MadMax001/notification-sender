package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.reporitory.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.FAILED;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;


@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    protected final NotificationRepository notificationRepository;
    protected final NotificationStageService notificationStageService;

    public Notification save(Notification notification) throws CreationNotificationException {
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification addStageWithMessageAndSave(NotificationProcessStageDictionary stage, String message, Notification notification) throws CreationNotificationException {
        notification.addStage(notificationStageService.createdStageByDictionaryWithMessage(stage, message));
        return save(notification);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllAttachments(Notification notification) {
        notification.clearAttachments();
        try {
            save(notification);
        } catch (CreationNotificationException e) {
            throw new ApplicationRuntimeException(e);
        }

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<Notification> getIncompleteNotifications() {
        List<Notification> failedNotificationsList =
                notificationRepository.findAllByLatestStage(FAILED);
        List<Notification> incompleteLongAgoUpdatedNotificationsList =
                notificationRepository.findAllByLatestStageAndCreatedBeforeAndUpdatedIsNull(
                        RECEIVED, LocalDateTime.now().minus(5, MINUTES)
                );
        return Stream.concat(failedNotificationsList.stream(), incompleteLongAgoUpdatedNotificationsList.stream())
                .collect(Collectors.toList());
    }

}
