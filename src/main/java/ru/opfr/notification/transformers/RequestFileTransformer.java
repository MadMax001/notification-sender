package ru.opfr.notification.transformers;

import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.model.NotificationAttachment;

import java.io.IOException;

public interface RequestFileTransformer {

    NotificationAttachment transform(MultipartFile file) throws IOException;
}
