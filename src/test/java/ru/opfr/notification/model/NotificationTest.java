package ru.opfr.notification.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
}