package ru.opfr.notification.reporitory;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.AbstractContainersIntegrationTest;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationAttachment;
import ru.opfr.notification.model.NotificationStage;
import ru.opfr.notification.model.Person;

import javax.validation.ConstraintViolationException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.RECEIVED;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("repo_test")
class ValidatorForNotificationRepositoryIT extends AbstractContainersIntegrationTest {

    private final NotificationRepository notificationRepository;
    private final TestEntityManager entityManager;

    @Test
    void tryToPersistNotificationWithNullType_AndThrowException() {
        Person modelPerson1 = getPersonWithAllFields();

        Notification notification1 = new Notification();
        notification1.setType(null);
        notification1.addStage(getStageForReceiving());
        notification1.setPerson(modelPerson1);
        notification1.setContent("Test content \nthe second line");
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
        notification1.setContent(null);
        notification1.setRemoteId("test-remote-id");

        notificationRepository.save(notification1);
        assertThrows(ConstraintViolationException.class, entityManager::flush);

    }

    @Nested
    class CollectionValidation {
        @Test
        void tryToPersistNotificationWithNullStageCollections_AndThrowException() {
            Person modelPerson1 = getPersonWithAllFields();
            Notification notification1 = new Notification();
            notification1.setType(EMAIL);
            notification1.setStages(null);
            notification1.setPerson(modelPerson1);
            notification1.setContent("Test content \nthe second line");
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
            notification1.setContent("Test content \nthe second line");
            notification1.setRemoteId("test-remote-id");

            notificationRepository.save(notification1);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }

        @Test
        void tryToPersistNotificationWithMoreThan5Attachments_AndThrowException() {
            Person modelPerson1 = getPersonWithAllFields();
            Notification notification1 = new Notification();
            notification1.setType(EMAIL);
            notification1.setPerson(modelPerson1);
            notification1.setContent("Test content \nthe second line");
            notification1.setRemoteId("test-remote-id");

            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification1.addStage(stage);

            for (int i = 0; i <= 6; i++) {
                NotificationAttachment attachment = new NotificationAttachment();
                attachment.setName("file" + i);
                attachment.setContent(("Content number " + i).getBytes());
                notification1.addAttachment(attachment);
            }

            notificationRepository.save(notification1);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }

        @Test
        void tryToPersistNotificationWithNullFieldAttachments_AndThrowException() {
            Person modelPerson1 = getPersonWithAllFields();
            Notification notification1 = new Notification();
            notification1.setType(EMAIL);
            notification1.setPerson(modelPerson1);
            notification1.setContent("Test content \nthe second line");
            notification1.setRemoteId("test-remote-id");

            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification1.addStage(stage);

            notification1.setAttachments(null);

            notificationRepository.save(notification1);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }
    }


    @Nested
    class TypeAndPersonFieldValidation {
        @Test
        void tryToPersistNotificationWithMessageType_AndNullIp_AndThrowException() {
            Person modelPerson1 = getPersonWithAllFields();
            modelPerson1.setIp(null);

            Notification notification1 = new Notification();
            notification1.setType(MESSAGE);
            notification1.addStage(getStageForReceiving());
            notification1.setPerson(modelPerson1);
            notification1.setContent("Content");
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
            notification1.setContent("Content");
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
            notification.setContent("Test content \nthe second line");
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
            notification1.setContent("Content");
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
            notification.setContent("Test content \nthe second line");
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
            notification.setContent("Test content \nthe second line");
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
            notification1.setContent("Content");
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
            notification.setContent("Test content \nthe second line");
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
            notification.setContent("Test content \nthe second line");
            notification.setRemoteId("test-remote-id");
            notificationRepository.save(notification);
            entityManager.flush();

            assertNotNull(notification.getId());
            Notification dbNotification = notificationRepository.findById(notification.getId()).orElse(null);
            assertNotNull(dbNotification);
        }
    }

