package ru.opfr.notification.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ResponseWrapper {
    public final String version;
    public final String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    public final LocalDateTime timestamp = LocalDateTime.now();
    public final Object response;

}
