package ru.opfr.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatRestController {
    private final NotificationService notificationService;

    @PostMapping("/incomplete")
    public List<Notification> getIncompleteNotifications() {
        return notificationService.getIncompleteNotifications();
    }
}
