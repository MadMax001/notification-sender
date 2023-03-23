package ru.opfr.notification.service;

import ru.opfr.notification.model.SMTPServerAnswer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

public interface SMTPMailSender {
    SMTPServerAnswer send(Session session, Message message) throws MessagingException;

}
