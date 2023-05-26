package ru.opfr.notification;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class NotificationSenderServiceApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(NotificationSenderServiceApplication.class)
				.run(args);
	}

}
