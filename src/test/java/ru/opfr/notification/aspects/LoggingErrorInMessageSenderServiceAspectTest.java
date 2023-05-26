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
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.messageprocess.AsyncWinMessageService;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.Person;
import ru.opfr.notification.service.SenderServiceSafeWrapper;


import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles({"repo_test"})
class LoggingErrorInMessageSenderServiceAspectTest {
    private final SenderServiceSafeWrapper senderServiceSafeWrapper;

    @MockBean
    private final AsyncWinMessageService messageService;

    @MockBean
    private final LogService logService;

    @Captor
    ArgumentCaptor<String> messageArgumentCaptor;

    @Captor
    ArgumentCaptor<Throwable> throwableArgumentCaptor;

    @Test
    void throwsSendNotificationException_InMessageSenderService_AndLogThisError() throws IOException, InterruptedException {
        Throwable throwable = new IOException("Error in sending process");
        doThrow(throwable).when(messageService).send(any(Notification.class));
        Notification notification = getNotificationByType(MESSAGE);

        senderServiceSafeWrapper.safeSend(notification);

        verify(logService).error(messageArgumentCaptor.capture(), throwableArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        Throwable error = throwableArgumentCaptor.getValue();
        assertTrue(error instanceof SendNotificationException);
        assertTrue(error.getMessage().contains("Error in sending process"));
        assertTrue(message.contains("send"), "Name of method is \"send\"");
        assertTrue(message.contains("MessageSenderService"), "Name of class is \"MessageSenderService\"");
        assertTrue(message.contains("user@server.ru"));
        assertTrue(message.contains("Theme"));
        assertTrue(message.contains("content in message!"));
    }

    @Test
    void throwsRuntimeException_InMessageSenderService_AndLoggingDoesNotInvoking_BecauseOfRTEDoesNotDeclare() throws IOException, InterruptedException {
        Throwable throwable = new RuntimeException("Runtime Error in sending process");
        doThrow(throwable).when(messageService).send(any(Notification.class));
        Notification notification = getNotificationByType(MESSAGE);

        assertThrows(RuntimeException.class, () -> senderServiceSafeWrapper.safeSend(notification));

        verify(logService, never()).error(any(), any());
    }

    @Test
    void withoutErrors_InEmailSenderService_AndLoggingDoesNotInvoking() throws IOException, InterruptedException {
        when(messageService.send(any())).thenReturn(
                CompletableFuture.completedFuture(
                        WinConsoleExecuteResponse.builder()
                                .exitCode(0)
                                .consoleStdOut(Collections.singletonList("All is correct"))
                                .consoleErrOut(Collections.emptyList())
                                .build())
        );
        Notification notification = getNotificationByType(MESSAGE);

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
