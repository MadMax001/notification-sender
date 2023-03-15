package ru.opfr.notification.reporitory;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationStage;
import ru.opfr.notification.model.Person;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("repo_test")
class ValidatorForNotificationRepositoryTest {

    private final NotificationRepository notificationRepository;
    private final EntityManager entityManager;


    @Test
    void tryToPersistNotificationWithNullType_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();

        Notification notification1 = new Notification();
        notification1.setType(null);
        notification1.addStage(getStageForReceiving());
        notification1.setPerson(modelPerson1);
        notification1.setMessage("Test content \nthe second line");
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);
    }



    @Test
    void tryToPersistNotificationWithNullStageCollections_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();
        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        notification1.setStages(null);
        notification1.setPerson(modelPerson1);
        notification1.setMessage("Test content \nthe second line");
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);
    }

    @Test
    void tryToPersistNotificationWithEmptyStageCollections_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();
        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        notification1.setStages(Collections.emptyList());
        notification1.setPerson(modelPerson1);
        notification1.setMessage("Test content \nthe second line");
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);
    }

    @Test
    void tryToPersistNotificationWithOneStage_WithNullStageValue_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();
        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        NotificationStage stage = new NotificationStage();
        stage.setStage(null);
        notification1.addStage(stage);
        notification1.setPerson(modelPerson1);
        notification1.setMessage("Test content \nthe second line");
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);
    }

    @Test
    void tryToPersistNotificationWithNullContent_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();
        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        notification1.addStage(getStageForReceiving());
        notification1.setPerson(modelPerson1);
        notification1.setMessage(null);
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);

    }

    @Test
    void tryToPersistNotificationWithMessageType_AndNullIp_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();
        modelPerson1.setIp(null);

        Notification notification1 = new Notification();
        notification1.setType(MESSAGE);
        notification1.addStage(getStageForReceiving());
        notification1.setPerson(modelPerson1);
        notification1.setMessage("Content");
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);
    }

    @Test
    void tryToPersistNotificationWithMessageType_AndNullUser_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();
        modelPerson1.setUser(null);

        Notification notification1 = new Notification();
        notification1.setType(MESSAGE);
        notification1.addStage(getStageForReceiving());
        notification1.setPerson(modelPerson1);
        notification1.setMessage("Content");
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);
    }

    @Test
    void persistNotificationWithMessageType_AndNullEmail() {
        Person modelPerson = getPersonWithAllFields();
        modelPerson.setEmail(null);
        Notification notification = new Notification();
        notification.setType(MESSAGE);
        notification.addStage(getStageForReceiving());
        notification.setPerson(modelPerson);
        notification.setMessage("Test content \nthe second line");
        notification.setRemoteId("test-remote-id");
        notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(notification.getId());
        Notification dbNotification = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(dbNotification);
    }

    @Test
    void tryToPersistNotificationWithEmailType_AndNullEmail_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();
        modelPerson1.setEmail(null);
        Notification notification1 = new Notification();
        notification1.setType(EMAIL);
        notification1.addStage(getStageForReceiving());
        notification1.setPerson(modelPerson1);
        notification1.setMessage("Content");
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);

    }

    @Test
    void persistNotificationWithEmailType_AndNullUser() {
        Person modelPerson = getPersonWithAllFields();
        modelPerson.setUser(null);
        Notification notification = new Notification();
        notification.setType(EMAIL);
        notification.addStage(getStageForReceiving());
        notification.setPerson(modelPerson);
        notification.setMessage("Test content \nthe second line");
        notification.setRemoteId("test-remote-id");
        notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(notification.getId());
        Notification dbNotification = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(dbNotification);
    }

    @Test
    void persistNotificationWithEmailType_AndNullIp() {
        Person modelPerson = getPersonWithAllFields();
        modelPerson.setIp(null);

        Notification notification = new Notification();
        notification.setType(EMAIL);
        notification.addStage(getStageForReceiving());
        notification.setPerson(modelPerson);
        notification.setMessage("Test content \nthe second line");
        notification.setRemoteId("test-remote-id");
        notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(notification.getId());
        Notification dbNotification = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(dbNotification);
    }

    @Test
    void tryToPersistNotificationWithFileType_AndNullEmail_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();
        modelPerson1.setEmail(null);

        Notification notification1 = new Notification();
        notification1.setType(FILE);
        notification1.addStage(getStageForReceiving());
        notification1.setPerson(modelPerson1);
        notification1.setMessage("Content");
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);

    }

    @Test
    void persistNotificationWithFileType_AndNullUser() {
        Person modelPerson = getPersonWithAllFields();
        modelPerson.setUser(null);

        Notification notification = new Notification();
        notification.setType(FILE);
        notification.addStage(getStageForReceiving());
        notification.setPerson(modelPerson);
        notification.setMessage("Test content \nthe second line");
        notification.setRemoteId("test-remote-id");
        notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(notification.getId());
        Notification dbNotification = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(dbNotification);
    }

    @Test
    void persistNotificationWithFileType_AndNullIp() {
        Person modelPerson = getPersonWithAllFields();
        modelPerson.setIp(null);

        Notification notification = new Notification();
        notification.setType(FILE);
        notification.addStage(getStageForReceiving());
        notification.setPerson(modelPerson);
        notification.setMessage("Test content \nthe second line");
        notification.setRemoteId("test-remote-id");
        notificationRepository.save(notification);
        entityManager.flush();

        assertNotNull(notification.getId());
        Notification dbNotification = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(dbNotification);
    }

    private static Person getPersonWithAllFields() {
        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.11.12.13");
        modelPerson1.setEmail("user@server.ru");
        return modelPerson1;
    }

    private  NotificationStage getStageForReceiving() {
        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);
        return stage;
    }

}