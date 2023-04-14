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
import ru.opfr.notification.aspects.service.LogService;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.SMTPServerAnswer;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import ru.opfr.notification.service.*;

import javax.mail.MessagingException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

@SpringBootTest()
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles({"repo_test"})
class LoggingInfoInSenderServiceFacadeAspectTest {
    private final SenderServiceFacade senderServiceFacade;

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

    @Test
    void sendNotification_withoutErrors_AndLogsInfoTwice() throws CreationNotificationException, MessagingException {

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
        senderServiceFacade.send(request);

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogInfoMode.INPUT));
        verify(logService).info(messageArgumentCaptorForOutput.capture(), eq(LogInfoMode.OUTPUT));

        String messageInput = messageArgumentCaptorForInput.getValue();
        String messageOutput = messageArgumentCaptorForOutput.getValue();

        assertTrue(messageInput.contains(request.toString()));
        assertTrue(messageOutput.contains(response.toString()));

    }

    @Test
    void sendNotification_withCreationNotificationException_AndLogsInfoOnce() throws CreationNotificationException {
        Throwable throwable = new CreationNotificationException("Error message");
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .thenThrow(throwable);
        Request request = getRequestByType(EMAIL);
        Assertions.assertThrows(CreationNotificationException.class, () -> senderServiceFacade.send(request));

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogInfoMode.INPUT));
        verify(logService, never()).info(messageArgumentCaptorForOutput.capture(), eq(LogInfoMode.OUTPUT));

        String messageInput = messageArgumentCaptorForInput.getValue();

        assertTrue(messageInput.contains(request.toString()));
    }

    @Test
    void sendNotification_withRuntimeException_AndLogsInfoOnce() throws CreationNotificationException {
        Throwable throwable = new RuntimeException("Error message");
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class)))
                .thenThrow(throwable);
        Request request = getRequestByType(EMAIL);
        Assertions.assertThrows(RuntimeException.class, () -> senderServiceFacade.send(request));

        verify(logService).info(messageArgumentCaptorForInput.capture(), eq(LogInfoMode.INPUT));
        verify(logService, never()).info(messageArgumentCaptorForOutput.capture(), eq(LogInfoMode.OUTPUT));

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
