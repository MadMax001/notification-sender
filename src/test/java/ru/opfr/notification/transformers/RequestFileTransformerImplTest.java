package ru.opfr.notification.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.model.NotificationAttachment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestFileTransformerImplTest {
    private RequestFileTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new RequestFileTransformerImpl();
    }

    @Test
    void transformThreeFilesFromRequest() throws IOException {
        MultipartFile[] files = {
            new MockMultipartFile("file1", "file1", null, "Content number 1".getBytes()),
            new MockMultipartFile("file2", "file2", null, "Content number 2".getBytes()),
        };
        List<NotificationAttachment> filesList= new ArrayList<>();
        for (MultipartFile file : files) {
            filesList.add(transformer.transform(file));
        }

        assertEquals("file1", filesList.get(0).getName());
        assertArrayEquals("Content number 1".getBytes(), filesList.get(0).getContent());

        assertEquals("file2", filesList.get(1).getName());
        assertArrayEquals("Content number 2".getBytes(), filesList.get(1).getContent());

    }
}
