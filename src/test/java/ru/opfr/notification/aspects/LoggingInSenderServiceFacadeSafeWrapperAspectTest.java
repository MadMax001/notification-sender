package ru.opfr.notification.aspects;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.aspects.logging.LogType;
import ru.opfr.notification.aspects.logging.service.LogService;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.SMTPServerAnswer;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import ru.opfr.notification.service.NotificationService;
import ru.opfr.notification.service.SMTPMailSender;
import ru.opfr.notification.service.SenderServiceFacadeSafeWrapper;

import javax.mail.MessagingException;
import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles({"repo_test"})
class LoggingInSenderServiceFacadeSafeWrapperAspectTest {
    private final SenderServiceFacadeSafeWrapper senderServiceFacadeSafeWrapper;

    @MockBean
    private final SMTPMailSender mailSender;

    @MockBean
    private final NotificationService notificationService;

    @MockBean
    private final LogService logService;

    @Captor
    ArgumentCaptor<String> messageArgumentCaptorForInput;

    @Captor
    ArgumentCaptor<String> messageArgumentCaptorForOutput;

    @Captor
    ArgumentCaptor<? extends Throwable> errorArgumentCaptor;

    @Test
    void safeSendNotification_withoutErrors_AndLogsInfoTwice() throws CreationNotificationException, MessagingException {
        when(notificationService.addStageWithMessageAndSave(any(), any(), any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(2);
            notification.setId(50L);
            return notification;
        });

        SMTPServerAnswer aSMTPServerAnswer = new SMTPServerAnswer(200, "All right");
        when(mailSender.send(any(), any())).thenReturn(aSMTPServerAnswer);
        doNothing().when(notificationService).deleteAllAttachments(any());

        Request request = getRequestByType(EMAIL);
        Response response = Response.builder()
                .success(true).operationId(50L).remoteId("modelRequestId").message(aSMTPServerAnswer.toString())
                .build();

        senderServiceFacadeSafeWrapper.safeSend(request);

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogType.INPUT));
        verify(logService).info(messageArgumentCaptorForOutput.capture(), eq(LogType.OUTPUT));
        verify(logService, never()).error(any(), any());

        String messageInput = messageArgumentCaptorForInput.getValue();
        String messageOutput = messageArgumentCaptorForOutput.getValue();

        assertTrue(messageInput.contains(request.toString()));
        assertTrue(messageOutput.contains(response.toString()));
    }

    @Test
    void safeSendNotification_withCreationNotificationExceptionInFirstStage_AndLogsInfoTwice_AndLogsErrorOne() throws CreationNotificationException{
        Throwable throwable = new CreationNotificationException("Error message in creating");
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .thenThrow(throwable);

        Request request = getRequestByType(EMAIL);
        Response response = Response.builder()
                .success(false).operationId(null).remoteId("modelRequestId").message(throwable.toString())
                .build();

        senderServiceFacadeSafeWrapper.safeSend(request);

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogType.INPUT));
        verify(logService).info(messageArgumentCaptorForOutput.capture(), eq(LogType.OUTPUT));
        verify(logService).error(any(), eq(throwable));

        String messageInput = messageArgumentCaptorForInput.getValue();
        String messageOutput = messageArgumentCaptorForOutput.getValue();

        assertTrue(messageInput.contains(request.toString()));
        assertTrue(messageOutput.contains(response.toString()));
    }

    @Test
    void safeSendNotification_withCreationNotificationExceptionInLastStage_AndLogsInfoTwice_AndLogsErrorOne() throws CreationNotificationException, MessagingException {
        Throwable throwable = new CreationNotificationException("Error message in creating");
        when(notificationService.addStageWithMessageAndSave(eq(PROCESSED), any(), any(Notification.class)))
                .thenThrow(throwable);
        when(notificationService.addStageWithMessageAndSave(eq(RECEIVED), any(), any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(2);
            notification.setId(50L);
            return notification;
        });
        SMTPServerAnswer aSMTPServerAnswer = new SMTPServerAnswer(200, "All right");
        when(mailSender.send(any(), any())).thenReturn(aSMTPServerAnswer);
        doNothing().when(notificationService).deleteAllAttachments(any());

        Request request = getRequestByType(EMAIL);
        Response response = Response.builder()
                .success(false).operationId(null).remoteId("modelRequestId").message(throwable.toString())              //todo на последнем шаге уже есть notificationID
                .build();

        senderServiceFacadeSafeWrapper.safeSend(request);

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogType.INPUT));
        verify(logService).info(messageArgumentCaptorForOutput.capture(), eq(LogType.OUTPUT));
        verify(logService).error(any(), eq(throwable));

        String messageInput = messageArgumentCaptorForInput.getValue();
        String messageOutput = messageArgumentCaptorForOutput.getValue();

        assertTrue(messageInput.contains(request.toString()));
        assertTrue(messageOutput.contains(response.toString()));
    }


    @Test
    void safeSendNotification_withSendNotificationException_AndLogsInfoTwice_AndLogsErrorOne() throws CreationNotificationException, MessagingException {
        String exceptionMessage = "Error message in messaging";
        Throwable throwable = new MessagingException(exceptionMessage);
        Throwable wrapperThrowable = new SendNotificationException(throwable.toString());
        when(notificationService.addStageWithMessageAndSave(any(), any(), any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(2);
            notification.setId(50L);
            return notification;
        });
        doThrow(throwable).when(mailSender).send(any(), any());
        doNothing().when(notificationService).deleteAllAttachments(any());

        Request request = getRequestByType(EMAIL);
        Response response = Response.builder()
                .success(false).operationId(50L).remoteId("modelRequestId").message(wrapperThrowable.toString())
                .build();

        senderServiceFacadeSafeWrapper.safeSend(request);

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogType.INPUT));
        verify(logService).info(messageArgumentCaptorForOutput.capture(), eq(LogType.OUTPUT));
        verify(logService).error(any(), errorArgumentCaptor.capture());

        String messageInput = messageArgumentCaptorForInput.getValue();
        String messageOutput = messageArgumentCaptorForOutput.getValue();
        Throwable error = errorArgumentCaptor.getValue();

        assertTrue(messageInput.contains(request.toString()));
        assertTrue(messageOutput.contains(response.toString()));
        assertTrue(error instanceof SendNotificationException);
        assertEquals(throwable.toString(), error.getMessage());
    }

    @Test
    void safeSendNotification_withSendNotificationExceptionInAfterSending_AndLogsInfoTwice_AndLogsErrorOne() throws CreationNotificationException, MessagingException {
        String exceptionMessage = "Error message in after sending";
        Throwable throwable = new PersistenceException(exceptionMessage);
        Throwable wrapperThrowable = new SendNotificationException(throwable.toString());
        when(notificationService.addStageWithMessageAndSave(any(), any(), any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(2);
            notification.setId(50L);
            return notification;
        });
        doThrow(throwable).when(notificationService).deleteAllAttachments(any(Notification.class));
        SMTPServerAnswer aSMTPServerAnswer = new SMTPServerAnswer(200, "All right");
        when(mailSender.send(any(), any())).thenReturn(aSMTPServerAnswer);

        Request request = getRequestByType(EMAIL);
        Response response = Response.builder()
                .success(false).operationId(50L).remoteId("modelRequestId").message(wrapperThrowable.toString())
                .build();

        senderServiceFacadeSafeWrapper.safeSend(request);

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogType.INPUT));
        verify(logService).info(messageArgumentCaptorForOutput.capture(), eq(LogType.OUTPUT));
        verify(logService).error(any(), errorArgumentCaptor.capture());

        String messageInput = messageArgumentCaptorForInput.getValue();
        String messageOutput = messageArgumentCaptorForOutput.getValue();
        Throwable error = errorArgumentCaptor.getValue();

        assertTrue(messageInput.contains(request.toString()));
        assertTrue(messageOutput.contains(response.toString()));
        assertTrue(error instanceof SendNotificationException);
        assertEquals(throwable.toString(), error.getMessage());
    }

    @Test
    void sendNotification_withRuntimeException_AndLogsInfoOnce_AndLogsErrorOnce() throws CreationNotificationException {
        Throwable throwable = new RuntimeException("Runtime Error message");
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .thenThrow(throwable);
        Request request = getRequestByType(EMAIL);
        Assertions.assertThrows(RuntimeException.class, () -> senderServiceFacadeSafeWrapper.safeSend(request));

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogType.INPUT));
        verify(logService).error(any(), eq(throwable));
        verify(logService, never()).info(messageArgumentCaptorForOutput.capture(), eq(LogType.OUTPUT));

        String messageInput = messageArgumentCaptorForInput.getValue();

        assertTrue(messageInput.contains(request.toString()));

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