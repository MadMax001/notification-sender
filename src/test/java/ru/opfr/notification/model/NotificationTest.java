package ru.opfr.notification.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

class NotificationTest {
    @Test
    void getAndSet() {
        LocalDateTime creationTime = LocalDateTime.now();
        LocalDateTime updateTime = LocalDateTime.now();
        Notification notification = new Notification();

        Person person = new Person();
        person.setIp("10.73.12.13");
        person.setUser("073User");
        person.setEmail("email@server.ru");

        NotificationStage stage1 = new NotificationStage();
        NotificationStage stage2 = new NotificationStage();
        List<NotificationStage> stagesList = Arrays.asList(stage1, stage2);

        notification.setId(5L);
        notification.setContent("content");
        notification.setTheme("Theme");
        notification.setPerson(person);
        notification.setCreated(creationTime);
        notification.setUpdated(updateTime);
        notification.setRemoteId("remote-id");
        notification.setStages(stagesList);
        notification.setType(EMAIL);

        assertEquals(5L, notification.getId());
        assertEquals("content", notification.getContent());
        assertEquals("Theme", notification.getTheme());
        assertEquals("10.73.12.13", notification.getPerson().getIp());
        assertEquals("073User", notification.getPerson().getUser());
        assertEquals("email@server.ru", notification.getPerson().getEmail());
        assertEquals(creationTime, notification.getCreated());
        assertEquals(updateTime, notification.getUpdated());
        assertEquals("remote-id", notification.getRemoteId());
        assertIterableEquals(stagesList, notification.getStages());
        assertEquals(EMAIL, notification.getType());

    }

    @Test
    void addStageAndAssertStagesList() {
        Notification notification = new Notification();
        NotificationStage stage1 = new NotificationStage();
        NotificationStage stage2 = new NotificationStage();
        List<NotificationStage> stagesList = Arrays.asList(stage1, stage2);

        notification.addStage(stage1);
        assertIterableEquals(Collections.singletonList(stage1), notification.getStages());
        for (NotificationStage stage : notification.getStages())
            assertEquals(notification, stage.getNotification());
        notification.addStage(stage2);
        assertIterableEquals(stagesList, notification.getStages());
        for (NotificationStage stage : notification.getStages())
            assertEquals(notification, stage.getNotification());

    }

    @Test
    void addAttachmentAndAssertAttachmentsList() {
        Notification notification = new Notification();
        NotificationAttachment attachment1 = new NotificationAttachment();
        notification.addAttachment(attachment1);
        NotificationAttachment attachment2 = new NotificationAttachment();
        notification.addAttachment(attachment2);
        List<NotificationAttachment> attachments = Arrays.asList(attachment1, attachment2);

        assertIterableEquals(attachments, notification.getAttachments());
        for (NotificationAttachment attachment : notification.getAttachments()) {
            assertEquals(notification, attachment.getNotification());
        }

    }

    @Test
    void clearAttachments() {
        Notification notification = new Notification();
        NotificationAttachment attachment1 = new NotificationAttachment();
        notification.addAttachment(attachment1);
        NotificationAttachment attachment2 = new NotificationAttachment();
        notification.addAttachment(attachment2);

        assertNotNull(attachment1.getNotification());
        assertNotNull(attachment2.getNotification());

        notification.clearAttachments();

        assertIterableEquals(Collections.emptyList(), notification.getAttachments());
        assertNull(attachment1.getNotification());
        assertNull(attachment2.getNotification());
    }

    @Test
    void calculateHashCode() {
        NotificationAttachment attachment1 = new NotificationAttachment();
        attachment1.setName("file1");
        attachment1.setContent("Content number 1".getBytes());
        attachment1.setId(1L);

        NotificationAttachment attachment2 = new NotificationAttachment();
        attachment2.setName("file2");
        attachment2.setContent("Content number 2".getBytes());
        attachment1.setId(2L);

        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.73.12.13");
        modelPerson1.setEmail("user@server.ru");

        Notification notification1 = new Notification();
        notification1.setId(3L);
        notification1.setType(EMAIL);
        notification1.setPerson(modelPerson1);
        notification1.setRemoteId("test-remote-id");
        notification1.setContent("text");
        NotificationStage stage1 = new NotificationStage();
        stage1.setStage(RECEIVED);
        stage1.setId(4L);
        notification1.addStage(stage1);

        notification1.addAttachment(attachment1);
        notification1.addAttachment(attachment2);

        assertNotEquals(0, notification1.hashCode() );
    }

    @Test
    void toStringForEmptyObject() {
        Notification notification = new Notification();
        assertDoesNotThrow(notification::toString);
    }

    @Test
    void toStringWithEmptyPerson() {
        LocalDateTime creationTime = LocalDateTime.now();
        LocalDateTime updateTime = LocalDateTime.now();
        Notification notification = new Notification();

        NotificationStage stage1 = new NotificationStage();
        NotificationStage stage2 = new NotificationStage();
        List<NotificationStage> stagesList = Arrays.asList(stage1, stage2);

        notification.setId(5L);
        notification.setContent("content");
        notification.setTheme("Theme");
        notification.setPerson(null);
        notification.setCreated(creationTime);
        notification.setUpdated(updateTime);
        notification.setRemoteId("remote-id");
        notification.setStages(stagesList);
        notification.setType(EMAIL);

        assertDoesNotThrow(notification::toString);
    }

}