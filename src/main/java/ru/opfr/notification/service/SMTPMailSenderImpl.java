package ru.opfr.notification.service;

import com.sun.mail.smtp.SMTPTransport;
import org.springframework.stereotype.Service;
import ru.opfr.notification.model.SMTPServerAnswer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

@Service
public class SMTPMailSenderImpl implements SMTPMailSender {
    @Override
    public SMTPServerAnswer send(Session session, Message message) throws MessagingException {
        try (SMTPTransport transport = (SMTPTransport) session.getTransport("smtp")) {
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            return new SMTPServerAnswer(transport.getLastReturnCode(), transport.getLastServerResponse());
        }
    }
}
