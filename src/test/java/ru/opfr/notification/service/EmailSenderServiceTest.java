package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.*;

import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.PersistenceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.EMAIL;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("repo_test")
class EmailSenderServiceTest {
    final EmailSenderService emailSenderService;

    @MockBean
    final NotificationService notificationService;
    @MockBean
    SMTPMailSender mailSender;

    @Captor
    ArgumentCaptor<MimeMessage> mimeMessageCaptor;
    @Captor
    ArgumentCaptor<Session> sessionCaptor;

    @Test
    void checkType() {
        assertEquals(EMAIL, emailSenderService.getType());
    }

    @Nested
    class afterSendingTests {
        @Test
        void afterSendingRemovesAllAttachments_IfSuccessfulSend() throws SendNotificationException {
            boolean success = true;

            Notification notification = new Notification();
            notification.setRemoteId("remote-id");
            notification.setContent("Content");
            notification.setType(EMAIL);
            notification.setPerson(new Person());
            notification.addStage(new NotificationStage());
            notification.addAttachment(new NotificationAttachment());

            emailSenderService.afterSending(notification, success);
            verify(notificationService).deleteAllAttachments(notification);

        }

        @Test
        void afterSendingDoesNotRemoveAttachments_IfUnSuccessfulSend() throws SendNotificationException {
            boolean success = false;

            Notification notification = new Notification();
            notification.setRemoteId("remote-id");
            notification.setContent("Content");
            notification.setType(EMAIL);
            notification.setPerson(new Person());
            notification.addStage(new NotificationStage());
            notification.addAttachment(new NotificationAttachment());

            emailSenderService.afterSending(notification, success);
            verify(notificationService, never()).deleteAllAttachments(notification);
        }

        @Test
        void afterSendingThrowExceptionInPersist_ThenTrowSendNotificationException() {
            doThrow(PersistenceException.class).when(notificationService).deleteAllAttachments(any(Notification.class));
            boolean success = true;

            Notification notification = new Notification();
            notification.setRemoteId("remote-id");
            notification.setContent("Content");
            notification.setType(EMAIL);
            notification.setPerson(new Person());
            notification.addStage(new NotificationStage());
            notification.addAttachment(new NotificationAttachment());

            assertThrows(SendNotificationException.class, () -> emailSenderService.afterSending(notification, success));
        }

    }
    @Test
    void sendMailWithoutAttachments_And200CodeReturn_AndCheckTrueResult() throws SendNotificationException, MessagingException, IOException {
        int answerCode = 200;
        String answerMessage = "message";
        when(mailSender.send(any(Session.class), any(Message.class)))
                .thenReturn(new SMTPServerAnswer(answerCode, answerMessage));

        Person person = new Person();
        person.setEmail("user@server.ru");
        Notification notification = new Notification();
        notification.setPerson(person);
        notification.setType(EMAIL);
        notification.setTheme("Theme of message");
        notification.setContent("Content of letter");

        boolean result = emailSenderService.send(notification);
        assertTrue(result);
        assertTrue(emailSenderService.getSendingResultMessage().contains(answerMessage));
        assertTrue(emailSenderService.getSendingResultMessage().contains(String.valueOf(answerCode)));
        verify(mailSender).send(sessionCaptor.capture(), mimeMessageCaptor.capture());

        MimeMessage emailMessage = mimeMessageCaptor.getValue();
        Session session = sessionCaptor.getValue();

        assertEquals("10.73.0.0", session.getProperty("mail.smtp.host"));
        assertEquals("25", session.getProperty("mail.smtp.port"));
        assertEquals("true", session.getProperty("mail.smtp.auth"));

        assertEquals(1, emailMessage.getAllRecipients().length);
        assertEquals(notification.getPerson().getEmail(), ((InternetAddress)(emailMessage.getAllRecipients()[0])).getAddress());
        assertEquals(1, emailMessage.getFrom().length);
        assertEquals("owner@server.info", ((InternetAddress)(emailMessage.getFrom()[0])).getAddress());
        assertEquals(notification.getTheme(), emailMessage.getSubject());
        assertEquals(1, ((MimeMultipart)emailMessage.getContent()).getCount());
        BodyPart textPart = ((MimeMultipart)emailMessage.getContent()).getBodyPart(0);
        assertEquals("Content of letter", textPart.getContent());

    }

