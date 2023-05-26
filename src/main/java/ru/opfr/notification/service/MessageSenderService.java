package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.opfr.notification.aspects.logging.Log;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.messageprocess.AsyncWinMessageService;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static ru.opfr.notification.aspects.logging.LogType.ERROR;
import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;
@Service
@RequiredArgsConstructor
public class MessageSenderService extends AbstractSenderService {
    private final AsyncWinMessageService messageService;
    @Override
    public NotificationTypeDictionary getType() {
        return MESSAGE;
    }

    @Override
    @Log(goals ={ERROR}, errors = {SendNotificationException.class})
    public boolean send(Notification notification) throws SendNotificationException {
        try {
            WinConsoleExecuteResponse response = messageService.send(notification).get();
            setResultMessage(response);

            return response.getExitCode() == 0;
        } catch (IOException|ExecutionException|InterruptedException e) {
            throw new SendNotificationException(e);
        }
    }

    private void setResultMessage(WinConsoleExecuteResponse response) {
        final StringBuilder sb = new StringBuilder();
        response.getConsoleStdOut().forEach(line -> {sb.append(line); sb.append(" ");});
        response.getConsoleErrOut().forEach(line -> {sb.append(line); sb.append(" ");});
        resultMessage = sb.toString().trim();

    }

}
