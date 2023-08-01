package ru.opfr.e2e.v1;

import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.opfr.notification.AbstractContainersIntegrationTest;
import ru.opfr.notification.NotificationSenderServiceApplication;
import ru.opfr.notification.ObjectWriterConfiguration;
import ru.opfr.notification.ValidationMessages;
import ru.opfr.notification.model.builders.RequestTestBuilder;
import ru.opfr.notification.model.dto.Request;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static ru.opfr.notification.ValidationMessages.FILES_SIZE_TOO_LARGE;
import static ru.opfr.notification.model.builders.RequestTestBuilder.FILE_CONTENT_SEPARATOR;

@SpringBootTest(classes = {NotificationSenderServiceApplication.class, ObjectWriterConfiguration.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("repo_test")
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ConstraintExceptionIT extends AbstractContainersIntegrationTest {
    private final MockMvc mockMvc;

    private final ObjectWriter objectWriter;


    @Test
    void sendEMAILTypeNotification_WithNullEmail_AndGetBadResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withType("EMAIL").withEmail(null)
                .build();

        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", is(ValidationMessages.NULL_EMAIL)),
                        jsonPath("$.response", nullValue())
                );
    }

    @Test
    void sendEMAILTypeNotification_WithWrongEmail_AndGetBadResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withType("EMAIL").withEmail("wrong@email")
                .build();

        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", is(ValidationMessages.WRONG_EMAIL)),
                        jsonPath("$.response", nullValue())
                );
    }

    @Test
    void sendMessageTypeNotification_WithNullUsername_AndGetBadResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withType("MESSAGE").withUser(null)
                .build();

        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", is(ValidationMessages.NULL_USER)),
                        jsonPath("$.response", nullValue())
                );
    }

    @Test
    void sendMessageTypeNotification_WithNullIP_AndGetBadResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withType("MESSAGE").withIp(null)
                .build();

        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", is(ValidationMessages.NULL_IP)),
                        jsonPath("$.response", nullValue())
                );
    }

    @Test
    void sendMessageTypeNotification_WithWrongIP_AndGetBadResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withType("MESSAGE").withIp("aaaaa")
                .build();

        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", is(ValidationMessages.WRONG_IP)),
                        jsonPath("$.response", nullValue())
                );
    }

    @Test
    void sendWrongTypeNotification_AndGetBadResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withType("WRONG_TYPE")
                .build();

        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", is(ValidationMessages.NULL_TYPE)),
                        jsonPath("$.response", nullValue())
                );
    }

    @Test
    void sendRequest_WithLargeAttachment_AndGetBadResponse() throws Exception {

        StringBuilder fileContent = new StringBuilder("files");
        fileContent.append(FILE_CONTENT_SEPARATOR);
        fileContent.append("large.txt");
        fileContent.append(FILE_CONTENT_SEPARATOR);
        IntStream.range(0, 10000000).forEach(i -> fileContent.append("0"));

        Request request = RequestTestBuilder.aRequest().
                withFiles(new String[]{fileContent.toString()})
                .build();
        String requestJson = objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());
        MockMultipartFile file1 = (MockMultipartFile) request.files[0];


        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .file(file1)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.message", is(FILES_SIZE_TOO_LARGE)),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1"))
                );
    }

    @Test
    void sendRequest_WithNullAttachment_AndGetBadResponse() throws Exception {
        String[] filesContent = IntStream.range(0, 7).mapToObj(ind -> {
            StringBuilder fileContent = new StringBuilder("files");
            fileContent.append(FILE_CONTENT_SEPARATOR);
            fileContent.append(String.format("filename_%d.txt", ind));
            fileContent.append(FILE_CONTENT_SEPARATOR);
            IntStream.range(0, 100).forEach(i -> fileContent.append("0"));
            return fileContent.toString();
        }).toArray(String[]::new);


        Request request = RequestTestBuilder.aRequest().
                withFiles(filesContent)
                .build();
        String requestJson = objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());
        MockMultipartFile file1 = (MockMultipartFile) request.files[0];
        MockMultipartFile file2 = (MockMultipartFile) request.files[1];
        MockMultipartFile file3 = (MockMultipartFile) request.files[2];
        MockMultipartFile file4 = (MockMultipartFile) request.files[3];
        MockMultipartFile file5 = (MockMultipartFile) request.files[4];
        MockMultipartFile file6 = (MockMultipartFile) request.files[5];
        MockMultipartFile file7 = (MockMultipartFile) request.files[6];


        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .file(file4)
                        .file(file5)
                        .file(file6)
                        .file(file7)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.message", is(ValidationMessages.MAX_COUNT_ATTACHMENTS)),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1"))
                );
    }

    @Test
    void sendNotification_WithNullContent_AndGetBadResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withContent(null)
                .build();

        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", is(ValidationMessages.NULL_CONTENT)),
                        jsonPath("$.response", nullValue())
                );
    }


}
