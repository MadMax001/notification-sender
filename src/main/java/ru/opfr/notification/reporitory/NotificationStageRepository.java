package ru.opfr.notification.reporitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.opfr.notification.model.NotificationStage;

@Repository
public interface NotificationStageRepository extends JpaRepository<NotificationStage, Long> {
}
