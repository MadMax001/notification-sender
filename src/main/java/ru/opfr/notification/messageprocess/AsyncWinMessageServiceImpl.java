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
    @Value("${app.message.showing_time:36000")
    private String showingTime;

    public AsyncWinMessageServiceImpl(CredentialsService credentialsService,
                                      WinConsoleCommandService commandService,
                                      WinCredentialsManagerStoreChecker checker) {
        this.credentialsService = credentialsService;
        this.commandService = commandService;
        this.checker = checker;
        isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    }

    @Override
    public CompletableFuture<WinConsoleExecuteResponse> send(Notification notification) throws IOException, InterruptedException {
        if (!isWindows) {
            throw new ApplicationRuntimeException("The sending message process is implements on windows platform only!");
        }
        ProcessBuilder builder = getProcessBuilder(notification);
        if (checker.isCredentialExists())
            return commandService.executeCommand(builder);
        else
            return commandService.executeCommand(builder, credentialsService.getPassword());
    }

    private ProcessBuilder getProcessBuilder(Notification notification) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(
                "C:\\Windows\\System32\\runas.exe",
                "/env",
                "/profile",
                "/savecred",
                "/user:" + credentialsService.getUsername(),
                "\"msg",
                notification.getPerson().getUser(),
                "/SERVER:" + notification.getPerson().getIp(),
                "/TIME:" + showingTime,
                notification.getContent(),
                "\"");
        return builder;
    }
}
