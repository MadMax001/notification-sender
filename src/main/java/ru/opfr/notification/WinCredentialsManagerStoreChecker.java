package ru.opfr.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.messageprocess.EncryptedCredentialsService;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;
import ru.opfr.notification.messageprocess.WinConsoleCommandService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class WinCredentialsManagerStoreChecker {
    private static final String NONE_LABEL = "* NONE *";
    private final EncryptedCredentialsService credentialsService;
    private final WinConsoleCommandService commandService;

    private boolean credentialExists;

    @EventListener
    public void checkByWinCommandOnRefreshEvent(ContextRefreshedEvent event) {
        ProcessBuilder builder = new ProcessBuilder()
                .command("cmdkey",
                        "/list:Domain:interactive=" + credentialsService.getUsername());

        try {
            WinConsoleExecuteResponse response = commandService.executeCommand(builder).get();
            if (response.getExitCode() != 0)
                throw new ApplicationRuntimeException("Unsuccessful execute of 'cmdkey /list' command: "
                        + getAllResponseOutInLine(response));
            credentialExists = checkLog(response);
            if (!credentialExists) {
                throw new ApplicationRuntimeException("There are no saved credentials for admin");
            }
        } catch (InterruptedException | IOException | ExecutionException e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    private boolean checkLog(WinConsoleExecuteResponse response) {
        boolean logContainsNoneLabel = response.getConsoleStdOut().stream().anyMatch(line -> line.contains(NONE_LABEL));
        boolean logContainsAdminUsername = response.getConsoleStdOut().stream().anyMatch(line -> line.contains(credentialsService.getUsername()));
        return logContainsAdminUsername && !logContainsNoneLabel
                && response.getConsoleStdOut().size() == 4
                && response.getConsoleErrOut().isEmpty();
    }

    private String getAllResponseOutInLine(WinConsoleExecuteResponse response) {
        final StringBuilder sb = new StringBuilder();
        response.getConsoleStdOut().forEach(line -> {sb.append(line); sb.append(" ");});
        response.getConsoleErrOut().forEach(line -> {sb.append(line); sb.append(" ");});
        return sb.toString();
    }

    public boolean isCredentialExists() {
        return credentialExists;
    }
}
