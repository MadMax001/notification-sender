package ru.opfr.notification.reporitory;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationStage;
import ru.opfr.notification.model.Person;


import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("repo_test")
class NotificationRepositoryTest {

    private final NotificationRepository notificationRepository;

    private final NotificationStageRepository notificationStageRepository;
    private final TestEntityManager entityManager;

    @Test
    void persistNotificationWithoutStages_AndGetItByIdAfterFlush_AndCheckForNonNullCreatedField() {
        Person modelPerson = new Person();
        modelPerson.setUser("073User");
        modelPerson.setIp("10.11.12.13");
        modelPerson.setEmail("user@server.ru");

        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);

        Notification notification = new Notification();
        notification.setType(MESSAGE);
        notification.setPerson(modelPerson);
        notification.addStage(stage);
        notification.setContent("Test content \nthe second line");
        notification.setRemoteId("test-remote-id");
        notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(notification.getId());
        Notification dbNotification = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(notification.getId());

        assertNotNull(dbNotification);
        assertEquals(notification.getContent(), dbNotification.getContent());
        assertEquals(notification.getRemoteId(), dbNotification.getRemoteId());
        assertEquals(notification.getType().toString().toLowerCase(), dbNotification.getType().toString().toLowerCase());

        assertEquals(notification.getPerson().getEmail(), dbNotification.getPerson().getEmail());
        assertEquals(notification.getPerson().getIp(), dbNotification.getPerson().getIp());
        assertEquals(notification.getPerson().getUser(), dbNotification.getPerson().getUser());

        assertEquals(1, dbNotification.getStages().size());

        assertNotNull(dbNotification.getCreated());
        assertNull(dbNotification.getUpdated());

