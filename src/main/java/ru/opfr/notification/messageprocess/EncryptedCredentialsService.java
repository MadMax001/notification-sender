package ru.opfr.notification.messageprocess;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.opfr.notification.exception.IllegalPropertyException;

import javax.annotation.PostConstruct;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
public class EncryptedCredentialsService implements CredentialsService{
    private final EncryptionService encryptionService;
    private final AppSecurityService appSecurityService;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private String username;
    private String password;

    @Override
    @PostConstruct
    public void setCredentials() throws Exception {
        String decryptedValue = encryptionService.decrypt(appSecurityService.getCredentials());
        String[] parts = decryptedValue.split(" ");
        if (parts.length != 2) {
            badCredentialsStructure();
        }
        setCredentialsFromArray(parts);

    }

    private void badCredentialsStructure() throws IllegalPropertyException {
        lock.writeLock().lock();
        try {
            username = null;
            password = null;
        } finally {
            lock.writeLock().unlock();
        }
        throw new IllegalPropertyException("The credentials has bad structure");
    }

    private void setCredentialsFromArray(String[] parts) {
        lock.writeLock().lock();
        try {
            username = parts[0];
            password = parts[1];
        } finally {
            lock.writeLock().unlock();
        }
    }


    @Override
    public String getUsername() {
        lock.readLock().lock();
        try {
            return username;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String getPassword() {
        lock.readLock().lock();
        try {
            return password;
        } finally {
            lock.readLock().unlock();
        }
    }
}
