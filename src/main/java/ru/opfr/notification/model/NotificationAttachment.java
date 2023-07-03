package ru.opfr.notification.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.Arrays;
import java.util.Objects;

import static javax.persistence.FetchType.LAZY;
import static ru.opfr.notification.ValidationMessages.*;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationAttachment)) return false;
        NotificationAttachment that = (NotificationAttachment) o;
        return Objects.equals(name, that.name) && Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }
}
