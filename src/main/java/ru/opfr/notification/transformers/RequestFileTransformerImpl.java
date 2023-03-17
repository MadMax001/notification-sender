package ru.opfr.notification.transformers;


import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.model.NotificationAttachment;

import java.io.IOException;

@Component
public class RequestFileTransformerImpl implements RequestFileTransformer{
    @Override
    public NotificationAttachment transform(MultipartFile file) throws IOException {
        NotificationAttachment attachment = new NotificationAttachment();
        attachment.setName(file.getName());
        attachment.setContent(file.getBytes());
        return attachment;
    }
}
