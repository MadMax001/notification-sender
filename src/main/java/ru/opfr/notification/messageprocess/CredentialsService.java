package ru.opfr.notification.messageprocess;

public interface CredentialsService {
    void setCredentials() throws Exception;
    String getUsername();
    String getPassword();

}
