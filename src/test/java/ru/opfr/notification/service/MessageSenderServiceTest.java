package ru.opfr.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.messageprocess.AsyncWinMessageService;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;
import ru.opfr.notification.model.Notification;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSenderServiceTest {
    @Mock
    AsyncWinMessageService messageService;

    MessageSenderService senderService;

    @BeforeEach
    void setUp() {
        senderService = new MessageSenderService(messageService);
    }

    @Test
    void successfulSendMessage_AndCheckTrueResult() throws IOException, InterruptedException, SendNotificationException {
        when(messageService.send(any(Notification.class))).thenReturn(
                CompletableFuture.completedFuture(
                        WinConsoleExecuteResponse.builder()
                        .exitCode(0)
                        .consoleStdOut(Collections.singletonList("All is correct"))
                        .consoleErrOut(Collections.emptyList())
                        .build())
        );
        boolean result = senderService.send(new Notification());
        assertTrue(result);
        assertEquals("All is correct", senderService.getSendingResultMessage());
    }

    @Test
    void unsuccessfulSendMessage_AndCheckFalseResult() throws IOException, InterruptedException, SendNotificationException {
        when(messageService.send(any(Notification.class))).thenReturn(
                CompletableFuture.completedFuture(
                        WinConsoleExecuteResponse.builder()
                                .exitCode(1)
                                .consoleStdOut(Collections.emptyList())
                                .consoleErrOut(Collections.singletonList("Error in process"))
                                .build())
        );
        boolean result = senderService.send(new Notification());
        assertFalse(result);
        assertEquals("Error in process", senderService.getSendingResultMessage());


    }

    @Test
    void sendMessage_AndThrowExceptionInSending_AndCatchException() throws IOException, InterruptedException{
        Throwable error = new IOException("Error");
        doThrow(error).when(messageService).send(any(Notification.class));
        assertThrows(SendNotificationException.class, () -> senderService.send(new Notification()));
    }
}