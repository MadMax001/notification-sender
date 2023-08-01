package ru.opfr.notification.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.opfr.notification.NotificationSenderSecurityConfiguration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest
@ContextConfiguration(classes={NotificationSenderSecurityConfiguration.class, HealthRestControllerV1.class})
@AutoConfigureMockMvc
@ActiveProfiles("repo_test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class HealthRestControllerV1IT {
    private final MockMvc mockMvc;

    @Test
    void checkHealth() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.timestamp", notNullValue()),
                    jsonPath("$.version", is("v1")),
                    jsonPath("$.response", is("ok")));

    }
}
