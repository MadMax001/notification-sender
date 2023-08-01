package ru.opfr.notification.messageprocess;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.opfr.notification.WinCredentialsManagerStoreChecker;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;
import ru.opfr.notification.model.Notification;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


@Service
public class AsyncWinMessageServiceImpl implements AsyncWinMessageService {

    private final CredentialsService credentialsService;
    private final WinConsoleCommandService commandService;
    private final WinCredentialsManagerStoreChecker checker;
    private final boolean isWindows;
    private final String showingTime;

    public AsyncWinMessageServiceImpl(CredentialsService credentialsService,
                                      WinConsoleCommandService commandService,
                                      WinCredentialsManagerStoreChecker checker,
                                      @Value("${app.message.showing_time}") String showingTime) {
        this.credentialsService = credentialsService;
        this.commandService = commandService;
        this.checker = checker;
        this.showingTime = showingTime;
        isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    }

    @Override
    public synchronized CompletableFuture<WinConsoleExecuteResponse> send(Notification notification) throws IOException, InterruptedException {
        if (!isWindows) {
            throw new ApplicationRuntimeException("The sending message process is implements on windows platform only!");
        }
        ProcessBuilder builder;
        if (checker.isCredentialExists()) {
            builder = getProcessBuilder(notification);
        } else {
            builder = getProcessBuilderWithPassword(notification);
        }
        return commandService.executeCommand(builder);
    }

    private ProcessBuilder getProcessBuilder(Notification notification) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(
                "cmd.exe",
                "/C",
                getRunsAsMessageCommand(notification));
        return builder;
    }

    private ProcessBuilder getProcessBuilderWithPassword(Notification notification) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(
                "cmd.exe",
                "/C",
                "echo " + credentialsService.getPassword() + "|" + getRunsAsMessageCommand(notification));
        return builder;
    }

    private String getRunsAsMessageCommand(Notification notification) {
        return new StringBuilder()
                .append("C:\\Windows\\System32\\runas.exe")
                .append(" /env")
                .append(" /profile")
                .append(" /savecred")
                .append(" /user:").append(credentialsService.getUsername())
                .append(" \"msg")
                .append(" ").append(notification.getPerson().getUser())
                .append(" /SERVER:").append(notification.getPerson().getIp())
                .append(" /TIME:").append(showingTime)
                .append(" ").append(notification.getContent())
                .append("\"")
                .toString();
    }
}
