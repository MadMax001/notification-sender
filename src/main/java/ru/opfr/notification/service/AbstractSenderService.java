package ru.opfr.notification.service;

import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;

public abstract class AbstractSenderService implements SenderService{
    protected String resultMessage;

    @Override
    public boolean sendNotificationWorkflow(Notification notification) {
        try {
            boolean result = send(notification);
            afterSending(notification, result);
            return result;
        } catch (SendNotificationException e) {
            resultMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            return false;
        }
    }

    @Override
    public void afterSending(Notification notification, boolean result) throws SendNotificationException {}

    @Override
    public String getSendingResultMessage() {
        return resultMessage;
    }
}
