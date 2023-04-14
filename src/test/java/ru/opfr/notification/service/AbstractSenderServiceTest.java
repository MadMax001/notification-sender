package ru.opfr.notification.service;

import org.junit.jupiter.api.Test;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import static org.junit.jupiter.api.Assertions.*;

class AbstractSenderServiceTest {

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

    @Test
    void setErrorMessage() {
        Throwable error = new Exception("Test");
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
        service.setErrorMessage(error);
        assertTrue(service.resultMessage.contains(error.getClass().getSimpleName()));
        assertTrue(service.resultMessage.contains("Test"));
    }
}