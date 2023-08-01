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
import ru.opfr.notification.aspects.logging.LogType;
import ru.opfr.notification.aspects.logging.service.LogService;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.SMTPServerAnswer;
import ru.opfr.notification.model.builders.NotificationTestBuilder;
import ru.opfr.notification.service.BackgroundTaskService;
import ru.opfr.notification.service.NotificationService;
import ru.opfr.notification.service.SMTPMailSender;
import javax.mail.MessagingException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.opfr.notification.aspects.logging.LogType.INPUT;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles({"tasks_test", "datasource_mock_test"})
class LoggingInfoInSenderServiceFacadeAspectTest {
    @MockBean
    private final NotificationService notificationService;

    @Incubating
    private final BackgroundTaskService taskService;

    @MockBean
    private final SMTPMailSender mailSender;

    @MockBean
    private final LogService logService;

    @Captor
    ArgumentCaptor<String> messageArgumentCaptor;

    @Captor
    ArgumentCaptor<LogType> logTypeArgumentCaptor;

    @Test
    void resendNotification_AndCheckForLogging() throws CreationNotificationException, MessagingException {
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
        verify(logService).info(messageArgumentCaptor.capture(), logTypeArgumentCaptor.capture());
        String message = messageArgumentCaptor.getValue();
        LogType logType = logTypeArgumentCaptor.getValue();
        assertEquals(INPUT, logType);
        assertTrue(message.contains("stages=" + notification.getStages().size()));
        assertTrue(message.contains(notification.getPerson().getEmail()));
        assertTrue(message.contains(notification.getTheme()));
        assertTrue(message.contains(notification.getContent()));
    }
}
