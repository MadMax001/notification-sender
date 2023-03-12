package ru.opfr.notification.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.NotificationStage;

import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;

class NotificationStageServiceImplTest {
    private NotificationStageService notificationStageService;

    @BeforeEach
    void setUp() {
        notificationStageService = new NotificationStageServiceImpl();
    }

    @Test
    void checkCreatedReceivedStage() {
        NotificationProcessStageDictionary stage = RECEIVED;
        NotificationStage notificationStage = notificationStageService.createdStageByDictionary(stage);
        assertNotNull(notificationStage);
        assertEquals(stage, notificationStage.getStage());
        assertNull(notificationStage.getMessage());
    }

}
