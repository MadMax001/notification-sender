package ru.opfr.notification.messageprocess;

import org.junit.jupiter.api.Test;
import org.apache.commons.codec.DecoderException;
import ru.opfr.notification.messageprocess.misc.SimpleAppSecurityService;

import javax.crypto.BadPaddingException;
import java.security.InvalidKeyException;

import static org.junit.jupiter.api.Assertions.*;


class AESEncryptionServiceTest {

    private final String correctTestKey = "98873837a5e2f242c3358d7543abc7b34df6ea03e8d9c84c6ad4384b86e2e7e5";
    private final String correctTestKey2 = "c12a5a50e469ea3cdb2dd9936685db45986c3a71cc6d38bf6ffc7a372d27c048";
    private final String incorrectOddTestKey = "00000";
    private final String incorrectEvenTestKey = "000000";

    @Test
    void setCorrectSecretKey_AndDoesNotThrowException() {
        assertDoesNotThrow(() -> new AESEncryptionService(new SimpleAppSecurityService(correctTestKey)));
    }

    @Test
    void setIncorrectOddSecretKey_AndThrowException() {
        assertThrows(DecoderException.class,
                () -> new AESEncryptionService(new SimpleAppSecurityService(incorrectOddTestKey)));
    }

    @Test
    void setIncorrectEvenSecretKey_AndDoesNotThrowException() {
        assertDoesNotThrow(
                () -> new AESEncryptionService(new SimpleAppSecurityService(incorrectEvenTestKey)));
    }

    @Test
    void encryptByCorrectKey_AndDoesNotThrowException() throws Exception {
        String testStr = "Hello world";
        EncryptionService service = new AESEncryptionService(new SimpleAppSecurityService(correctTestKey));
        String encryptedString = service.encrypt(testStr);
        assertNotNull(encryptedString);
    }

    @Test
    void encryptByIncorrectEvenKey_AndThrowException() throws Exception {
        final String testStr = "Hello world";
        EncryptionService service = new AESEncryptionService(new SimpleAppSecurityService(incorrectEvenTestKey));
        assertThrows(InvalidKeyException.class, () -> service.encrypt(testStr));
    }


    @Test
    void decodeIncorrectString_WithCorrectKey_AndThrowException() throws Exception {
        String testStr = "Hello world";
        EncryptionService service = new AESEncryptionService(new SimpleAppSecurityService(correctTestKey));
        assertThrows(Exception.class, () -> service.decrypt(testStr));
    }

    @Test
    void encryptAndDecryptString() throws Exception {
        String testStr = "Hello world";
        EncryptionService service = new AESEncryptionService(new SimpleAppSecurityService(correctTestKey));

        String encryptedString = service.encrypt(testStr);
        assertNotEquals(testStr, encryptedString);

        String decryptedString = service.decrypt(encryptedString);
        assertEquals(testStr, decryptedString);
    }

    @Test
    void encryptByCorrectKey_AndDecryptByIncorrectEvenKey_AndThrowException() throws Exception {
        String testStr = "Hello world";
        EncryptionService service1 = new AESEncryptionService(new SimpleAppSecurityService(correctTestKey));

        final String encryptedString = service1.encrypt(testStr);

        EncryptionService service2 = new AESEncryptionService(new SimpleAppSecurityService(incorrectEvenTestKey));
        assertThrows(InvalidKeyException.class, () -> service2.decrypt(encryptedString));
    }

    @Test
    void encryptByCorrectKey_AndDecryptByAnotherCorrectEvenKey_AndAssertNotEqualsResult() throws Exception {
        String testStr = "Hello world";
        EncryptionService service1 = new AESEncryptionService(new SimpleAppSecurityService(correctTestKey));

        final String encryptedString = service1.encrypt(testStr);

        EncryptionService service2 = new AESEncryptionService(new SimpleAppSecurityService(correctTestKey2));
        assertThrows(BadPaddingException.class, () -> service2.decrypt(encryptedString));

        service2.encrypt(testStr);
    }

}