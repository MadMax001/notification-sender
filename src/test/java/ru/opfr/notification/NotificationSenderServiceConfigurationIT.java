package ru.opfr.notification;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.service.SenderService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("datasource_mock_test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NotificationSenderServiceConfigurationIT {
    private final Map<NotificationTypeDictionary, SenderService> senderServiceMap;

    @Test
    void checkOfExistingInSenderServiceMapOfAllServiceSenderServiceByNotificationTypes() {
        for (NotificationTypeDictionary type : NotificationTypeDictionary.values()) {
            assertTrue(senderServiceMap.containsKey(type));
        }
    }
}