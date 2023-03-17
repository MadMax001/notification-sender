package ru.opfr.notification;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import ru.opfr.notification.reporitory.NotificationAttachmentRepository;
import ru.opfr.notification.reporitory.NotificationRepository;
import ru.opfr.notification.reporitory.NotificationStageRepository;
import ru.opfr.notification.service.NotificationService;
import ru.opfr.notification.service.NotificationStageService;
import ru.opfr.notification.service.SendNotificationFacade;
import ru.opfr.notification.service.SenderService;
import ru.opfr.notification.transformers.RequestFileTransformer;
import ru.opfr.notification.transformers.RequestNotificationTransformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
		assertNotNull(context.getBean(NotificationService.class));
		assertNotNull(context.getBean(NotificationStageService.class));
		assertNotNull(context.getBean(SendNotificationFacade.class));

		assertNotNull(context.getBean(RequestFileTransformer.class));
		assertNotNull(context.getBean(RequestNotificationTransformer.class));

		assertNotNull(context.getBean(NotificationSenderServiceConfiguration.class));
	}

}
