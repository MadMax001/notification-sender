package ru.opfr.notification.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.opfr.notification.ApplicationConstants;
import ru.opfr.notification.constraint.NotEmptyCollection;
import ru.opfr.notification.constraint.NotNullByType;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static javax.persistence.FetchType.LAZY;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification", schema = "notification")
@NotNullByType(field = "person.user", type = MESSAGE, message = ApplicationConstants.NULL_USER)
@NotNullByType(field = "person.ip", type = MESSAGE, message = ApplicationConstants.NULL_IP)
@NotNullByType(field = "person.email", types = {EMAIL, FILE}, message = ApplicationConstants.NULL_EMAIL)
public class Notification {
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_sequence")
    @SequenceGenerator(name="notification_sequence", sequenceName = "notification.notification_sequence", allocationSize = 1)
    private Long id;

    private String remoteId;

    @Enumerated(EnumType.STRING)
    @NotNull(message = ApplicationConstants.NULL_TYPE)
    private NotificationTypeDictionary type;

    @NotEmptyCollection(message = ApplicationConstants.NO_STAGES)
    @OneToMany(fetch = LAZY, mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationStage> stages = new ArrayList<>();

    private LocalDateTime updated;

    private LocalDateTime created;

    @Embedded
    @AttributeOverride(name="user", column=@Column(name="person_user"))
    @AttributeOverride(name="ip", column=@Column(name="person_ip"))
    @AttributeOverride(name="email", column=@Column(name="person_email"))
    @Valid
    private Person person;

    @NotNull(message = ApplicationConstants.NULL_CONTENT)
    private String content;

    private String theme;

    @PrePersist
    private void onCreate() {
        created = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updated = LocalDateTime.now();
    }

    public void addStage(NotificationStage stage) {
        stage.setNotification(this);
        this.stages.add(stage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id) && Objects.equals(remoteId, that.remoteId) && type == that.type && Objects.equals(stages, that.stages) && Objects.equals(updated, that.updated) && Objects.equals(created, that.created) && Objects.equals(person, that.person) && Objects.equals(content, that.content) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, remoteId, type, stages, updated, created, person, content, theme);
    }
}
