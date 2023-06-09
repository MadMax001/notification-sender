package ru.opfr.notification.messageprocess;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class AppSecurityPropertiesContextConfiguration {
    private String key;
    private String credentials;
}
