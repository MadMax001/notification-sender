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
    void checkCreatedReceivedStageWithMessage() {
        NotificationProcessStageDictionary stage = RECEIVED;
        String message = "Test message";
        NotificationStage notificationStage = notificationStageService.createdStageByDictionaryWithMessage(stage, message);
        assertNotNull(notificationStage);
        assertEquals(stage, notificationStage.getStage());
        assertEquals(message, notificationStage.getMessage());
        assertEquals(message, notificationStage.getMessage());
    }

}
