package ru.opfr.notification.transformers;

import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.NotificationAttachment;


public interface RequestFileTransformer {

    NotificationAttachment transform(MultipartFile file) throws CreationNotificationException;
}
