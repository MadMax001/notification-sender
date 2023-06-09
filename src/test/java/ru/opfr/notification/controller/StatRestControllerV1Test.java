package ru.opfr.notification.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.opfr.notification.NotificationSenderSecurityConfiguration;
import ru.opfr.notification.model.NotificationProcessStageDictionary;
import ru.opfr.notification.model.builders.NotificationTestBuilder;
import ru.opfr.notification.service.NotificationService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.FAILED;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;
import static org.hamcrest.Matchers.*;

@WebMvcTest
@ContextConfiguration(classes={NotificationSenderSecurityConfiguration.class, ExceptionHandlerController.class, StatRestControllerV1.class})
@ActiveProfiles("repo_test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatRestControllerV1Test {
    @MockBean
    private final NotificationService notificationService;

    private final WebApplicationContext context;

    MockMvc mockMvc;


    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void statisticsPageWithoutAuthentication_ByPOSTMethod_AndGetUnauthorizedStatus() throws Exception {
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Collections.singletonList(NotificationTestBuilder.aNotification()
                        .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                        .build())
        );
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/statistics/incomplete"))
                .andExpect(status().isUnauthorized());
    }


    @WithMockUser(value = "stat_user", roles = "STAT")
    @Test
    void statisticsPageWithCorrectAuthentication_AndCheckThemeAndStagesOfTwoReturnedNotifications() throws Exception {
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Arrays.asList(
                        NotificationTestBuilder.aNotification()
                                .withTheme("Тестовая тема1")
                                .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                                .build(),
                        NotificationTestBuilder.aNotification()
                                .withTheme("Тестовая тема2")
                                .withStages(new NotificationProcessStageDictionary[]{RECEIVED, FAILED})
                                .build())

        );
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/statistics/incomplete"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().encoding(StandardCharsets.ISO_8859_1),
                        jsonPath("$.timestamp", notNullValue()),
                        jsonPath("$.version", is("v1")),
                        jsonPath("$.response", hasSize(2)),
                        jsonPath("$.response[0].theme", is("Тестовая тема1")),
                        jsonPath("$.response[0].stages", hasSize(1)),
                        jsonPath("$.response[0].stages[0].stage", is("RECEIVED")),
                        jsonPath("$.response[0].attachments").doesNotExist(),
                        jsonPath("$.response[1].theme", is("Тестовая тема2")),
                        jsonPath("$.response[1].attachments").doesNotExist(),
                        jsonPath("$.response[1].stages", hasSize(2)),
                        jsonPath("$.response[1].stages[0].stage", is("RECEIVED")),
                        jsonPath("$.response[1].stages[1].stage", is("FAILED"))
                        );
    }

    @WithMockUser(value = "stat_user", roles = "STAT")
    @Test
    void statisticsPageWithCorrectAuthentication_ByGETMethod_AndGetUnauthorizedStatus() throws Exception {
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Arrays.asList(
                        NotificationTestBuilder.aNotification()
                                .withTheme("Тестовая тема1")
                                .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                                .build(),
                        NotificationTestBuilder.aNotification()
                                .withTheme("Тестовая тема2")
                                .withStages(new NotificationProcessStageDictionary[]{RECEIVED, FAILED})
                                .build())

        );
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/statistics/incomplete"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(value = "stat_user", password = "bbbb", roles = "USER")
    @Test
    void statisticsPageWithIncorrectAuthentication_AndGetUnauthorizedStatus() throws Exception {
        when(notificationService.getIncompleteNotifications()).thenReturn(
                Arrays.asList(
                        NotificationTestBuilder.aNotification()
                                .withTheme("Тестовая тема1")
                                .withStages(new NotificationProcessStageDictionary[]{RECEIVED})
                                .build(),
                        NotificationTestBuilder.aNotification()
                                .withTheme("Тестовая тема2")
                                .withStages(new NotificationProcessStageDictionary[]{RECEIVED, FAILED})
                                .build())

        );
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/statistics/incomplete"))
                .andExpect(status().isForbidden());
    }

}