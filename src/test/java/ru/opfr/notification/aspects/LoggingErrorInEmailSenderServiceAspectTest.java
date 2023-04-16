package ru.opfr.notification.aspects;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.aspects.logging.service.LogService;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.Person;
import ru.opfr.notification.model.SMTPServerAnswer;
import ru.opfr.notification.service.*;

import javax.mail.MessagingException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

@SpringBootTest()
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles({"repo_test"})
class LoggingErrorInEmailSenderServiceAspectTest {
    private final SenderServiceSafeWrapper senderServiceSafeWrapper;

    @MockBean
    private final SMTPMailSender mailSender;

    @MockBean
    private final NotificationService notificationService;
    @MockBean
    private final LogService logService;

    @Captor
    ArgumentCaptor<String> messageArgumentCaptor;

    @Captor
    ArgumentCaptor<Throwable> throwableArgumentCaptor;

    @Test
    void throwsSendNotificationException_InEmailSenderService_AndLogThisError() throws MessagingException {
        Throwable throwable = new MessagingException("Error in sending process");
        doThrow(throwable).when(mailSender).send(any(), any());
        Notification notification = getNotificationByType(EMAIL);

        senderServiceSafeWrapper.safeSend(notification);

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof SendNotificationException);
        assertTrue(error.getMessage().contains("Error in sending process"));
        assertTrue(message.contains("send"), "Name of method is \"send\"");
        assertTrue(message.contains("EmailSenderService"), "Name of class is \"EmailSenderService\"");
        assertTrue(message.contains("user@server.ru"));
        assertTrue(message.contains("Theme"));
        assertTrue(message.contains("content in message!"));
    }

    @Test
    void throwsRuntimeException_InEmailSenderService_AndLoggingDoesNotInvoking_BecauseOfRTEDoesNotDeclare() throws MessagingException {
        Throwable throwable = new RuntimeException("Runtime Error in sending process");
        doThrow(throwable).when(mailSender).send(any(), any());
        Notification notification = getNotificationByType(EMAIL);

        assertThrows(RuntimeException.class, () -> senderServiceSafeWrapper.safeSend(notification));

        verify(logService, never()).error(any(), any());
    }

    @Test
    void throwsSubRuntimeException_InEmailSenderService_AndLoggingDoesNotInvoking_BecauseOfRTEDoesNotDeclare() throws MessagingException {
        Throwable throwable = new ApplicationRuntimeException(new Exception("Application runtime Error in sending process"));
        doThrow(throwable).when(mailSender).send(any(), any());
        Notification notification = getNotificationByType(EMAIL);

        assertThrows(ApplicationRuntimeException.class, () -> senderServiceSafeWrapper.safeSend(notification));

        verify(logService, never()).error(any(), any());
    }

    @Test
    void withoutErrors_InEmailSenderService_AndLoggingDoesNotInvoking() throws MessagingException {
        when(mailSender.send(any(), any())).thenReturn(new SMTPServerAnswer(200, ""));
        doNothing().when(notificationService).deleteAllAttachments(any(Notification.class));
        Notification notification = getNotificationByType(EMAIL);

        senderServiceSafeWrapper.safeSend(notification);

        verify(logService, never()).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
    }

    private Notification getNotificationByType(NotificationTypeDictionary type) {
        Person person = new Person();
        person.setEmail("user@server.ru");
        person.setIp("10.73.13.14");
        person.setUser("073User");

        Notification notification = new Notification();
        notification.setType(type);
        notification.setPerson(person);
        notification.setContent("content in message!");
        notification.setTheme("Theme");
        notification.setRemoteId("100");

        return notification;
    }
}
