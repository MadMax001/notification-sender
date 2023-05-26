package ru.opfr.notification.messageprocess;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.opfr.notification.messageprocess.misc.SimpleAppSecurityService;

import javax.crypto.BadPaddingException;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedCredentialsServiceTest {

    private final String correctTestKey = "98873837a5e2f242c3358d7543abc7b34df6ea03e8d9c84c6ad4384b86e2e7e5";
    private final String correctTestCredentials = "tv3JMttX9S9nR9pM+JwNhg==";           //user1 pass1
    private final String incorrectTestCredentials = "mkCNPIOUrTslPN5HXld7ZQ==";

    private EncryptionService encryptionService;


    @BeforeEach
    void initEncryptionService() throws DecoderException {
        encryptionService = new AESEncryptionService(new SimpleAppSecurityService(correctTestKey));
    }

    @Test
    void setCorrectCredentials_AndGetUsernameAndPassword() throws Exception {
        AppSecurityService securityService = new SimpleAppSecurityService(correctTestKey, correctTestCredentials);
        CredentialsService credentialsService = new EncryptedCredentialsService(encryptionService, securityService);
        credentialsService.setCredentials();
        assertEquals("user1", credentialsService.getUsername());
        assertEquals("pass1", credentialsService.getPassword());
    }

    @Test
    void setIncorrectCredentials_AndThrowsException() {
        AppSecurityService securityService = new SimpleAppSecurityService(correctTestKey, incorrectTestCredentials);
        CredentialsService credentialsService = new EncryptedCredentialsService(encryptionService, securityService);
        assertThrows(BadPaddingException.class, credentialsService::setCredentials);
        assertNull(credentialsService.getUsername());
        assertNull(credentialsService.getPassword());
    }

}