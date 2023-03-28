package ru.opfr.notification.converters;


import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.NotificationAttachment;

import java.io.IOException;

@Component
public class RequestFileConverterImpl implements RequestFileConverter {
    @Override
    public NotificationAttachment convert(MultipartFile file) throws CreationNotificationException {
        NotificationAttachment attachment = new NotificationAttachment();
        attachment.setName(file.getName());
        try {
            attachment.setContent(file.getBytes());
        } catch (IOException e) {
            throw new CreationNotificationException(e);
        }
        return attachment;
    }
}
