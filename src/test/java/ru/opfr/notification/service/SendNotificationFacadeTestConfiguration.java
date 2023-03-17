package ru.opfr.notification.service;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.transformers.RequestFileTransformerImpl;
import ru.opfr.notification.transformers.RequestNotificationTransformerImpl;

import java.util.EnumMap;
import java.util.Map;

@TestConfiguration
public class SendNotificationFacadeTestConfiguration {
    @Bean
    public RequestNotificationTransformerImpl requestNotificationTransformer() {
        return new RequestNotificationTransformerImpl(new RequestFileTransformerImpl());
    }

    @Bean
    public Map<NotificationTypeDictionary, SenderService> senderServiceMap() {
        Map<NotificationTypeDictionary, SenderService> sendersMap = new EnumMap<>(NotificationTypeDictionary.class);
        for (NotificationTypeDictionary type : NotificationTypeDictionary.values()) {
            sendersMap.put(type, new SenderService() {
                @Override
                public NotificationTypeDictionary getType() {
                    return type;
                }

                @Override
                public boolean send(Notification notification) throws SendNotificationException {
                    return false;
                }

                @Override
                public void afterSending(Notification notification, boolean result) throws SendNotificationException {}

                @Override
                public String getSendingResultMessage() {
                    return null;
                }
            });
        }
        return sendersMap;
    }

    @Bean
    public NotificationStageService notificationStageService() {
        return new NotificationStageServiceImpl();
    }

    @Bean
    public NotificationService notificationService() {
        return new NotificationServiceImpl(null);
    }

}
