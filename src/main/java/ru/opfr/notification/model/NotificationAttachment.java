package ru.opfr.notification.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.FetchType.LAZY;
import static ru.opfr.notification.ApplicationConstants.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_attachment", schema = "notification")
public class NotificationAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_sequence")
    @SequenceGenerator(name="attachment_sequence", sequenceName = "notification.attachment_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Size(max=255, message = MAX_LENGTH_ATTACHMENT_NAME)
    @NotNull(message = NULL_ATTACHMENT_NAME)
    private String name;

    private byte[] content;


}
