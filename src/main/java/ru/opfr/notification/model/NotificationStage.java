package ru.opfr.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.FetchType.LAZY;
import static ru.opfr.notification.ApplicationConstants.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_stage", schema = "notification")
public class NotificationStage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stage_sequence")
    @SequenceGenerator(name="stage_sequence", sequenceName = "notification.stage_sequence", allocationSize = 1)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    //@Cascade(SAVE_UPDATE)
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @NotNull(message = NULL_STAGE_TYPE)
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
        return stage == that.stage &&
                Objects.equals(message, that.message) &&
                Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stage, message, created);
    }
}
