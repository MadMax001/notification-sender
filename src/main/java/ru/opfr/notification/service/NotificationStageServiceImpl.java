package ru.opfr.notification.service;

import org.springframework.stereotype.Service;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.NotificationStage;

@Service
public class NotificationStageServiceImpl implements NotificationStageService {
    @Override
    public NotificationStage createdStageByDictionaryWithMessage(NotificationProcessStageDictionary stageDictionary, String message) {
        NotificationStage newStage = new NotificationStage();
        newStage.setStage(stageDictionary);
        newStage.setMessage(message);
        return newStage;
    }
}
