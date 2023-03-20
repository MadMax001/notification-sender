package ru.opfr.notification.service;

import org.junit.jupiter.api.Test;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import static org.junit.jupiter.api.Assertions.*;

class AbstractSenderServiceTest {

    @Test
    void sendNotificationWorkflow_AndSuccessfulSending_AndCheckResultAndMessage() {
        AbstractSenderService service = new AbstractSenderService() {
            @Override
            public NotificationTypeDictionary getType() {
                return null;
            }
            @Override
            public boolean send(Notification notification) {
                return true;
            }
            @Override
            public void afterSending(Notification notification, boolean result) {}
        };
        service.resultMessage = "message";
        boolean result = service.sendNotificationWorkflow(new Notification());
        assertTrue(result);
        assertEquals("message", service.getSendingResultMessage());

    }

    @Test
    void sendNotificationWorkflow_AndUnsuccessfulSending_AndCheckResultAndMessage() {
        AbstractSenderService service = new AbstractSenderService() {
            @Override
            public NotificationTypeDictionary getType() {
                return null;
            }
            @Override
            public boolean send(Notification notification)  {
                return false;
            }
            @Override
            public void afterSending(Notification notification, boolean result)  {}
        };
        service.resultMessage = "message";
        boolean result = service.sendNotificationWorkflow(new Notification());
        assertFalse(result);
        assertEquals("message", service.getSendingResultMessage());
    }

    @Test
    void sendNotificationWorkflow_AndThrowInSendingProcess_AndCheckResultAndMessage() {
        AbstractSenderService service = new AbstractSenderService() {
            @Override
            public NotificationTypeDictionary getType() {
                return null;
            }
            @Override
            public boolean send(Notification notification) throws SendNotificationException {
                throw new SendNotificationException("Error!");
            }
            @Override
            public void afterSending(Notification notification, boolean result)  {}
        };
        service.resultMessage = "message";
        boolean result = service.sendNotificationWorkflow(new Notification());
        assertFalse(result);
        assertTrue(service.getSendingResultMessage().contains("Error!"));
        assertTrue(service.getSendingResultMessage().contains(SendNotificationException.class.getSimpleName()));

    }

    @Test
    void getSendingResultMessage() {
        AbstractSenderService service = new AbstractSenderService() {
            @Override
            public NotificationTypeDictionary getType() {
                return null;
            }
            @Override
            public boolean send(Notification notification) {
                return true;
            }
            @Override
            public void afterSending(Notification notification, boolean result)  {}
        };
        service.resultMessage = "message";
        assertEquals("message", service.getSendingResultMessage());
    }
}