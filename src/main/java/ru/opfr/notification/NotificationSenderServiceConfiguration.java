package ru.opfr.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.service.SenderService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class NotificationSenderServiceConfiguration {

    @Bean
    public Map<NotificationTypeDictionary, SenderService> senderServiceMap(List<SenderService> senderServicesList) {
        Map<NotificationTypeDictionary, SenderService> sendersMap = new EnumMap<>(NotificationTypeDictionary.class);
        for (SenderService service : senderServicesList) {
            sendersMap.put(service.getType(), service);
        }
        return sendersMap;
    }

    @Bean
    public Executor taskSenderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("MessageSender-");
        executor.initialize();
        return executor;
    }
}
