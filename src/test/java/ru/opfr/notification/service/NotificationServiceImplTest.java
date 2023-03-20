package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.*;
import ru.opfr.notification.reporitory.NotificationRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = NotificationServiceImpl.class)

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NotificationServiceImplTest {
    @MockBean
    private final NotificationRepository notificationRepository;
    @MockBean
    private final NotificationStageService notificationStageService;

    private final NotificationService notificationService;

    @Test
    void registerNewRequest_AndCheckResultInInitialObject() throws CreationNotificationException {
        final Long notificationId = 100L;
        final Long stageId = 101L;

        when(notificationRepository.save(any(Notification.class)))
                .then(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    notification.setId(notificationId);
                    notification.setCreated(LocalDateTime.now());

                    notification.getStages().get(0).setId(stageId);
                    notification.getStages().get(0).setCreated(LocalDateTime.now());

                    return notification;
                });

        Notification notification = new Notification();
        notification.setRemoteId("remote-id");
        notification.setContent("Content");
        notification.setType(EMAIL);

        Person person = new Person();
        person.setUser("073User");
        person.setIp("10.73.13.14");
        person.setEmail("user@server.ru");
        notification.setPerson(person);

        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);
        notification.addStage(stage);

        notificationService.save(notification);

        verify(notificationRepository).save(any(Notification.class));

        assertNotNull(notification);
        assertEquals(notificationId, notification.getId());
        assertNotNull(notification.getCreated());

        assertEquals(1, notification.getStages().size());
        assertEquals(notification, notification.getStages().get(0).getNotification());
        assertNotNull(notification.getStages().get(0).getCreated());
        assertEquals(stageId, notification.getStages().get(0).getId());
    }

    @Test
    void registerNewRequest_AndCheckResultInReturnedObject() throws CreationNotificationException {
        final Long notificationId = 100L;
        final Long stageId = 101L;

        when(notificationRepository.save(any(Notification.class)))
                .then(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    notification.setId(notificationId);
                    notification.setCreated(LocalDateTime.now());

                    notification.getStages().get(0).setId(stageId);
                    notification.getStages().get(0).setCreated(LocalDateTime.now());

                    return notification;
                });

        Notification notification = new Notification();
        notification.setRemoteId("remote-id");
        notification.setContent("Content");
        notification.setType(EMAIL);

        Person person = new Person();
        person.setUser("073User");
        person.setIp("10.73.13.14");
        person.setEmail("user@server.ru");
        notification.setPerson(person);

        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);
        notification.addStage(stage);

        Notification newNotification = notificationService.save(notification);

        verify(notificationRepository).save(any(Notification.class));

        assertNotNull(newNotification);
        assertEquals(notificationId, newNotification.getId());
        assertNotNull(newNotification.getCreated());

        assertEquals(1, newNotification.getStages().size());
        assertEquals(newNotification, newNotification.getStages().get(0).getNotification());
        assertNotNull(newNotification.getStages().get(0).getCreated());
        assertEquals(stageId, newNotification.getStages().get(0).getId());
    }

    @Test
    void deleteAllAttachmentsByNotification() {
        when(notificationRepository.save(any(Notification.class)))
                .then(invocation ->  invocation.getArgument(0));

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setType(EMAIL);

        NotificationAttachment attachment1 = new NotificationAttachment();
        attachment1.setName("file1");
        attachment1.setContent("Content number 1".getBytes());
        attachment1.setId(20L);

        NotificationAttachment attachment2 = new NotificationAttachment();
        attachment2.setName("file2");
        attachment2.setContent("Content number 2".getBytes());
        attachment2.setId(21L);

        notification.addAttachment(attachment1);
        notification.addAttachment(attachment2);

        notificationService.deleteAllAttachments(notification);

        assertEquals(0, notification.getAttachments().size());
        assertNull(attachment1.getNotification());
        assertNull(attachment2.getNotification());


    }

    @Test
    void addNewStageAndSave() throws CreationNotificationException {
        final Long notificationId = 100L;
        final Long stageId = 101L;

        when(notificationRepository.save(any(Notification.class)))
                .then(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    notification.setId(notificationId);
                    notification.setCreated(LocalDateTime.now());

                    notification.getStages().get(0).setId(stageId);
                    notification.getStages().get(0).setCreated(LocalDateTime.now());

                    return notification;
                });
        when(notificationStageService.createdStageByDictionary(any(NotificationProcessStageDictionary.class)))
                .then(invocatopn -> {
                    NotificationStage stage = new NotificationStage();
                    stage.setStage(invocatopn.getArgument(0));
                    return stage;
                });

        Notification notification = new Notification();
        notification.setRemoteId("remote-id");
        notification.setContent("Content");
        notification.setType(EMAIL);

        Person person = new Person();
        person.setUser("073User");
        person.setIp("10.73.13.14");
        person.setEmail("user@server.ru");
        notification.setPerson(person);

        notificationService.addStageAndSave(RECEIVED, notification);

        verify(notificationRepository).save(any(Notification.class));
        verify(notificationStageService).createdStageByDictionary(any(NotificationProcessStageDictionary.class));

        assertNotNull(notification);
        assertEquals(notificationId, notification.getId());
        assertNotNull(notification.getCreated());

        assertEquals(1, notification.getStages().size());
        assertEquals(notification, notification.getStages().get(0).getNotification());
        assertNotNull(notification.getStages().get(0).getCreated());
        assertEquals(stageId, notification.getStages().get(0).getId());
    }
}