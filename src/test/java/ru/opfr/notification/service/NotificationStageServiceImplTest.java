package ru.opfr.notification.service;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.NotificationStage;

import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = NotificationStageServiceImpl.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NotificationStageServiceImplTest {
    private final NotificationStageService notificationStageService;

    @Test
    void checkCreatedReceivedStage() {
        NotificationProcessStageDictionary stage = RECEIVED;
        NotificationStage notificationStage = notificationStageService.createdStageByDictionary(stage);
        assertNotNull(notificationStage);
        assertEquals(stage, notificationStage.getStage());
        assertNull(notificationStage.getMessage());
    }

}
