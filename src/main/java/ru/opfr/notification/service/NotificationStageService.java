package ru.opfr.notification.service;

import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.NotificationStage;

public interface NotificationStageService {
    NotificationStage createdStageByDictionary(NotificationProcessStageDictionary stageDictionary);
}
