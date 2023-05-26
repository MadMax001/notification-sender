package ru.opfr.notification.messageprocess;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.opfr.notification.WinCredentialsManagerStoreChecker;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.Person;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = AppSecurityPropertiesContextConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AsyncWinMessageServiceImplTest {
    final AppSecurityPropertiesContextConfiguration securityProperties;
    AsyncWinMessageService messageService;
    CredentialsService credentialsService;
    WinConsoleCommandService commandService;
    @Mock
    WinCredentialsManagerStoreChecker checker;

    static String OSEnvValue;

    @BeforeAll
    static void beforeAll() {
        OSEnvValue = System.getenv().get("OS");
    }

    @BeforeEach
    void setUp() throws Exception {
        when(checker.isCredentialExists()).thenReturn(false);
        PropertiesFileSecurityService securityService = new PropertiesFileSecurityService(
                securityProperties.getKey(), securityProperties.getCredentials());
        EncryptionService encryptionService = new AESEncryptionService(securityService);
        credentialsService = new EncryptedCredentialsService(encryptionService, securityService);
        credentialsService.setCredentials();
        commandService = new WinConsoleCommandServiceImpl();
        System.setProperty("os.name", OSEnvValue);
    }

    @Test
    void checkForRunningOnAnotherOS_AndThrowException() {
        System.setProperty("os.name", "MacOS");
        messageService = new AsyncWinMessageServiceImpl(credentialsService, commandService, checker);
        Notification notification = new Notification();
        assertThrows(ApplicationRuntimeException.class, () -> messageService.send(notification));
    }

    @Test
    void sendCorrectMessage() throws IOException, InterruptedException, ExecutionException {
        Notification notification = new Notification();
        notification.setRemoteId("remote-id");
        notification.setContent("Тест");
        notification.setType(MESSAGE);

        Person person = new Person();
        person.setUser("073BodrovMB");
        person.setIp("10.73.3.11");
        person.setEmail("user@server.ru");
        notification.setPerson(person);

        messageService = new AsyncWinMessageServiceImpl(credentialsService, commandService, checker);
        WinConsoleExecuteResponse response = messageService.send(notification).get();
        assertEquals(0, response.getExitCode());
        assertEquals(0, response.getConsoleStdOut().size());
        assertEquals(0, response.getConsoleErrOut().size());

    }

}