        NotificationStage dbStage = dbNotification.getStages().get(0);
        assertNotNull(dbStage.getId());
        assertNotNull(dbStage.getCreated());
        assertEquals(RECEIVED, dbStage.getStage());
        assertNull(dbStage.getMessage());
        assertEquals(dbNotification.getId(), dbStage.getNotification().getId());

    }

    @Test()
    void persistNotification_AndGetByAnotherId() {
        Person modelPerson = new Person();
        modelPerson.setUser("073User");
        modelPerson.setIp("10.11.12.13");
        modelPerson.setEmail("user@server.ru");

        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);

        Notification notification = new Notification();
        notification.setType(MESSAGE);
        notification.setPerson(modelPerson);
        notification.setContent("Test content \nthe second line");
        notification.setRemoteId("test-remote-id");
        notification.addStage(stage);
        notificationRepository.save(notification);
        entityManager.flush();
        assertNull(notificationRepository.findById(Long.MAX_VALUE).orElse(null));

    }

    @Test
    void updateNotification_WithFlushBetweenTwoPersistOperations_AndCheckCreatedAndUpdatedFields() {
        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.11.12.13");
        modelPerson1.setEmail("user@server.ru");
        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);

        Notification notification1 = new Notification();
        notification1.setType(MESSAGE);
        notification1.setPerson(modelPerson1);
        notification1.setContent("Test content \nthe second line");
        notification1.setRemoteId("test-remote-id");
        notification1.addStage(stage);
        notificationRepository.save(notification1);
        Long id = notification1.getId();
        entityManager.flush();

        Notification dbNotification = notificationRepository.findById(id).orElse(null);
        assertNotNull(dbNotification);

        Person modelPerson2 = new Person();
        modelPerson2.setUser("073User2");
        modelPerson2.setIp("14.15.16.17");
        modelPerson2.setEmail("user2@server2.ru");
        dbNotification.setType(EMAIL);
        dbNotification.setPerson(modelPerson2);
        dbNotification.setContent("Test content2");
        dbNotification.setRemoteId("test-remote2-id");
        notificationRepository.save(dbNotification);
        entityManager.flush();

        Notification dbUpdatedNotification = notificationRepository.findById(id).orElse(null);

        assertNotNull(dbUpdatedNotification);
        assertEquals(dbNotification.getContent(), dbUpdatedNotification.getContent());
        assertEquals(dbNotification.getRemoteId(), dbUpdatedNotification.getRemoteId());
        assertEquals(dbNotification.getType().toString().toLowerCase(), dbUpdatedNotification.getType().toString().toLowerCase());
        assertEquals(dbNotification.getPerson().getEmail(), dbUpdatedNotification.getPerson().getEmail());
        assertEquals(dbNotification.getPerson().getIp(), dbUpdatedNotification.getPerson().getIp());
        assertEquals(dbNotification.getPerson().getUser(), dbUpdatedNotification.getPerson().getUser());
        assertEquals(1, dbUpdatedNotification.getStages().size());

        assertNotNull(dbUpdatedNotification.getCreated());
        assertNotNull(dbUpdatedNotification.getUpdated());
        assertTrue(dbUpdatedNotification.getUpdated().isAfter(dbUpdatedNotification.getCreated()));

    }

    @Test
    void persistNotificationWIthOneStage_ThenUpdateNotificationByAddingAnotherStage_AndCheckForNullStagesFields() {
        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.11.12.13");
        modelPerson1.setEmail("user@server.ru");

        NotificationStage stage1 = new NotificationStage();
        stage1.setStage(RECEIVED);

        NotificationStage stage2 = new NotificationStage();
        stage2.setStage(PROCESSED);

        Notification notification1 = new Notification();
        notification1.setType(MESSAGE);
        notification1.setPerson(modelPerson1);
        notification1.setContent("Test content \nthe second line");
        notification1.setRemoteId("test-remote-id");
        notification1.addStage(stage1);
        notificationRepository.save(notification1);
        Long id = notification1.getId();
        entityManager.flush();

        Notification dbNotification = notificationRepository.findById(id).orElse(null);
        assertNotNull(dbNotification);
        dbNotification.addStage(stage2);
        notificationRepository.save(notification1);
        entityManager.flush();

        Notification dbUpdatedNotification = notificationRepository.findById(id).orElse(null);
        assertNotNull(dbUpdatedNotification);
        assertEquals(2, dbUpdatedNotification.getStages().size());

        NotificationStage dbStage1 = dbUpdatedNotification.getStages().get(0);
        NotificationStage dbStage2 = dbUpdatedNotification.getStages().get(1);

        assertEquals(stage1.getStage(), dbStage1.getStage());
        assertEquals(stage2.getStage(), dbStage2.getStage());

        assertNotNull(dbStage1.getCreated());
        assertNotNull(dbStage2.getCreated());

        assertEquals(dbStage1.getNotification(), dbStage2.getNotification());


    }

    @Test
    void persistNotificationWithTwoStages_ThenDeleteNotification_AndTryToFindNotificationAndStagesByIds() {
        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.11.12.13");
        modelPerson1.setEmail("user@server.ru");

        NotificationStage stage1 = new NotificationStage();
        stage1.setStage(RECEIVED);

        NotificationStage stage2 = new NotificationStage();
        stage2.setStage(PROCESSED);

        Notification notification1 = new Notification();
        notification1.setType(MESSAGE);
        notification1.setPerson(modelPerson1);
        notification1.setContent("Test content \nthe second line");
        notification1.setRemoteId("test-remote-id");
        notification1.addStage(stage1);
        notification1.addStage(stage2);
        notificationRepository.save(notification1);
        entityManager.flush();

        Long notificationId = notification1.getId();
        Long stage1Id = stage1.getId();
        Long stage2Id = stage2.getId();

        notificationRepository.deleteById(notificationId);
        entityManager.flush();

        Notification dbNotification = notificationRepository.findById(notificationId).orElse(null);
        NotificationStage dbStage1 = notificationStageRepository.findById(stage1Id).orElse(null);
        NotificationStage dbStage2 = notificationStageRepository.findById(stage2Id).orElse(null);

        assertNull(dbNotification);
        assertNull(dbStage1);
        assertNull(dbStage2);
    }
}