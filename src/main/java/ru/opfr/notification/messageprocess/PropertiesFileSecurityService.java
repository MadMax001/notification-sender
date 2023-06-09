package ru.opfr.notification.messageprocess;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PropertiesFileSecurityService implements AppSecurityService {

    private final String key;

    private final String credentials;

    public PropertiesFileSecurityService(@Value("${app.security.key}") String key,
                                         @Value("${app.security.credentials}") String credentials) {
        this.key = key;
        this.credentials = credentials;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }
}
