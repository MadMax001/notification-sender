package ru.opfr.notification.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;

class NotificationStageTest {
    @Test
    void setAndGet() {
        LocalDateTime testCreationTime = LocalDateTime.now();
        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);
        stage.setId(10L);
        stage.setCreated(testCreationTime);
        stage.setMessage("message");

        assertEquals(RECEIVED, stage.getStage());
        assertEquals(10L, stage.getId());
        assertEquals("message", stage.getMessage());
        assertEquals(testCreationTime, stage.getCreated());
    }
}