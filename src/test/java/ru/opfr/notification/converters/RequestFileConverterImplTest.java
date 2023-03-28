package ru.opfr.notification.converters;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.NotificationAttachment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RequestFileConverterImpl.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestFileConverterImplTest {
    private final RequestFileConverter transformer;


    @Test
    void transformTwoFilesFromRequest() throws CreationNotificationException {
        MultipartFile[] files = {
            new MockMultipartFile("file1", "file1", null, "Content number 1".getBytes()),
            new MockMultipartFile("file2", "file2", null, "Content number 2".getBytes()),
        };
        List<NotificationAttachment> filesList= new ArrayList<>();
        for (MultipartFile file : files) {
            filesList.add(transformer.convert(file));
        }

        assertEquals("file1", filesList.get(0).getName());
        assertArrayEquals("Content number 1".getBytes(), filesList.get(0).getContent());

        assertEquals("file2", filesList.get(1).getName());
        assertArrayEquals("Content number 2".getBytes(), filesList.get(1).getContent());

    }

    @Test
    void transformFileFromRequest_AndCantGetContent_AndThrowException() throws IOException {
        MultipartFile file = new MockMultipartFile("file1", "file1", null, "Content number 1".getBytes());
        MultipartFile mockFile = spy(file);
        doThrow(IOException.class).when(mockFile).getBytes();

        assertThrows(CreationNotificationException.class, () -> transformer.convert(mockFile));

    }
}
