package ru.opfr.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationSenderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationSenderServiceApplication.class, args);
	}

}
