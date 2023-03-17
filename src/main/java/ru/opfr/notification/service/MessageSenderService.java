package ru.opfr.notification.service;

import org.springframework.stereotype.Service;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;
@Service
public class MessageSenderService implements SenderService {
    @Override
    public NotificationTypeDictionary getType() {
        return MESSAGE;
    }

    @Override
    public boolean send(Notification notification) throws SendNotificationException {
        throw new AssertionError("Not implemented yet");
    }

    @Override
    public void afterSending(Notification notification, boolean result) throws SendNotificationException {

    }

    @Override
    public String getSendingResultMessage() {
        return null;
    }
}
