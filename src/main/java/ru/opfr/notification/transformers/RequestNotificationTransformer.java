package ru.opfr.notification.transformers;

import org.springframework.stereotype.Component;
import ru.opfr.notification.ApplicationConstants;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.Person;
import ru.opfr.notification.model.dto.Request;

import java.util.Objects;

@Component
public class RequestNotificationTransformer {

    public Notification transform(Request request) throws CreationNotificationException {
        checkForNullRequest(request);

        Notification notification = new Notification();
        notification.setContent(request.content);
        notification.setType(NotificationTypeDictionary.of(request.type));
        notification.setPerson(createPerson(request));
        notification.setRemoteId(request.id);
        notification.setTheme(request.theme);
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

}
