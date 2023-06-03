package ru.opfr.notification.reporitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationProcessStageDictionary;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByLatestStage(NotificationProcessStageDictionary stageDictionary);
    List<Notification> findAllByLatestStageAndCreatedBeforeAndUpdatedIsNull
            (NotificationProcessStageDictionary stageDictionary, LocalDateTime creation);
}
