package ru.opfr.notification.messageprocess;

import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface WinConsoleCommandService {
    CompletableFuture<WinConsoleExecuteResponse> executeCommand(ProcessBuilder builder) throws IOException, InterruptedException;
    CompletableFuture<WinConsoleExecuteResponse> executeCommand(ProcessBuilder builder, String inputValue) throws IOException, InterruptedException;
}
