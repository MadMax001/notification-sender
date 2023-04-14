package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SenderServiceSafeWrapperImpl implements SenderServiceSafeWrapper {
    protected final Map<NotificationTypeDictionary, SenderService> sendersMap;

    @Override
    public boolean safeSend(Notification notification) {
        SenderService service = sendersMap.get(notification.getType());
        try {
            boolean result = service.send(notification);
            service.afterSending(notification, result);
            return result;
        } catch (SendNotificationException e) {
            service.setErrorMessage(e);
            return false;
        }
    }

}
