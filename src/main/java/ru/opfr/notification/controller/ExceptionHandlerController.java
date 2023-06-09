package ru.opfr.notification.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.opfr.notification.model.dto.ResponseWrapper;



@ControllerAdvice
public class ExceptionHandlerController {
    @Value("${app.api.version}")
    private String apiVersion;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseWrapper> handleException(Throwable exception) {
        return ResponseEntity.internalServerError().body(ResponseWrapper.builder()
                .message(getExceptionMessage(exception))
                .version(apiVersion)
                .build());
    }

    private String getExceptionMessage(Throwable throwable) {
        return throwable.getMessage();
    }
}
