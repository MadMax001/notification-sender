package ru.opfr.notification.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.FetchType.LAZY;
import static ru.opfr.notification.ApplicationConstants.MAX_LENGTH_STAGE_MESSAGE;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_stage", schema = "notification")
public class NotificationStage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_sequence")
    @SequenceGenerator(name="notification_sequence", sequenceName = "notification.notification_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = LAZY)
    //@Cascade(SAVE_UPDATE)
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "stage can't be null")
    private NotificationProcessStageDictionary stage;

    @Size(max=255, message = MAX_LENGTH_STAGE_MESSAGE)
    private String message;

    private LocalDateTime created;

    @PrePersist
    private void onCreate() {
        created = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationStage)) return false;
        NotificationStage that = (NotificationStage) o;
        return notification.equals(that.notification) && stage == that.stage && Objects.equals(message, that.message) && Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notification, stage, message, created);
    }
}
