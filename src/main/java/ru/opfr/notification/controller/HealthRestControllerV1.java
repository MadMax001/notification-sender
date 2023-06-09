package ru.opfr.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.opfr.notification.model.dto.ResponseWrapper;

@RestController
@RequestMapping("/api/v1/health")
public class HealthRestControllerV1 {
    @GetMapping()
    public ResponseWrapper health() {
        return ResponseWrapper.builder().version("v1").response("ok").build();
    }
}
