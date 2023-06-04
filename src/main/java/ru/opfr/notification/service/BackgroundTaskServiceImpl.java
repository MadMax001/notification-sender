package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.opfr.notification.aspects.logging.service.LogService;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.tasks.enable", havingValue = "true")
public class BackgroundTaskServiceImpl implements BackgroundTaskService {
    private final NotificationService notificationService;
    private final SenderServiceFacade senderService;
    private final LogService logService;

    @Override
    @Scheduled(initialDelay = 5, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void resendIncompleteNotifications() throws CreationNotificationException {
        List<Notification> incompleteNotificationsList = notificationService.getIncompleteNotifications();
        logService.info(String.format("Finds %d incomplete notification(s)", incompleteNotificationsList.size()));
        for (Notification notification : incompleteNotificationsList) {
            senderService.resend(notification);
        }
    }
}
