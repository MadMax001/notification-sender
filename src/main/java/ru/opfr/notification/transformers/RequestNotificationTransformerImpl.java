package ru.opfr.notification.transformers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.ApplicationConstants;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.Person;
import ru.opfr.notification.model.dto.Request;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RequestNotificationTransformerImpl implements RequestNotificationTransformer {

    protected final RequestFileTransformerImpl requestFileTransformer;

    public Notification transform(Request request) throws CreationNotificationException {
        checkForNullRequest(request);

        Notification notification = new Notification();
        notification.setContent(request.content);
        notification.setType(NotificationTypeDictionary.of(request.type));
        notification.setPerson(createPerson(request));
        notification.setRemoteId(request.id);
        notification.setTheme(request.theme);
        setFilesIntoNotification(request.files, notification);
        return notification;
    }

    private void checkForNullRequest(Request request) throws CreationNotificationException {
        if (Objects.isNull(request))
            throw new CreationNotificationException(ApplicationConstants.NULL_REQUEST);
    }

    private Person createPerson(Request request) {
        Person person = new Person();
        person.setUser(request.user);
        person.setIp(request.ip);
        person.setEmail(request.email);
        return person;
    }

    private void setFilesIntoNotification(MultipartFile[] files, Notification notification) throws CreationNotificationException {
        if (Objects.nonNull(files)) {
            for (MultipartFile file : files)
                notification.addAttachment(requestFileTransformer.transform(file));
        }
    }


}