    @Nested
    class ThemeValidation {
        @Test
        void persistNotificationWithNullTheme() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setTheme(null);
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(MESSAGE);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);

        }

        @Test
        void persistNotificationWithEmptyTheme() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setTheme("");
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(MESSAGE);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);
        }

        @Test
        void persistNotificationWithThemeWithLengthMoreThan255_AndThrowException() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setTheme("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(MESSAGE);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            notificationRepository.save(notification);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }
    }

    @Nested
    class RemoteIdValidation {
        @Test
        void persistNotificationWithNullRemoteId() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setRemoteId(null);
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(MESSAGE);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);

        }

        @Test
        void persistNotificationWithEmptyRemoteId() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setRemoteId("");
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(MESSAGE);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);
        }

        @Test
        void persistNotificationWithRemoteIdWithLengthMoreThan255_AndThrowException() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setRemoteId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(MESSAGE);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            notificationRepository.save(notification);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }
    }


    @Nested
    class PersonValidation {
        @Test
        void setCorrectPerson() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setTheme("Theme");
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(MESSAGE);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);
        }

        @Nested
        class User {
            @Test
            void setUserWithLengthMoreThan255_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setUser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

            @Test
            void setNullUser() {
                Person person = getPersonWithAllFields();
                person.setUser(null);
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(EMAIL);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                Notification savedNotification = notificationRepository.save(notification);
                entityManager.flush();
                assertNotNull(savedNotification);
            }
        }

        @Nested
        class IP {
            @Test
            void setIpWithLengthMoreThan16_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setIp("12345678901234567890");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

            @Test
            void setIpWithWrongLettersPrefix_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setIp("ab.73.30.40");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }


            @Test
            void setIpWithWrongDigitPrefix_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setIp("125.20.30.40");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }
            @Test
            void setIpWithWrongDigitPrefixInSecondSection_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setIp("10.20.30.40");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

            @Test
            void setIpWithCorrectPrefix_AndZeroValueInThirdSection() {
                Person person = getPersonWithAllFields();
                person.setIp("10.73.0.40");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                Notification savedNotification = notificationRepository.save(notification);
                entityManager.flush();
                assertNotNull(savedNotification);
            }

            @Test
            void setIpWithCorrectPrefix_AndValueMoreThen255InThirdSection_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setIp("10.73.300.40");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

            @Test
            void setIpWithCorrectPrefix_AndZeroValueInForthSection() {
                Person person = getPersonWithAllFields();
                person.setIp("10.73.30.0");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                Notification savedNotification = notificationRepository.save(notification);
                entityManager.flush();
                assertNotNull(savedNotification);
            }

            @Test
            void setIpWithCorrectPrefix_AndValueMoreThen255InForthSection_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setIp("10.73.30.400");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

            @Test
            void setCorrectIp() {
                Person person = getPersonWithAllFields();
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                Notification savedNotification = notificationRepository.save(notification);
                entityManager.flush();
                assertNotNull(savedNotification);
            }

            @Test
            void setEmptyNotNullIp() {
                Person person = getPersonWithAllFields();
                person.setIp("");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                Notification savedNotification = notificationRepository.save(notification);
                entityManager.flush();
                assertNotNull(savedNotification);
            }

            @Test
            void setNullIp() {
                Person person = getPersonWithAllFields();
                person.setIp(null);
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(EMAIL);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                Notification savedNotification = notificationRepository.save(notification);
                entityManager.flush();
                assertNotNull(savedNotification);
            }
        }

        @Nested
        class Email {
            @Test
            void setEmailWithLengthMoreThan255_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.aaa");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

            @Test
            void setEmptyEmail_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setEmail("");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                Notification savedNotification = notificationRepository.save(notification);
                entityManager.flush();
                assertNotNull(savedNotification);
            }

            @Test
            void setNullEmail_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setEmail(null);
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                Notification savedNotification = notificationRepository.save(notification);
                entityManager.flush();
                assertNotNull(savedNotification);
            }

            @Test
            void setWrongEmail_WithoutDomain_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setEmail("username@server");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

            @Test
            void setWrongEmail_WithoutUserAndAt_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setEmail("server.ru");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

            @Test
            void setWrongEmail_WithoutUser_AndThrowException() {
                Person person = getPersonWithAllFields();
                person.setEmail("@server.ru");
                Notification notification = new Notification();
                notification.setTheme("Theme");
                notification.setContent("Content");
                notification.setPerson(person);
                notification.setType(MESSAGE);
                NotificationStage stage = new NotificationStage();
                stage.setStage(RECEIVED);
                notification.addStage(stage);
                notificationRepository.save(notification);
                assertThrows(ConstraintViolationException.class, entityManager::flush);
            }

        }
    }

    @Nested
    class InnerFieldsInStageValidation {
        ThreadLocal<Notification> storedNotification;
        @BeforeEach
        void setUp() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setTheme("Theme");
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(MESSAGE);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);
            storedNotification = new ThreadLocal<>();
            storedNotification.set(notification);

        }

        @Test
        void addNotificationStageWithNullMessage() {
            Notification notification = notificationRepository.findById(storedNotification.get().getId()).orElse(null);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            stage.setMessage(null);
            assertNotNull(notification);
            notification.addStage(stage);

            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);

        }

        @Test
        void addNotificationStageWithEmptyMessage() {
            Notification notification = notificationRepository.findById(storedNotification.get().getId()).orElse(null);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            stage.setMessage("");
            assertNotNull(notification);
            notification.addStage(stage);
            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);
        }

        @Test
        void addNotificationStageWithMessage_WithLengthMoreThan255_AndThrowException() {
            Notification notification = notificationRepository.findById(storedNotification.get().getId()).orElse(null);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            stage.setMessage("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            assertNotNull(notification);
            notification.addStage(stage);

            notificationRepository.save(notification);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }

        @Test
        void addNotificationStageWithNullStageType_AndThrowException() {
            Notification notification = notificationRepository.findById(storedNotification.get().getId()).orElse(null);
            NotificationStage stage = new NotificationStage();
            stage.setStage(null);
            assertNotNull(notification);
            notification.addStage(stage);

            notificationRepository.save(notification);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }

    }

    @Nested
    class InnerFieldsInAttachmentValidation {
        ThreadLocal<Notification> storedNotification;
        @BeforeEach
        void setUp() {
            Person person = getPersonWithAllFields();
            Notification notification = new Notification();
            notification.setTheme("Theme");
            notification.setContent("Content");
            notification.setPerson(person);
            notification.setType(EMAIL);
            NotificationStage stage = new NotificationStage();
            stage.setStage(RECEIVED);
            notification.addStage(stage);
            Notification savedNotification = notificationRepository.save(notification);
            entityManager.flush();
            assertNotNull(savedNotification);
            storedNotification = new ThreadLocal<>();
            storedNotification.set(notification);
        }

        @Test
        void persistToAddFileWithNullFilename_AntThrowException() {
            Notification notification = notificationRepository.findById(storedNotification.get().getId()).orElse(null);
            assertNotNull(notification);
            NotificationAttachment attachment1 = new NotificationAttachment();
            attachment1.setName(null);
            attachment1.setContent("Content number 1".getBytes());
            notification.addAttachment(attachment1);

            notificationRepository.save(notification);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }

        @Test
        void persistToAddFileWithFilename_WithLengthMoreThan255_AntThrowException() {
            Notification notification = notificationRepository.findById(storedNotification.get().getId()).orElse(null);
            assertNotNull(notification);
            NotificationAttachment attachment1 = new NotificationAttachment();
            attachment1.setName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            attachment1.setContent("Content number 1".getBytes());
            notification.addAttachment(attachment1);

            notificationRepository.save(notification);
            assertThrows(ConstraintViolationException.class, entityManager::flush);
        }
    }


    private static Person getPersonWithAllFields() {
        Person modelPerson1 = new Person();
        modelPerson1.setUser("073User");
        modelPerson1.setIp("10.73.12.13");
        modelPerson1.setEmail("user@server.ru");
        return modelPerson1;
    }

    private  NotificationStage getStageForReceiving() {
        NotificationStage stage = new NotificationStage();
        stage.setStage(RECEIVED);
        return stage;
    }

}