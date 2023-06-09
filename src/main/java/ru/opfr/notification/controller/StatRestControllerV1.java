package ru.opfr.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.dto.ResponseWrapper;
import ru.opfr.notification.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatRestControllerV1 {
    private final NotificationService notificationService;

    @PostMapping("/incomplete")
    public ResponseWrapper getIncompleteNotifications() {
        List<Notification> notifications = notificationService.getIncompleteNotifications();
        return ResponseWrapper.builder().version("v1").response(notifications).build();
    }
}
