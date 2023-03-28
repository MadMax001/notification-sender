package ru.opfr.notification.converters;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.dto.Request;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextHierarchy({
        @ContextConfiguration(classes = RequestFileConverterImpl.class),
        @ContextConfiguration(classes = RequestNotificationConverterImpl.class)
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestNotificationConverterImplTest {
    private final RequestNotificationConverterImpl transformer;

    @Test
    void createEntityFromDTO_WithAllNonNullFields() throws CreationNotificationException {
        Request dto = new Request();
        dto.type = NotificationTypeDictionary.EMAIL.toString();
        dto.id = "remote-id";
        dto.user = "073User";
        dto.ip = "10.73.13.14";
        dto.email = "user@server.ru";
        dto.content = "Content";
        dto.theme = "theme";
        Notification notification = transformer.convert(dto);

        assertNotNull(notification);
        assertNull(notification.getId());
        assertEquals(dto.id, notification.getRemoteId());
        assertEquals(dto.user, notification.getPerson().getUser());
        assertEquals(dto.ip, notification.getPerson().getIp());
        assertEquals(dto.email, notification.getPerson().getEmail());
        assertEquals(dto.content, notification.getContent());
        assertEquals(dto.theme, notification.getTheme());
        assertEquals(NotificationTypeDictionary.of(dto.type), notification.getType());
        assertEquals(0, notification.getStages().size());
        assertIterableEquals(Collections.emptyList(), notification.getAttachments());
        assertNull(notification.getCreated());
        assertNull(notification.getUpdated());
    }

    @Test
    void createEntityFromNullDTO_andThrowException() {
        assertThrows(CreationNotificationException.class, () -> transformer.convert(null));
    }

    @Test
    void createEntityWithWrongType_andGetNotificationWithNullType() throws CreationNotificationException {
        Request dto = new Request();
        dto.type = "aaa";
        dto.id = "remote-id";
        dto.user = "073User";
        dto.ip = "10.73.13.14";
        dto.email = "user@server.ru";
        dto.content = "Content";
        dto.theme = "Theme";

        Notification notification = transformer.convert(dto);
        assertNull(notification.getType());
    }

    @Test
    void createEntityWithTypeToLowerCase_andGetNotificationWithNullType() throws CreationNotificationException {
        Request dto = new Request();
        dto.type = NotificationTypeDictionary.EMAIL.toString().toLowerCase();
        dto.id = "remote-id";
        dto.user = "073User";
        dto.ip = "10.73.13.14";
        dto.email = "user@server.ru";
        dto.content = "Content";
        dto.theme = "Theme";

        Notification notification = transformer.convert(dto);
        assertNull(notification.getType());
    }

    @Test
    void createEntityWithAttachedFiles() throws CreationNotificationException {
        Request dto = new Request();
        dto.type = NotificationTypeDictionary.EMAIL.toString();
        dto.id = "remote-id";
        dto.email = "user@server.ru";
        dto.content = "Content";
        dto.theme = "theme";
        dto.files = new MultipartFile[]{
                new MockMultipartFile("file1", "file1", null, "Content number 1".getBytes()),
                new MockMultipartFile("file2", "file2", null, "Content number 2".getBytes()),
        };
        Notification notificationWithFiles = transformer.convert(dto);

        assertNotNull(notificationWithFiles);
        assertEquals(2, notificationWithFiles.getAttachments().size());
        assertEquals("file1", notificationWithFiles.getAttachments().get(0).getName());
        assertArrayEquals("Content number 1".getBytes(), notificationWithFiles.getAttachments().get(0).getContent());
        assertEquals("file2", notificationWithFiles.getAttachments().get(1).getName());
        assertArrayEquals("Content number 2".getBytes(), notificationWithFiles.getAttachments().get(1).getContent());
    }
}