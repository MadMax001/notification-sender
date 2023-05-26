/**
 * //https://github.com/stuinzuri/SimpleJavaKeyStore
 * //https://howtodoinjava.com/java/java-security/java-aes-lib-example/
 */


package ru.opfr.notification.messageprocess;

import org.springframework.stereotype.Service;
import org.apache.commons.codec.DecoderException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.apache.commons.codec.binary.Hex.decodeHex;


@Service
public class AESEncryptionService implements EncryptionService {
    private static final String ALGO = "AES";
    private static final String CIPHER_MODE = "AES/ECB/PKCS5Padding";
    private final SecretKey secretKey;

    public AESEncryptionService(AppSecurityService appSecurityService) throws DecoderException {
        char[] hex = appSecurityService.getKey().toCharArray();
        byte[] encoded = decodeHex(hex);
        secretKey = new SecretKeySpec(encoded, ALGO);
    }

    public String encrypt(String strToEncrypt)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));

    }

    public String decrypt(String strToDecrypt)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }
}
