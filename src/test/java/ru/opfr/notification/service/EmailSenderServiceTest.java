package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EmailSenderService.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EmailSenderServiceTest {
    @MockBean
    private final NotificationService notificationService;

    final EmailSenderService emailSenderService;


    @Test
    void checkTYpe() {
        assertEquals(EMAIL, emailSenderService.getType());
    }

    @Test
    void afterSendingRemovesAllAttachments_IfSuccessfulSend() throws SendNotificationException {
        boolean success = true;

        Notification notification = new Notification();
        notification.setRemoteId("remote-id");
        notification.setContent("Content");
        notification.setType(EMAIL);
        notification.setPerson(new Person());
        notification.addStage(new NotificationStage());
        notification.addAttachment(new NotificationAttachment());

        emailSenderService.afterSending(notification, success);
        verify(notificationService).deleteAllAttachments(notification);

    }

    @Test
    void afterSendingDoesNotRemoveAttachments_IfUnSuccessfulSend() throws SendNotificationException {
        boolean success = false;

        Notification notification = new Notification();
        notification.setRemoteId("remote-id");
        notification.setContent("Content");
        notification.setType(EMAIL);
        notification.setPerson(new Person());
        notification.addStage(new NotificationStage());
        notification.addAttachment(new NotificationAttachment());

        emailSenderService.afterSending(notification, success);
        verify(notificationService, never()).deleteAllAttachments(notification);
    }

    @Test
    void afterSendingThrowExceptionInPersist_ThenTrowSendNotificationException() {
        doThrow(ApplicationRuntimeException.class).when(notificationService).deleteAllAttachments(any(Notification.class));
        boolean success = true;

        Notification notification = new Notification();
        notification.setRemoteId("remote-id");
        notification.setContent("Content");
        notification.setType(EMAIL);
        notification.setPerson(new Person());
        notification.addStage(new NotificationStage());
        notification.addAttachment(new NotificationAttachment());

        assertThrows(SendNotificationException.class, () -> emailSenderService.afterSending(notification, success));
    }

    @Test
    void afterSendingThrowNotApplicationException_ThenTrowException()  {
        doThrow(RuntimeException.class).when(notificationService).deleteAllAttachments(any(Notification.class));
        boolean success = true;

        Notification notification = new Notification();
        notification.setRemoteId("remote-id");
        notification.setContent("Content");
        notification.setType(EMAIL);
        notification.setPerson(new Person());
        notification.addStage(new NotificationStage());
        notification.addAttachment(new NotificationAttachment());

        assertThrows(RuntimeException.class, () -> emailSenderService.afterSending(notification, success));
    }

}