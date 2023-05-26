package ru.opfr.notification.messageprocess;

public interface EncryptionService {
    String encrypt(String strToEncrypt) throws Exception;
    String decrypt(String strToDecrypt) throws Exception;

}
