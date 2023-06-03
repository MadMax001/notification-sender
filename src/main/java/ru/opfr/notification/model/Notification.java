package ru.opfr.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JoinFormula;
import ru.opfr.notification.constraint.NotEmptyCollection;
import ru.opfr.notification.constraint.NotNullByType;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static javax.persistence.FetchType.LAZY;
import static ru.opfr.notification.ApplicationConstants.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification", schema = "notification")
@NotNullByType(field = "person.user", type = MESSAGE, message = NULL_USER)
@NotNullByType(field = "person.ip", type = MESSAGE, message = NULL_IP)
@NotNullByType(field = "person.email", types = {EMAIL, FILE}, message = NULL_EMAIL)
public class Notification {
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_sequence")
    @SequenceGenerator(name="notification_sequence", sequenceName = "notification.notification_sequence", allocationSize = 1)
    private Long id;


    @Size(max=255, message = MAX_LENGTH_REMOTE_ID)
    private String remoteId;

    @Enumerated(EnumType.STRING)
    @NotNull(message = NULL_TYPE)
    private NotificationTypeDictionary type;

    @NotEmptyCollection(message = NO_STAGES)
    @OneToMany(fetch = LAZY, mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationStage> stages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula(value = "(SELECT stage.id FROM notification.notification_stage stage " +
            "WHERE stage.notification_id = id ORDER BY stage.id DESC LIMIT 1)", referencedColumnName = "id")
    private NotificationStage latest;

    private LocalDateTime updated;

    private LocalDateTime created;

    @Embedded
    @AttributeOverride(name="user", column=@Column(name="person_user"))
    @AttributeOverride(name="ip", column=@Column(name="person_ip"))
    @AttributeOverride(name="email", column=@Column(name="person_email"))
    @Valid
    private Person person;

    @NotNull(message = NULL_CONTENT)
    private String content;

    @Size(max=255, message = MAX_LENGTH_THEME)
    private String theme;

    @JsonIgnore
    @OneToMany(fetch = LAZY, mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(max=5, message = MAX_COUNT_ATTACHMENTS)
    @NotNull(message = NULL_ATTACHMENTS)
    private List<NotificationAttachment> attachments = new ArrayList<>();

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

    public void addAttachment(NotificationAttachment attachment) {
        attachment.setNotification(this);
        this.attachments.add(attachment);
    }

    public void clearAttachments() {
        for (NotificationAttachment attachment : this.attachments) {
            attachment.setNotification(null);
        }
        this.attachments.clear();
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

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.LL.yyyy HH:mm:ss");
        return "Notification{" +
                "id=" + Optional.ofNullable(id).orElse(0L) +
                ", remoteId='" + remoteId + '\'' +
                ", type=" + Optional.ofNullable(type).map(NotificationTypeDictionary::toString).orElse("not defined") +
                ", stages=" + Optional.ofNullable(stages).map(Collection::size).orElse(0) +
                ", updated=" + Optional.ofNullable(updated).map(upd -> upd.format(formatter)).orElse("") +
                ", created=" + Optional.ofNullable(created).map(crt -> crt.format(formatter)).orElse("") +
                ", person=" + Optional.ofNullable(person).map(Person::toString).orElse("not defined") +
                ", content='" + content + '\'' +
                ", theme='" + theme + '\'' +
                ", attachments=" + Optional.ofNullable(attachments).map(Collection::size).orElse(0) +
                '}';
    }
}
