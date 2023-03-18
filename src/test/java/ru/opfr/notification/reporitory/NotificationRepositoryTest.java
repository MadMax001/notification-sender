package ru.opfr.notification.reporitory;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationAttachment;
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

    private final NotificationAttachmentRepository notificationAttachmentRepository;
    private final TestEntityManager entityManager;

    @Test
    void persistNotificationWithoutStages_AndGetItByIdAfterFlush_AndCheckForNonNullCreatedField() {
        Person modelPerson = new Person();
        modelPerson.setUser("073User");
        modelPerson.setIp("10.73.12.13");
        modelPerson.setEmail("user@server.ru");

        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);

        Notification notification = new Notification();
        notification.setType(MESSAGE);
        notification.setPerson(modelPerson);
        notification.addStage(stage);
        notification.setContent("Test content \nthe second line");
        notification.setTheme("Theme");
        notification.setRemoteId("test-remote-id");
        notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(notification.getId());
        Notification dbNotification = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(notification.getId());

        assertNotNull(dbNotification);
        assertEquals(notification.getContent(), dbNotification.getContent());
        assertEquals(notification.getTheme(), dbNotification.getTheme());
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
        modelPerson.setIp("10.73.12.13");
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
        modelPerson1.setIp("10.73.12.13");
        modelPerson1.setEmail("user@server.ru");
        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);

        Notification notification1 = new Notification();
        notification1.setType(MESSAGE);
        notification1.setPerson(modelPerson1);
        notification1.setContent("Test content \nthe second line");
        notification1.setTheme("Theme");
        notification1.setRemoteId("test-remote-id");
        notification1.addStage(stage);
        notificationRepository.save(notification1);
        Long id = notification1.getId();
        entityManager.flush();

        Notification dbNotification = notificationRepository.findById(id).orElse(null);
        assertNotNull(dbNotification);

        Person modelPerson2 = new Person();
        modelPerson2.setUser("073User2");
        modelPerson2.setIp("10.73.16.17");
        modelPerson2.setEmail("user2@server2.ru");
        dbNotification.setType(EMAIL);
        dbNotification.setPerson(modelPerson2);
        dbNotification.setContent("Test content2");
        dbNotification.setTheme("Modified theme");
        dbNotification.setRemoteId("test-remote2-id");
        notificationRepository.save(dbNotification);
        entityManager.flush();

        Notification dbUpdatedNotification = notificationRepository.findById(id).orElse(null);

        assertNotNull(dbUpdatedNotification);
        assertEquals(dbNotification.getContent(), dbUpdatedNotification.getContent());
        assertEquals(dbNotification.getTheme(), dbUpdatedNotification.getTheme());
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
        modelPerson1.setIp("10.73.12.13");
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
        modelPerson1.setIp("10.73.12.13");
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

    @Test
    void persistNotificationWithTwoAttachedFiles_ThenLoadNotification_AndCheckFilesContent() {
        NotificationAttachment attachment1 = new NotificationAttachment();
        attachment1.setName("file1");
        attachment1.setContent("Content number 1".getBytes());

        NotificationAttachment attachment2 = new NotificationAttachment();
        attachment2.setName("file2");
        attachment2.setContent("Content number 2".getBytes());

        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.73.12.13");
        modelPerson1.setEmail("user@server.ru");

        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        notification1.setPerson(modelPerson1);
        notification1.setRemoteId("test-remote-id");
        notification1.setContent("text");
        NotificationStage stage1 = new NotificationStage();
        stage1.setStage(RECEIVED);
        notification1.addStage(stage1);

        notification1.addAttachment(attachment1);
        notification1.addAttachment(attachment2);

        notificationRepository.save(notification1);
        entityManager.flush();
        Long notificationId = notification1.getId();
        assertNotNull(attachment1.getId());
        assertNotNull(attachment2.getId());

        Notification dbNotification = notificationRepository.findById(notificationId).orElse(null);
        assertNotNull(dbNotification);
        assertEquals(2, dbNotification.getAttachments().size());
        NotificationAttachment dbAttachment1 = dbNotification.getAttachments().get(0);
        NotificationAttachment dbAttachment2 = dbNotification.getAttachments().get(1);

        assertNotNull(dbAttachment1);
        assertArrayEquals("Content number 1".getBytes(), dbAttachment1.getContent());
        assertEquals(dbNotification, dbAttachment1.getNotification());
        assertEquals("file1", dbAttachment1.getName());

        assertNotNull(dbAttachment2);
        assertArrayEquals("Content number 2".getBytes(), dbAttachment2.getContent());
        assertEquals(dbNotification, dbAttachment2.getNotification());
        assertEquals("file2", dbAttachment2.getName());

    }

    @Test
    void persistNotificationWithTwoAttachedFiles_ThenLoadOnlyFiles_AndCheckFilesContent() {
        NotificationAttachment attachment1 = new NotificationAttachment();
        attachment1.setName("file1");
        attachment1.setContent("Content number 1".getBytes());

        NotificationAttachment attachment2 = new NotificationAttachment();
        attachment2.setName("file2");
        attachment2.setContent("Content number 2".getBytes());

        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.73.12.13");
        modelPerson1.setEmail("user@server.ru");

        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        notification1.setPerson(modelPerson1);
        notification1.setRemoteId("test-remote-id");
        notification1.setContent("text");
        NotificationStage stage1 = new NotificationStage();
        stage1.setStage(RECEIVED);
        notification1.addStage(stage1);

        notification1.addAttachment(attachment1);
        notification1.addAttachment(attachment2);

        notificationRepository.save(notification1);
        entityManager.flush();

        NotificationAttachment dbAttachment1 = notificationAttachmentRepository.findById(attachment1.getId()).orElse(null);
        NotificationAttachment dbAttachment2 = notificationAttachmentRepository.findById(attachment2.getId()).orElse(null);

        assertNotNull(dbAttachment1);
        assertArrayEquals("Content number 1".getBytes(), dbAttachment1.getContent());
        assertEquals("file1", dbAttachment1.getName());

        assertNotNull(dbAttachment2);
        assertArrayEquals("Content number 2".getBytes(), dbAttachment2.getContent());
        assertEquals("file2", dbAttachment2.getName());
    }

    @Test
    void persistNotificationWithTwoAttachedFiles_ThenClearAttachments_AndCheckThatNoFileExists() {
        NotificationAttachment attachment1 = new NotificationAttachment();
        attachment1.setName("file1");
        attachment1.setContent("Content number 1".getBytes());

        NotificationAttachment attachment2 = new NotificationAttachment();
        attachment2.setName("file2");
        attachment2.setContent("Content number 2".getBytes());

        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.73.12.13");
        modelPerson1.setEmail("user@server.ru");

        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        notification1.setPerson(modelPerson1);
        notification1.setRemoteId("test-remote-id");
        notification1.setContent("text");
        NotificationStage stage1 = new NotificationStage();
        stage1.setStage(RECEIVED);
        notification1.addStage(stage1);

        notification1.addAttachment(attachment1);
        notification1.addAttachment(attachment2);

        notificationRepository.save(notification1);
        entityManager.flush();
        Long attachment1Id = attachment1.getId();
        Long attachment2Id = attachment2.getId();

        notification1.clearAttachments();
        notificationRepository.save(notification1);
        entityManager.flush();

        Notification dbNotification = notificationRepository.findById(notification1.getId()).orElse(null);
        assertNotNull(dbNotification);
        assertEquals(0, dbNotification.getAttachments().size());

        NotificationAttachment dbAttachment1 = notificationAttachmentRepository.findById(attachment1Id).orElse(null);
        NotificationAttachment dbAttachment2 = notificationAttachmentRepository.findById(attachment2Id).orElse(null);

        assertNull(dbAttachment1);
        assertNull(dbAttachment2);
    }

    @Test
    void persistNotificationWithoutAttachedFiles_ThenClearAttachments_AndCheckThatNoFileExists() {

        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.73.12.13");
        modelPerson1.setEmail("user@server.ru");

        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        notification1.setPerson(modelPerson1);
        notification1.setRemoteId("test-remote-id");
        notification1.setContent("text");
        NotificationStage stage1 = new NotificationStage();
        stage1.setStage(RECEIVED);
        notification1.addStage(stage1);

        notificationRepository.save(notification1);
        entityManager.flush();

        notification1.clearAttachments();
        notificationRepository.save(notification1);
        entityManager.flush();

        Notification dbNotification = notificationRepository.findById(notification1.getId()).orElse(null);
        assertNotNull(dbNotification);
        assertEquals(0, dbNotification.getAttachments().size());
    }
}