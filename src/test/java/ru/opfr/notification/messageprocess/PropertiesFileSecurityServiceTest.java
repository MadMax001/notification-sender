package ru.opfr.notification.messageprocess;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = AppSecurityPropertiesContextConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class PropertiesFileSecurityServiceTest {

    private final AppSecurityPropertiesContextConfiguration securityProperties;

    @Test
    void checkSecurityKeyInPropertiesFile() {
        assertNotNull(securityProperties.getKey());
        assertNotEquals("", securityProperties.getKey());
    }

    @Test
    void checkSecurityCredentialsInPropertiesFile() {
        assertNotNull(securityProperties.getCredentials());
        assertNotEquals("", securityProperties.getCredentials());
    }

    @Test
    void checkForGetMethods() {
        PropertiesFileSecurityService securityService = new PropertiesFileSecurityService("key1", "credentials1");
        assertEquals("key1", securityService.getKey());
        assertEquals("credentials1", securityService.getCredentials());
    }

    @Test
    void checkForValidKeyCredentialsPair() throws Exception {
        PropertiesFileSecurityService securityService = new PropertiesFileSecurityService(
                securityProperties.getKey(), securityProperties.getCredentials());
        EncryptionService encryptionService = new AESEncryptionService(securityService);
        CredentialsService credentialsService = new EncryptedCredentialsService(encryptionService, securityService);
        credentialsService.setCredentials();
        assertNotNull(credentialsService.getUsername());
        assertNotNull(credentialsService.getPassword());
    }
}