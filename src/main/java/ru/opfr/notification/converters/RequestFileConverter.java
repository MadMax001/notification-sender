package ru.opfr.notification.converters;

import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.NotificationAttachment;


public interface RequestFileConverter {

    NotificationAttachment convert(MultipartFile file) throws CreationNotificationException;
}