    @Test
    void sendMailWithoutAttachments_And300CodeReturn_AndCheckFalseResult() throws SendNotificationException, MessagingException {
        int answerCode = 300;
        String answerMessage = "message";
        when(mailSender.send(any(Session.class), any(Message.class)))
                .thenReturn(new SMTPServerAnswer(answerCode, answerMessage));

        Person person = new Person();
        person.setEmail("user@server.ru");
        Notification notification = new Notification();
        notification.setPerson(person);
        notification.setType(EMAIL);
        notification.setTheme("Theme of message");
        notification.setContent("Content of letter");

        boolean result = emailSenderService.send(notification);
        assertFalse(result);
    }

    @Test
    void sendMailWithoutAttachments_AndThrowExceptionInSending() throws MessagingException {
        Person person = new Person();
        person.setEmail("user@server.ru");
        Notification notification = new Notification();
        notification.setPerson(person);
        notification.setType(EMAIL);
        notification.setTheme("Theme of message");
        notification.setContent("Content of letter");

        doThrow(new MessagingException("error"){}).when(mailSender).send(any(Session.class), any(Message.class));
        assertThrows(SendNotificationException.class, () -> emailSenderService.send(notification));
    }

    @Test
    void sendMailWithAttachment_And200CodeReturn_AndCheckTrueResult() throws SendNotificationException, MessagingException, IOException {
        int answerCode = 200;
        String answerMessage = "message";
        when(mailSender.send(any(Session.class), any(Message.class)))
                .thenReturn(new SMTPServerAnswer(answerCode, answerMessage));

        Person person = new Person();
        person.setEmail("user@server.ru");
        Notification notification = new Notification();
        notification.setPerson(person);
        notification.setType(EMAIL);
        notification.setTheme("Theme of message");
        notification.setContent("Content of letter");

        NotificationAttachment attachment = new NotificationAttachment();
        attachment.setName("test_file");
        byte[] fileContentByteArray = "Content text\nSecond line".getBytes();
        attachment.setContent(fileContentByteArray);
        notification.addAttachment(attachment);

        boolean result = emailSenderService.send(notification);
        assertTrue(result);
        assertTrue(emailSenderService.getSendingResultMessage().contains(answerMessage));
        assertTrue(emailSenderService.getSendingResultMessage().contains(String.valueOf(answerCode)));
        verify(mailSender).send(any(Session.class), mimeMessageCaptor.capture());

        MimeMessage emailMessage = mimeMessageCaptor.getValue();

        assertEquals(1, emailMessage.getAllRecipients().length);
        assertEquals(notification.getPerson().getEmail(), ((InternetAddress)(emailMessage.getAllRecipients()[0])).getAddress());
        assertEquals(1, emailMessage.getFrom().length);
        assertEquals("owner@server.info", ((InternetAddress)(emailMessage.getFrom()[0])).getAddress());
        assertEquals(notification.getTheme(), emailMessage.getSubject());
        assertEquals(2, ((MimeMultipart)emailMessage.getContent()).getCount());
        BodyPart textPart = ((MimeMultipart)emailMessage.getContent()).getBodyPart(0);
        BodyPart filePart = ((MimeMultipart)emailMessage.getContent()).getBodyPart(1);
        assertEquals("Content of letter", textPart.getContent());
        assertEquals("test_file",filePart.getFileName());
        DataSource fileDataSource = filePart.getDataHandler().getDataSource();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileDataSource.getInputStream()))) {
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        assertEquals("Content textSecond line", sb.toString());

    }

}