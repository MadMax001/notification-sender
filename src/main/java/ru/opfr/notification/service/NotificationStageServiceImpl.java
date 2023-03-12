package ru.opfr.notification.service;

import org.springframework.stereotype.Service;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.NotificationStage;

@Service
public class NotificationStageServiceImpl implements NotificationStageService {
    @Override
    public NotificationStage createdStageByDictionary(NotificationProcessStageDictionary stageDictionary) {
        NotificationStage receivedStage = new NotificationStage();
        receivedStage.setStage(stageDictionary);
        return receivedStage;
    }
}
