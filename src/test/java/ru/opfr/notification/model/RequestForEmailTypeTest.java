package ru.opfr.notification.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.model.dto.Request;

import java.io.IOException;

class RequestForEmailTypeTest {

    @Test
    void createRequestWithFileContent() throws IOException {
        String content = "Hello World";
        byte[] contentBytes = content.getBytes();
        MultipartFile multipartFile = new MockMultipartFile("sourceFile.tmp", contentBytes);
        Request request = new Request();
        request.id = "id0";
        request.type = "EMAIL";
        request.email = "name@server.ru";
        request.content = new String(multipartFile.getBytes());

        MultipartFile receivedMultipartFile = new MockMultipartFile("unknown", request.content.getBytes());
        Assertions.assertArrayEquals(contentBytes, receivedMultipartFile.getBytes());
    }
}
