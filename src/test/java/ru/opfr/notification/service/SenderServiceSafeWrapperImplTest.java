package ru.opfr.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;

@ExtendWith(MockitoExtension.class)
class SenderServiceSafeWrapperImplTest {
    private SenderServiceSafeWrapperImpl senderServiceSafeWrapper;


    @Test
    void successfulSending_AndGetTrue() {
        NotificationTypeDictionary type = MESSAGE;
        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = true;
        SenderService service = createSenderService(type, sendOperation, null);
        sendersMap.put(service.getType(), spy(service));

        senderServiceSafeWrapper = new SenderServiceSafeWrapperImpl(sendersMap);
        Notification notification = new Notification();
        notification.setType(type);

        boolean result = senderServiceSafeWrapper.safeSend(notification);

        assertTrue(result);

    }

    @Test
    void unsuccessfulSending_AndGetFalse() {

        NotificationTypeDictionary type = MESSAGE;
        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = false;
        SenderService service = createSenderService(type, sendOperation, null);
        sendersMap.put(service.getType(), spy(service));

        senderServiceSafeWrapper = new SenderServiceSafeWrapperImpl(sendersMap);
        Notification notification = new Notification();
        notification.setType(type);

        boolean result = senderServiceSafeWrapper.safeSend(notification);
        assertFalse(result);

    }

    @Test
    void ThrowSendNotificationExceptionWhileSending_AndGetFalse_AndCheckSetErrorMessageInvocation() throws SendNotificationException {

        NotificationTypeDictionary type = MESSAGE;
        Throwable error = new SendNotificationException("Error while sending");
        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = false;
        SenderService spiedService = spy(createSenderService(type, sendOperation, null));
        doThrow(error).when(spiedService).send(any(Notification.class));
        sendersMap.put(spiedService.getType(), spiedService);

        senderServiceSafeWrapper = new SenderServiceSafeWrapperImpl(sendersMap);
        Notification notification = new Notification();
        notification.setType(MESSAGE);

        boolean result = senderServiceSafeWrapper.safeSend(notification);
        assertFalse(result);
        verify(spiedService, times(1)).setErrorMessage(error);

    }

    @Test
    void ThrowSendNotificationExceptionInAfterSending_AndGetFalse_AndCheckSetErrorMessageInvocation() throws SendNotificationException {
        NotificationTypeDictionary type = MESSAGE;
        Throwable error = new SendNotificationException("Error while sending");
        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = false;
        SenderService spiedService = spy(createSenderService(type, sendOperation, null));
        doThrow(error).when(spiedService).afterSending(any(Notification.class), any(Boolean.class));
        sendersMap.put(spiedService.getType(), spiedService);

        senderServiceSafeWrapper = new SenderServiceSafeWrapperImpl(sendersMap);
        Notification notification = new Notification();
        notification.setType(MESSAGE);

        boolean result = senderServiceSafeWrapper.safeSend(notification);
        assertFalse(result);
        verify(spiedService, times(1)).setErrorMessage(error);
    }

    private SenderService createSenderService(
            NotificationTypeDictionary type,
            boolean sendOperation,
            String resultMessage
    ) {
        return new SenderService() {
            @Override
            public NotificationTypeDictionary getType() {
                return type;
            }

            @Override
            public boolean send(Notification notification) throws SendNotificationException {
                return sendOperation;
            }

            @Override
            public void afterSending(Notification notification, boolean result) throws SendNotificationException {}


            @Override
            public String getSendingResultMessage() {
                return resultMessage;
            }

            @Override
            public void setErrorMessage(Throwable e) {}
        };
    }

}