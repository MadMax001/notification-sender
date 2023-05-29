package ru.opfr.notification.messageprocess;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class WinConsoleCommandServiceImpl implements WinConsoleCommandService {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Charset CONSOLE_CHARSET = Charset.forName("cp866");

    public CompletableFuture<WinConsoleExecuteResponse> executeCommand(ProcessBuilder builder) throws IOException, InterruptedException {
        return executeCommand(builder, null);
    }

    @Override
    @Async("taskSenderExecutor")
    public CompletableFuture<WinConsoleExecuteResponse> executeCommand(ProcessBuilder builder, String inputValue) throws IOException, InterruptedException {
        int exitCode = -1;
        List<String> stdOut = new ArrayList<>();
        List<String> errOut = new ArrayList<>();
        Process process = builder.start();

        try (BufferedReader opReader = new BufferedReader(new InputStreamReader(process.getInputStream(), CONSOLE_CHARSET));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), CONSOLE_CHARSET));
             BufferedWriter valueWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), CONSOLE_CHARSET))) {

            executorService.submit(() -> {
                try {
                    if (Objects.nonNull(inputValue)) {
                        valueWriter.write(inputValue);
                        valueWriter.write("\n");
                        valueWriter.flush();
                    }
                    opReader.lines().filter(s -> !s.isEmpty()).filter(line -> !line.trim().isEmpty()).forEach(stdOut::add);
                    errorReader.lines().filter(s -> !s.isEmpty()).filter(line -> !line.trim().isEmpty()).forEach(errOut::add);
                } catch (IOException e) {
                        throw new ApplicationRuntimeException(e);
                }
            });

            boolean result = process.waitFor(5, TimeUnit.SECONDS);
            if (result)
                exitCode = process.exitValue();
            process.destroyForcibly();
        }

        WinConsoleExecuteResponse response =  WinConsoleExecuteResponse.builder()
                .exitCode(exitCode)
                .consoleStdOut(stdOut)
                .consoleErrOut(errOut)
                .build();

        return CompletableFuture.completedFuture(response);
    }


}
