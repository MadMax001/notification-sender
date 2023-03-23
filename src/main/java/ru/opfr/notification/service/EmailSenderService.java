package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

@RequiredArgsConstructor
public class EmailSenderService extends AbstractSenderService {
    protected final NotificationService notificationService;


    @Override
    public NotificationTypeDictionary getType() {
        return EMAIL;
    }

    @Override
    public boolean send(Notification notification) throws SendNotificationException {
        throw new AssertionError("Not implemented yet");
    }

    @Override
    public void afterSending(Notification notification, boolean result) throws SendNotificationException {
        try {
            if (result)
                notificationService.deleteAllAttachments(notification);
        }catch (ApplicationRuntimeException e) {
            throw new SendNotificationException(e);
        }
    }
}
