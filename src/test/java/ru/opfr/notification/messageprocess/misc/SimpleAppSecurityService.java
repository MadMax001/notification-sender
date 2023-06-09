package ru.opfr.notification.messageprocess.misc;

import ru.opfr.notification.messageprocess.AppSecurityService;

public class SimpleAppSecurityService implements AppSecurityService {
    private final String key;
    private final String credentials;

    public SimpleAppSecurityService(String key, String credentials) {
        this.key = key;
        this.credentials = credentials;
    }

    public SimpleAppSecurityService(String key) {
        this(key, null);
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

