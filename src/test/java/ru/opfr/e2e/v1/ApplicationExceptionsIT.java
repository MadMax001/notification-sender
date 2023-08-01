package ru.opfr.e2e.v1;

import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.opfr.notification.AbstractContainersIntegrationTest;
import ru.opfr.notification.NotificationSenderServiceApplication;
import ru.opfr.notification.ObjectWriterConfiguration;
import ru.opfr.notification.converters.RequestNotificationConverter;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.builders.RequestTestBuilder;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.service.EmailSenderService;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(classes = {NotificationSenderServiceApplication.class, ObjectWriterConfiguration.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("repo_test")
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ApplicationExceptionsIT extends AbstractContainersIntegrationTest {
    private final MockMvc mockMvc;

    private final ObjectWriter objectWriter;

    @SpyBean
    RequestNotificationConverter requestNotificationConverter;

    @SpyBean
    EmailSenderService emailSenderService;

    @Test
    void sendRequest_AndThrowsCNE_AndBadResponse() throws Exception {
        Throwable exception = new CreationNotificationException("Error in converter");
        doThrow(exception).when(requestNotificationConverter).convert(any(Request.class));
        Request request = RequestTestBuilder.aRequest().build();
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
                        jsonPath("$.message", nullValue()),
                        jsonPath("$.response.success", is(false)),
                        jsonPath("$.response.operationId", nullValue()),
                        jsonPath("$.response.remoteId", is(request.id)),
                        jsonPath("$.response.message", is(exception.toString())));

    }

    @Test
    void sendEMAILRequest_AndThrowsSNE_AndBadResponse() throws Exception {
        Throwable exception = new SendNotificationException("Error in sending");
        doThrow(exception).when(emailSenderService).send(any(Notification.class));
        Request request = RequestTestBuilder.aRequest()
                .withType("EMAIL").build();
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
                        jsonPath("$.message", nullValue()),
                        jsonPath("$.response.success", is(false)),
                        jsonPath("$.response.operationId", notNullValue()),
                        jsonPath("$.response.remoteId", is(request.id)),
                        jsonPath("$.response.message", is(exception.toString())));
    }
}
