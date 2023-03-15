package ru.opfr.notification.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.dto.Request;

import static org.junit.jupiter.api.Assertions.*;

class RequestNotificationTransformerTest {
    private RequestNotificationTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new RequestNotificationTransformer();
    }

    @Test
    void createEntityFromDTO_WithAllNonNullFields() throws CreationNotificationException {
        Request dto = new Request();
        dto.type = NotificationTypeDictionary.EMAIL.toString();
        dto.id = "remote-id";
        dto.user = "073User";
        dto.ip = "10.12.13.14";
        dto.email = "user@server.ru";
        dto.content = "Content";
        Notification notification = transformer.transform(dto);

        assertNotNull(notification);
        assertNull(notification.getId());
        assertEquals(dto.id, notification.getRemoteId());
        assertEquals(dto.user, notification.getPerson().getUser());
        assertEquals(dto.ip, notification.getPerson().getIp());
        assertEquals(dto.email, notification.getPerson().getEmail());
        assertEquals(dto.content, notification.getMessage());
        assertEquals(NotificationTypeDictionary.of(dto.type), notification.getType());
        assertEquals(0, notification.getStages().size());
        assertNull(notification.getCreated());
        assertNull(notification.getUpdated());
    }

    @Test
    void createEntityFromNullDTO_andThrowException() {
        assertThrows(CreationNotificationException.class, () -> transformer.transform(null));
    }

    @Test
    void createEntityWithWrongType_andGetNotificationWithNullType() throws CreationNotificationException {
        Request dto = new Request();
        dto.type = "aaa";
        dto.id = "remote-id";
        dto.user = "073User";
        dto.ip = "10.12.13.14";
        dto.email = "user@server.ru";
        dto.content = "Content";

        Notification notification = transformer.transform(dto);
        assertNull(notification.getType());
    }

    @Test
    void createEntityWithTypeToLowerCase_andGetNotificationWithNullType() throws CreationNotificationException {
        Request dto = new Request();
        dto.type = NotificationTypeDictionary.EMAIL.toString().toLowerCase();
        dto.id = "remote-id";
        dto.user = "073User";
        dto.ip = "10.12.13.14";
        dto.email = "user@server.ru";
        dto.content = "Content";

        Notification notification = transformer.transform(dto);
        assertNull(notification.getType());
    }
}