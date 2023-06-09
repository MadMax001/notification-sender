package ru.opfr.notification.messageprocess;

import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;
import ru.opfr.notification.model.Notification;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface AsyncWinMessageService {

    CompletableFuture<WinConsoleExecuteResponse> send(Notification notification) throws IOException, InterruptedException;
}
