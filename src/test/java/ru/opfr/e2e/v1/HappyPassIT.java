package ru.opfr.e2e.v1;

import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.opfr.notification.AbstractContainersIntegrationTest;
import ru.opfr.notification.NotificationSenderServiceApplication;
import ru.opfr.notification.ObjectWriterConfiguration;
import ru.opfr.notification.messageprocess.AsyncWinMessageService;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.SMTPServerAnswer;
import ru.opfr.notification.model.builders.RequestTestBuilder;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.service.SMTPMailSender;

import javax.mail.Message;
import javax.mail.Session;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static ru.opfr.notification.model.builders.RequestTestBuilder.FILE_CONTENT_SEPARATOR;

@SpringBootTest(classes = {NotificationSenderServiceApplication.class, ObjectWriterConfiguration.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("repo_test")
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class HappyPassIT extends AbstractContainersIntegrationTest {
    private final MockMvc mockMvc;

    private final ObjectWriter objectWriter;

    @MockBean
    SMTPMailSender smtpMailSender;

    @MockBean
    AsyncWinMessageService messageService;

    @Test
    void sendEMAILRequest_WithoutAttachments_andCheckResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withType("EMAIL")
                .build();

        SMTPServerAnswer smtpResponse = new SMTPServerAnswer(200, "Successful");
        when(smtpMailSender.send(any(Session.class), any(Message.class))).thenReturn(
                smtpResponse
        );


        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", nullValue()),
                        jsonPath("$.response.success", is(true)),
                        jsonPath("$.response.operationId", notNullValue()),
                        jsonPath("$.response.remoteId", is(request.id)),
                        jsonPath("$.response.message", is(smtpResponse.toString())));
    }

    @Test
    void sendEMAILRequest_WithAttachments_andCheckResponse() throws Exception {
        StringBuilder fileContent = new StringBuilder("files");
        fileContent.append(FILE_CONTENT_SEPARATOR);
        fileContent.append("filename.txt");
        fileContent.append(FILE_CONTENT_SEPARATOR);
        IntStream.range(0, 100).forEach(i -> fileContent.append("0"));

        Request request = RequestTestBuilder.aRequest()
                .withType("EMAIL")
                .withFiles(new String[]{fileContent.toString()})
                .build();
        String requestJson = objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());
        MockMultipartFile file1 = (MockMultipartFile) request.files[0];

        SMTPServerAnswer smtpResponse = new SMTPServerAnswer(200, "Successful");
        when(smtpMailSender.send(any(Session.class), any(Message.class))).thenReturn(
                smtpResponse
        );

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .file(file1)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", nullValue()),
                        jsonPath("$.response.success", is(true)),
                        jsonPath("$.response.operationId", notNullValue()),
                        jsonPath("$.response.remoteId", is(request.id)),
                        jsonPath("$.response.message", is(smtpResponse.toString())));
    }

    @Test
    void sendMESSAGERequest_WithoutAttachments_andCheckResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest()
                .withType("MESSAGE")
                .build();

        WinConsoleExecuteResponse messengerResponse = WinConsoleExecuteResponse.builder()
                .consoleStdOut(Collections.singletonList("Send successfully"))
                .consoleErrOut(Collections.emptyList())
                .build();
        when(messageService.send(any(Notification.class))).thenReturn(
                CompletableFuture.completedFuture(messengerResponse)
        );

        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", nullValue()),
                        jsonPath("$.response.success", is(true)),
                        jsonPath("$.response.operationId", notNullValue()),
                        jsonPath("$.response.remoteId", is(request.id)),
                        jsonPath("$.response.message", is("Send successfully")));
    }
}
