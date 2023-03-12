package ru.opfr.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.service.SenderService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
public class NotificationSenderServiceConfiguration {

    @Bean
    public Map<NotificationTypeDictionary, SenderService> senderServiceMap(List<SenderService> senderServicesList) {
        Map<NotificationTypeDictionary, SenderService> sendersMap = new EnumMap<>(NotificationTypeDictionary.class);
        for (SenderService service : senderServicesList) {
            sendersMap.put(service.getType(), service);
        }
        return sendersMap;
    }
}
