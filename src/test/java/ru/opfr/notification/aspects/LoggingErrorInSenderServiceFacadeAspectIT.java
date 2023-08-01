package ru.opfr.notification.aspects;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Incubating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.aspects.logging.service.LogService;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.*;
import ru.opfr.notification.model.builders.NotificationTestBuilder;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.service.BackgroundTaskService;
import ru.opfr.notification.service.NotificationService;
import ru.opfr.notification.service.SMTPMailSender;
import ru.opfr.notification.service.SenderServiceFacadeSafeWrapper;

import javax.mail.MessagingException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;
import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles({"tasks_test", "datasource_mock_test"})
class LoggingErrorInSenderServiceFacadeAspectIT {
    @MockBean
    private final NotificationService notificationService;

    private final SenderServiceFacadeSafeWrapper senderServiceFacadeSafeWrapper;
    @Incubating
    private final BackgroundTaskService taskService;

    @MockBean
    private final SMTPMailSender mailSender;

    @MockBean
    private final LogService logService;

    @Captor
    ArgumentCaptor<String> messageArgumentCaptor;

    @Captor
    ArgumentCaptor<Throwable> throwableArgumentCaptor;


    @Test
    void throwsCreationNotificationException_InNotificationSendProcess_AndLogThisError() throws CreationNotificationException {
        Throwable throwable = new CreationNotificationException("Error message");
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .thenThrow(throwable);

        Request request = getRequestByType(MESSAGE);
        senderServiceFacadeSafeWrapper.safeSend(request);

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof CreationNotificationException);
        assertEquals("Error message", error.getMessage());
        assertTrue(message.contains("send"), "Name of method is \"send\"");
        assertTrue(message.contains("SenderServiceFacadeImpl"), "Name of class is \"SenderServiceFacadeImpl\"");
        assertTrue(message.contains("user@server.ru"));
        assertTrue(message.contains("Theme"));
        assertTrue(message.contains("content in message!"));

    }



    @Test
    void throwsRuntimeException_InNotificationSendFacadeProcess_AndLogThisError() throws CreationNotificationException {
        Throwable throwable = new RuntimeException("Runtime Error message");
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .thenThrow(throwable);

        Request request = getRequestByType(MESSAGE);
        assertThrows(RuntimeException.class, () -> senderServiceFacadeSafeWrapper.safeSend(request));

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof RuntimeException);
        assertTrue(error.getMessage().contains("Runtime Error message"));
        assertTrue(message.contains("send"), "Name of method is \"send\"");
        assertTrue(message.contains("SenderServiceFacadeImpl"), "Name of class is \"SenderServiceFacadeImpl\"");
        assertTrue(message.contains("user@server.ru"));
        assertTrue(message.contains("Theme"));
        assertTrue(message.contains("content in message!"));
    }

    @Test
    void throwsSubRuntimeException_InNotificationSendFacadeProcess_AndLogThisError() throws CreationNotificationException {
        Throwable throwable = new ApplicationRuntimeException(new Exception("Application Runtime Error message"));
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .thenThrow(throwable);

        Request request = getRequestByType(MESSAGE);
        assertThrows(ApplicationRuntimeException.class, () -> senderServiceFacadeSafeWrapper.safeSend(request));

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof RuntimeException);
        assertTrue(error.getMessage().contains("Runtime Error message"));
        assertTrue(message.contains("send"), "Name of method is \"send\"");
        assertTrue(message.contains("SenderServiceFacadeImpl"), "Name of class is \"SenderServiceFacadeImpl\"");
        assertTrue(message.contains("user@server.ru"));
        assertTrue(message.contains("Theme"));
        assertTrue(message.contains("content in message!"));
    }

    @Test
    void throwsRuntimeException_InSenderServiceProcess_AndLogThisErrorOnce() throws CreationNotificationException, MessagingException {
        Throwable throwable = new RuntimeException("Runtime Error in sending process");
        doThrow(throwable).when(mailSender).send(any(), any());
        when(notificationService.addStageWithMessageAndSave(any(), any(), any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(2);
            return notification;
        });

        Request request = getRequestByType(EMAIL);
        assertThrows(RuntimeException.class, () -> senderServiceFacadeSafeWrapper.safeSend(request));

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof RuntimeException);
        assertTrue(error.getMessage().contains("Runtime Error in sending process"));
        assertTrue(message.contains("send"), "Name of method is \"send\"");
        assertTrue(message.contains("SenderServiceFacadeImpl"), "Name of class is \"SenderServiceFacadeImpl\"");
        assertTrue(message.contains("user@server.ru"));
        assertTrue(message.contains("Theme"));
        assertTrue(message.contains("content in message!"));
    }

    @Test
    void throwsSubRuntimeException_InSenderServiceProcess_AndLogThisErrorOnce() throws CreationNotificationException, MessagingException {
        Throwable throwable = new ApplicationRuntimeException(new Exception("Application Runtime Error in sending process"));
        doThrow(throwable).when(mailSender).send(any(), any());
        when(notificationService.addStageWithMessageAndSave(any(), any(), any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(2);
            return notification;
        });

        Request request = getRequestByType(EMAIL);
        assertThrows(ApplicationRuntimeException.class, () -> senderServiceFacadeSafeWrapper.safeSend(request));

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof RuntimeException);
        assertTrue(error.getMessage().contains("Runtime Error in sending process"));
        assertTrue(message.contains("send"), "Name of method is \"send\"");
        assertTrue(message.contains("SenderServiceFacadeImpl"), "Name of class is \"SenderServiceFacadeImpl\"");
        assertTrue(message.contains("user@server.ru"));
        assertTrue(message.contains("Theme"));
        assertTrue(message.contains("content in message!"));
    }

    @Test
    void withoutErrors_InSendMethod_AndLoggingDoesNotInvoking() throws CreationNotificationException, MessagingException {

        when(notificationService.addStageWithMessageAndSave(any(), any(), any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(2);
            return notification;
        });
        when(mailSender.send(any(), any())).thenReturn(new SMTPServerAnswer(200, "All right"));
        doNothing().when(notificationService).deleteAllAttachments(any());
        Request request = getRequestByType(EMAIL);
        senderServiceFacadeSafeWrapper.safeSend(request);

        verify(logService, never()).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
    }

    @Test
    void throwsCreationNotificationException_InResendSendProcess_AndLogThisError() throws CreationNotificationException, MessagingException {
        Throwable throwable = new CreationNotificationException("Error message");
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .thenThrow(throwable);

        Notification notification = NotificationTestBuilder.aNotification()
                .withType(EMAIL)
                .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                        .build();
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Collections.singletonList(notification)
        );
        when(mailSender.send(any(), any())).thenReturn(new SMTPServerAnswer(200, "OK"));

        assertThrows(CreationNotificationException.class, taskService::resendIncompleteNotifications);

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof CreationNotificationException);
        assertEquals("Error message", error.getMessage());
        assertTrue(message.contains("resend"), "Name of method is \"resend\"");
        assertTrue(message.contains("SenderServiceFacadeImpl"), "Name of class is \"SenderServiceFacadeImpl\"");
        assertTrue(message.contains(notification.getPerson().getEmail()));
        assertTrue(message.contains(notification.getTheme()));
        assertTrue(message.contains(notification.getContent()));

    }

    @Test
    void throwsSubRuntimeException_InResendSendProcess_AndLogThisError() throws CreationNotificationException, MessagingException {
        Throwable throwable = new ApplicationRuntimeException(new Exception("Error message"));
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .then(invocationOnMock -> invocationOnMock.getArgument(2));

        Notification notification = NotificationTestBuilder.aNotification()
                .withType(EMAIL)
                .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                .build();
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Collections.singletonList(notification)
        );
        doThrow(throwable).when(mailSender).send(any(), any());

        assertThrows(ApplicationRuntimeException.class, taskService::resendIncompleteNotifications);

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof ApplicationRuntimeException);
        assertTrue(error.getMessage().contains("Error message"));
        assertTrue(error.getMessage().contains("java.lang.Exception"));
        assertTrue(message.contains("resend"), "Name of method is \"resend\"");
        assertTrue(message.contains("SenderServiceFacadeImpl"), "Name of class is \"SenderServiceFacadeImpl\"");
        assertTrue(message.contains(notification.getPerson().getEmail()));
        assertTrue(message.contains(notification.getTheme()));
        assertTrue(message.contains(notification.getContent()));

    }

    @Test
    void withoutErrors_InResendMethod_AndLoggingDoesNotInvoking() throws CreationNotificationException, MessagingException {

        when(notificationService.addStageWithMessageAndSave(any(), any(), any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(2);
            return notification;
        });
        when(mailSender.send(any(), any())).thenReturn(new SMTPServerAnswer(200, "All right"));
        Notification notification = NotificationTestBuilder.aNotification()
                .withType(EMAIL)
                .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                .build();
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Collections.singletonList(notification));

        taskService.resendIncompleteNotifications();

        verify(logService, never()).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
    }


    private Request getRequestByType(NotificationTypeDictionary type) {
        Request request = new Request();
        request.type = type.toString();
        request.id = "modelRequestId";
        request.user = "073User";
        request.ip = "10.73.13.14";
        request.email = "user@server.ru";
        request.content = "content in message!";
        request.theme = "Theme";
        return request;
    }

}