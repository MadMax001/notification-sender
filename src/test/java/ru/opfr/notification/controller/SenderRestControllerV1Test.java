package ru.opfr.notification.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.opfr.notification.NotificationSenderSecurityConfiguration;
import ru.opfr.notification.constraint.impl.FilesSizeValidator;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.model.builders.RequestTestBuilder;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import ru.opfr.notification.service.SenderServiceFacadeSafeWrapper;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static ru.opfr.notification.ApplicationConstants.FILES_SIZE_TOO_LARGE;
import static ru.opfr.notification.model.builders.RequestTestBuilder.FILE_CONTENT_SEPARATOR;

@WebMvcTest
@ContextConfiguration(classes=
        {
                NotificationSenderSecurityConfiguration.class,
                SenderRestControllerV1.class,
                ExceptionHandlerController.class,
                FilesSizeValidator.class
        })
@ActiveProfiles("repo_test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SenderRestControllerV1Test {
    @MockBean
    SenderServiceFacadeSafeWrapper senderService;

    private final WebApplicationContext context;


    MockMvc mockMvc;

    ObjectWriter objectWriter;

    @Captor
    ArgumentCaptor<Request> requestArgumentCaptor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectWriter = mapper.writer().withDefaultPrettyPrinter();

    }

    @Test
    void sendRequestWithoutAttachments_andCheckResponse() throws Exception {
        Request request = RequestTestBuilder.aRequest().build();

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                            .success(true).operationId(10L)
                            .remoteId(request1.id).message("Русскоязычное сообщение")
                                    .build();
        });

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
                        jsonPath("$.response.operationId", is(10)),
                        jsonPath("$.response.remoteId", is(request.id)),
                        jsonPath("$.response.message", is("Русскоязычное сообщение")));
    }

    @Test
    void sendRequestWithAttachments_andCheckFileContent() throws Exception {

        Request request = RequestTestBuilder.aRequest().
                withFiles(new String[]{
                        "files" + FILE_CONTENT_SEPARATOR + "file1.txt" + FILE_CONTENT_SEPARATOR + "Content of file 1",
                        "files" + FILE_CONTENT_SEPARATOR + "file2.txt" + FILE_CONTENT_SEPARATOR + "Содержимое файла 2",
                })
                .build();

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("Русскоязычное сообщение")
                    .build();
        });

        MockMultipartFile file1 = (MockMultipartFile)request.files[0];
        MockMultipartFile file2 = (MockMultipartFile)request.files[1];
        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(file1)
                        .file(file2)
                        .file(requestFile))
                .andDo(print())
                .andExpect(status().isCreated());
        verify(senderService, times(1)).safeSend(requestArgumentCaptor.capture());
        Request request1 = requestArgumentCaptor.getValue();
        assertNotNull(request1);
        assertEquals(2, request1.files.length);
        assertEquals("file1.txt", request1.files[0].getOriginalFilename());
        assertEquals("Content of file 1", new String(request1.files[0].getBytes()));
        assertEquals("file2.txt", request1.files[1].getOriginalFilename());
        assertEquals("Содержимое файла 2", new String(request1.files[1].getBytes()));

    }

    @Test
    void sendNullRequest_viaPOSTMethod_AndGet400Status() throws Exception {

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("Русскоязычное сообщение")
                    .build();
        });

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("message")
                    .build();
        });


        mockMvc.perform(post("/api/v1/notifications")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.message", nullValue())

                );
    }

    @Test
    void sendRequestWithoutAttachments_viaWrongMethod_AndGet405Status() throws Exception {
        Request request = RequestTestBuilder.aRequest().build();
        String requestJson=objectWriter.writeValueAsString(request);

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("Русскоязычное сообщение")
                    .build();
        });

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("message")
                    .build();
        });


        mockMvc.perform(put("/api/v1/notifications")
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isMethodNotAllowed(),
                        jsonPath("$").doesNotExist()
                        );
    }

    @Test
    void sendRequest_AndThrowsRuntimeException_AndGet500Status() throws Exception {
        Request request = RequestTestBuilder.aRequest().build();
        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        Throwable exception = new ApplicationRuntimeException(new Exception("Error in send process"));
        doThrow(exception).when(senderService).safeSend(any());
        mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isInternalServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.message", containsString("Error in send process")),
                        jsonPath("$.message", containsString("Exception")),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1"))
                );
    }

    @Test
    void sendRequestToWrongAddress_StartsWithNotifications_AndGet401Status() throws Exception
    {
        Request request = RequestTestBuilder.aRequest().build();
        String requestJson=objectWriter.writeValueAsString(request);

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("Русскоязычное сообщение")
                    .build();
        });

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("message")
                    .build();
        });


        mockMvc.perform(put("/api/v1/notifications0")
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void sendRequestToWrongAddress_AndGet403Status() throws Exception
    {
        Request request = RequestTestBuilder.aRequest().build();
        String requestJson=objectWriter.writeValueAsString(request);

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("Русскоязычное сообщение")
                    .build();
        });

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("message")
                    .build();
        });


        mockMvc.perform(put("/api/v1/another")
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void sendRequestWithLargeAttachment_andThrowConstraintException() throws Exception {

        StringBuilder fileContent = new StringBuilder("files");
        fileContent.append(FILE_CONTENT_SEPARATOR);
        fileContent.append("large.txt");
        fileContent.append(FILE_CONTENT_SEPARATOR);
        IntStream.range(0, 10000000).forEach(i -> fileContent.append("0"));

        Request request = RequestTestBuilder.aRequest().
                withFiles(new String[]{fileContent.toString()})
                .build();
        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());
        MockMultipartFile file1 = (MockMultipartFile)request.files[0];

        when(senderService.safeSend(any(Request.class))).then(invocationOnMock -> {
            Request request1 = invocationOnMock.getArgument(0);
            return Response.builder()
                    .success(true).operationId(10L)
                    .remoteId(request1.id).message("Русскоязычное сообщение")
                    .build();
        });

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

}