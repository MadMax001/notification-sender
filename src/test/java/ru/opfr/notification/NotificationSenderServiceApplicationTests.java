package ru.opfr.notification;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import ru.opfr.notification.reporitory.NotificationAttachmentRepository;
import ru.opfr.notification.reporitory.NotificationRepository;
import ru.opfr.notification.reporitory.NotificationStageRepository;
import ru.opfr.notification.service.*;
import ru.opfr.notification.converters.RequestFileConverter;
import ru.opfr.notification.converters.RequestNotificationConverter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NotificationSenderServiceApplicationTests {
	private final ApplicationContext context;
	@Test
	void contextLoads() {
		assertNotNull(context.getBean(NotificationAttachmentRepository.class));
		assertNotNull(context.getBean(NotificationRepository.class));
		assertNotNull(context.getBean(NotificationStageRepository.class));

		assertNotNull(context.getBeansOfType(SenderService.class));
		assertEquals(3, context.getBeansOfType(SenderService.class).entrySet().size());
		assertEquals(1, context.getBeansOfType(EmailSenderService.class).entrySet().size());
		assertNotNull(context.getBean(SMTPMailSender.class));
		assertNotNull(context.getBean(NotificationService.class));
		assertNotNull(context.getBean(NotificationStageService.class));
		assertNotNull(context.getBean(SenderServiceFacade.class));

		assertNotNull(context.getBean(RequestFileConverter.class));
		assertNotNull(context.getBean(RequestNotificationConverter.class));

		assertNotNull(context.getBean(NotificationSenderServiceConfiguration.class));
	}

}
