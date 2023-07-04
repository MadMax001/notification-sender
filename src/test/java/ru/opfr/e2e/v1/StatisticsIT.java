package ru.opfr.e2e.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.opfr.notification.NotificationSenderServiceApplication;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.builders.RequestTestBuilder;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.service.EmailSenderService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = NotificationSenderServiceApplication.class)
@ActiveProfiles("repo_test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatisticsIT {
    private final WebApplicationContext context;

    MockMvc mockMvc;

    ObjectWriter objectWriter;

    @SpyBean
    EmailSenderService emailSenderService;

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
    @WithMockUser(value = "stat_user", roles = "STAT")
    void sendEMAILRequest_AndThrowsSNE_AndGetStatistics_AndFindNotificationById() throws Exception{
        Throwable exception = new SendNotificationException("Error in sending");
        doThrow(exception).when(emailSenderService).send(any(Notification.class));
        Request request = RequestTestBuilder.aRequest()
                .withType("EMAIL").build();
        String requestJson=objectWriter.writeValueAsString(request);
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes());

        MvcResult badResponseMvc = mockMvc.perform(multipart("/api/v1/notifications")
                        .file(requestFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        JSONObject jsonObject = new JSONObject(badResponseMvc.getResponse().getContentAsString());
        Integer notificationId = (Integer)jsonObject.getJSONObject("response").get("operationId");

        MvcResult statisticsResponseMvc = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/statistics/incomplete"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject statisticsObj = new JSONObject(statisticsResponseMvc.getResponse().getContentAsString());
        JSONArray incompleteNotifications = statisticsObj.getJSONArray("response");
        List<Integer> incompleteNotificationIdList =  IntStream.range(0,incompleteNotifications.length())
                .mapToObj(i -> {
                    try {
                        return (Integer)incompleteNotifications.getJSONObject(i).get("id");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        assertTrue(incompleteNotificationIdList.contains(notificationId));
    }
}
