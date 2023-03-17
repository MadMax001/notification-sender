package ru.opfr.notification.reporitory;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.opfr.notification.model.NotificationAttachment;

public interface NotificationAttachmentRepository extends JpaRepository<NotificationAttachment, Long> {
}
