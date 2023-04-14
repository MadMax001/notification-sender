package ru.opfr.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.opfr.notification.aspects.LogError;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationAttachment;
import ru.opfr.notification.model.SMTPServerAnswer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

@Service
public class EmailAnswerableSenderService extends EmailSenderService {
    private final SMTPMailSender mailSender;

    public EmailAnswerableSenderService(NotificationService notificationService, SMTPMailSender mailSender) {
        super(notificationService);
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private String port;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String smtpAuth;
    @Value("${app.mail.from}")
    private String mailFrom;

    private Properties connectionProperties;
    Authenticator authenticator;

    @PostConstruct
    private void setupConnectionProperties() {
        connectionProperties = System.getProperties();
        connectionProperties.put("mail.smtp.host", host);
        connectionProperties.put("mail.smtp.port", port);
        connectionProperties.put("mail.smtp.auth", smtpAuth);

        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    @Override
    @LogError(values = {SendNotificationException.class})
    public boolean send(Notification notification) throws SendNotificationException {
        try {
            Session session = Session.getInstance(connectionProperties, authenticator);
            Message message = createMessageFromNotification(notification, session);
            SMTPServerAnswer serverAnswer = mailSender.send(session, message);
            setResultMessage(serverAnswer);

            return serverAnswer.code >= 200 && serverAnswer.code < 300;
        } catch (MessagingException me) {
            throw new SendNotificationException(me);
        }
    }

    private Message createMessageFromNotification(Notification notification, Session session) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailFrom));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(notification.getPerson().getEmail()));
        message.setSubject(notification.getTheme());
        message.setContent(createMultipartFormNotification(notification));
        return message;
    }

    protected static Multipart createMultipartFormNotification(Notification notification) throws MessagingException {
        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(notification.getContent());
        multipart.addBodyPart(messageBodyPart);

        for (NotificationAttachment attachment : notification.getAttachments()) {
            MimeBodyPart attachmentPart = getBodyPartFromAttachment(attachment);
            multipart.addBodyPart(attachmentPart);
        }
        return multipart;
    }

    private static MimeBodyPart getBodyPartFromAttachment(NotificationAttachment attachment) throws MessagingException {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource(attachment.getContent(), null);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(attachment.getName());
        return messageBodyPart;
    }

    private void setResultMessage(SMTPServerAnswer serverAnswer) {
        resultMessage = serverAnswer.toString();
    }


}
