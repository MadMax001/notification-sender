package ru.opfr.notification.messageprocess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.opfr.notification.messageprocess.model.WinConsoleExecuteResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class WinConsoleCommandServiceImplTest {
    private WinConsoleCommandService commandService;

    @BeforeEach
    void setUp() {
        commandService = new WinConsoleCommandServiceImpl();
    }

    @Test
    void execCorrectWinCommand() throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder builder = new ProcessBuilder()
            .command("cmd.exe", "/c", "dir");
        CompletableFuture<WinConsoleExecuteResponse> responseCompletableFuture = commandService.executeCommand(builder);
        WinConsoleExecuteResponse response = responseCompletableFuture.get();
        assertEquals(0, response.getExitCode());
        assertTrue(response.getConsoleStdOut().size() > 0);
        assertEquals(0, response.getConsoleErrOut().size());
    }

    @Test
    void execIncorrectWinCommand() throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder builder = new ProcessBuilder()
                .command("cmd.exe", "/c", "dirdir");
        CompletableFuture<WinConsoleExecuteResponse> responseCompletableFuture = commandService.executeCommand(builder);
        WinConsoleExecuteResponse response = responseCompletableFuture.get();
        assertEquals(1, response.getExitCode());
        assertEquals(0, response.getConsoleStdOut().size());
        assertTrue(response.getConsoleErrOut().size() > 0);
    }

    @Test
    void execNotExistedCommand_AndThrowException(){
        ProcessBuilder builder = new ProcessBuilder()
                .command("cmdaaaaaa.exe");
        assertThrows(IOException.class, () -> commandService.executeCommand(builder));
    }

    @Test
    void execUnlimitedTimeCommand() throws ExecutionException, InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder()
                .command("cmd.exe", "/c", "pause");
        CompletableFuture<WinConsoleExecuteResponse> responseCompletableFuture = commandService.executeCommand(builder);
        WinConsoleExecuteResponse response = responseCompletableFuture.get();
        assertEquals(-1, response.getExitCode());
        assertEquals(1, response.getConsoleStdOut().size());
        assertEquals(0, response.getConsoleErrOut().size());

    }

    @Test
    void execCommandWithPrompt_AndInputValue() throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder builder = new ProcessBuilder()
                .command("cmd.exe", "/c", "CHOICE /T 10 /C ync /CS /D y /M \"Сделайте выбор\"");
        WinConsoleExecuteResponse response = commandService.executeCommand(builder, "c").get();
        assertEquals(3, response.getExitCode());
        assertEquals(1, response.getConsoleStdOut().size());
        assertEquals("Сделайте выбор [y,n,c]?c", response.getConsoleStdOut().get(0));
        assertEquals(0, response.getConsoleErrOut().size());
   }

    @Test
    void execCorrectWinCommandTwice() throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder builder = new ProcessBuilder()
                .command("cmd.exe", "/c", "dir");
        CompletableFuture<WinConsoleExecuteResponse> responseCompletableFuture1 = commandService.executeCommand(builder);
        WinConsoleExecuteResponse response1 = responseCompletableFuture1.get();
        assertEquals(0, response1.getExitCode());
        assertTrue(response1.getConsoleStdOut().size() > 0);
        assertEquals(0, response1.getConsoleErrOut().size());

        CompletableFuture<WinConsoleExecuteResponse> responseCompletableFuture2 = commandService.executeCommand(builder);
        WinConsoleExecuteResponse response2 = responseCompletableFuture2.get();
        assertEquals(0, response2.getExitCode());
        assertTrue(response2.getConsoleStdOut().size() > 0);
        assertEquals(0, response2.getConsoleErrOut().size());
    }

    @Test
    void execCommandWithPrompt_AndInputValue_Twice() throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder builder = new ProcessBuilder()
                .command("cmd.exe", "/c", "CHOICE /T 10 /C ync /CS /D y /M \"Сделайте выбор\"");
        WinConsoleExecuteResponse response = commandService.executeCommand(builder, "c").get();
        assertEquals(3, response.getExitCode());
        assertEquals(1, response.getConsoleStdOut().size());
        assertEquals("Сделайте выбор [y,n,c]?c", response.getConsoleStdOut().get(0));
        assertEquals(0, response.getConsoleErrOut().size());

        WinConsoleExecuteResponse response2 = commandService.executeCommand(builder, "n").get();
        assertEquals(2, response2.getExitCode());
        assertEquals(1, response2.getConsoleStdOut().size());
        assertEquals("Сделайте выбор [y,n,c]?n", response2.getConsoleStdOut().get(0));
        assertEquals(0, response2.getConsoleErrOut().size());

    }

    @Test
    void execPrintCommand_AndCheckOutput() throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder builder = new ProcessBuilder()
                .command("cmd.exe", "/c", "echo test_string");
        WinConsoleExecuteResponse response = commandService.executeCommand(builder, "c").get();
        assertEquals(0, response.getExitCode());
        assertEquals(1, response.getConsoleStdOut().size());
        assertEquals("test_string", response.getConsoleStdOut().get(0));
        assertEquals(0, response.getConsoleErrOut().size());

    }
}