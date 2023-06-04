package ru.opfr.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.opfr.notification.aspects.logging.service.LogService;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.builders.NotificationTestBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.FAILED;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;

@ExtendWith(MockitoExtension.class)
class BackgroundTaskServiceImplTest {
    @Mock
    private NotificationService notificationService;
    @Mock
    private SenderServiceFacade senderService;
    @Mock
    private LogService logService;

    @Captor
    ArgumentCaptor<Notification> notificationCaptor;

    BackgroundTaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new BackgroundTaskServiceImpl(notificationService, senderService, logService);
    }

    @Test
    void resendIncompleteNotificationSuccessfully() throws CreationNotificationException {
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Arrays.asList(
                        NotificationTestBuilder.aNotification()
                                .withTheme("Тестовая тема1")
                                .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                                .build(),
                        NotificationTestBuilder.aNotification()
                                .withTheme("Тестовая тема2")
                                .withStages(new NotificationProcessStageDictionary[]{RECEIVED, FAILED})
                                .build())
        );
        doNothing().when(senderService).resend(any(Notification.class));
        doNothing().when(logService).info(any(String.class));

        taskService.resendIncompleteNotifications();

        verify(logService).info(any(String.class));
        verify(senderService, times(2)).resend(notificationCaptor.capture());

        List<Notification> notificationList = notificationCaptor.getAllValues();
        assertEquals(2, notificationList.size());
        assertEquals("Тестовая тема1", notificationList.get(0).getTheme());
        assertEquals(1, notificationList.get(0).getStages().size());
        assertEquals("Тестовая тема2", notificationList.get(1).getTheme());
        assertEquals(2, notificationList.get(1).getStages().size());

    }

    @Test
    void tryResendZeroLengthIncompleteNotification() throws CreationNotificationException {
        when(notificationService.getIncompleteNotifications()).thenReturn(Collections.emptyList());
        doNothing().when(logService).info(any(String.class));

        taskService.resendIncompleteNotifications();

        verify(logService).info(any(String.class));
        verify(senderService, never()).resend(any(Notification.class));
    }

    @Test
    void resendIncompleteNotification_AndThrowExceptionInResendProcessOfSecondNotification_AndCheckInvocations() throws CreationNotificationException {
        Notification notification1 = NotificationTestBuilder.aNotification()
                .withTheme("Тестовая тема1")
                .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                .build();
        Notification notification2 = NotificationTestBuilder.aNotification()
                .withTheme("Тестовая тема2")
                .withStages(new NotificationProcessStageDictionary[]{RECEIVED, FAILED})
                .build();
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Arrays.asList(notification1, notification2)
        );
        Throwable exception = new CreationNotificationException("Error in resend method");
        doNothing().when(senderService).resend(notification1);
        doThrow(exception).when(senderService).resend(notification2);

        doNothing().when(logService).info(any(String.class));

        assertThrows(CreationNotificationException.class, () -> taskService.resendIncompleteNotifications());

        verify(logService).info(any(String.class));
        verify(senderService, times(2)).resend(notificationCaptor.capture());
        List<Notification> notificationList = notificationCaptor.getAllValues();
        assertEquals(2, notificationList.size());
        assertEquals("Тестовая тема1", notificationList.get(0).getTheme());
        assertEquals(1, notificationList.get(0).getStages().size());
    }
}