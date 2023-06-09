package ru.opfr.notification.model.builders;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.model.dto.Request;

import java.util.Arrays;

@AllArgsConstructor
@NoArgsConstructor(staticName = "aRequest")
@With
public class RequestTestBuilder implements TestBuilder<Request>{
    public static final String FILE_CONTENT_SEPARATOR = "___";
    private String id = "remote-test-id";
    private String type = "EMAIL";
    private String user = "073User";
    private String ip = "10.73.1.1";
    private String email = "mail@yandex.ru";
    private String content = "Test content";
    private String theme = "Test theme";
    public String[] files = new String[0];


    @Override
    public Request build() {
        Request request = new Request();
        request.id = id;
        request.type = type;
        request.user = user;
        request.ip = ip;
        request.email = email;
        request.content = content;
        request.theme = theme;
        request.files = Arrays.stream(files).map(element -> {
            String[] parts = element.split(FILE_CONTENT_SEPARATOR);
            if (parts.length == 2) {
                return new MockMultipartFile(
                        "file",
                        parts[0],
                        MediaType.TEXT_PLAIN_VALUE,
                        parts[1].getBytes()
                );
            }
            if (parts.length == 3) {
                return new MockMultipartFile(
                        parts[0],
                        parts[1],
                        MediaType.TEXT_PLAIN_VALUE,
                        parts[2].getBytes()
                );
            }
            throw new IllegalArgumentException("File content must have 2 or 3 parts with separator: " + FILE_CONTENT_SEPARATOR);
        }).toArray(MultipartFile[]::new);
        return request;
    }
}
