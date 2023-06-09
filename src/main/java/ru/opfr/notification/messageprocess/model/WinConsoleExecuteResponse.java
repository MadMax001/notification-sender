package ru.opfr.notification.messageprocess.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Objects;

@Builder
@Getter
public class WinConsoleExecuteResponse {
    private final int exitCode;
    private final List<String> consoleStdOut;
    private final List<String> consoleErrOut;

    @Override
    public String toString() {
        return "{" +
                "exitCode=" + exitCode +
                (Objects.nonNull(consoleStdOut) ? (", stdOut:'" + Strings.join(consoleStdOut, '|') + '\'') : "") +
                (Objects.nonNull(consoleErrOut) ? (", errOut:'" + Strings.join(consoleErrOut, '|') + '\'') : "") +
                '}';
    }
}
