package ru.opfr.notification.service;

import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;

public abstract class AbstractSenderService implements SenderService{
    protected String resultMessage;

    @Override
    public void afterSending(Notification notification, boolean result) throws SendNotificationException {}

    @Override
    public String getSendingResultMessage() {
        return resultMessage;
    }

    @Override
    public void setErrorMessage(Throwable e) {
        resultMessage = e.toString();
    }
}
