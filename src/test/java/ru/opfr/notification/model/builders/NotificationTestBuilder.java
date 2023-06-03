package ru.opfr.notification.model.builders;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.opfr.notification.model.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;

@AllArgsConstructor
@NoArgsConstructor(staticName = "aNotification")
@With
public class NotificationTestBuilder implements TestBuilder<Notification> {
    private Long id;
    private String remoteId = "remote-id";
    private NotificationTypeDictionary type = MESSAGE;
    private NotificationProcessStageDictionary[] stages = new NotificationProcessStageDictionary[0];
    private LocalDateTime updated;
    private LocalDateTime created;
    private String user = "073User";
    private String ip = "10.73.1.1";
    private String email = "mail@yandex.ru";
    private String content = "Test content";
    private String theme = "Test theme";
    private String[] attachments = new String[0];

    @Override
    public Notification build() {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setRemoteId(remoteId);
        notification.setType(type);
        Arrays.stream(stages).forEach(stageDictionary -> {
            NotificationStage stage = new NotificationStage();
            stage.setStage(stageDictionary);
            notification.addStage(stage);
        });
        notification.setCreated(LocalDateTime.now());
        Person person = new Person();
        person.setUser(user);
        person.setIp(ip);
        person.setEmail(email);
        notification.setPerson(person);
        notification.setContent(content);
        notification.setTheme(theme);
        Arrays.stream(attachments).forEach(stringAttachment -> {
            String[] parts = stringAttachment.split("|");
            NotificationAttachment attachment = new NotificationAttachment();
            attachment.setName(parts[0]);
            attachment.setContent(parts[1].getBytes());
            notification.addAttachment(attachment);
        });
        return notification;
    }
}
