package ru.opfr.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.opfr.notification.model.dto.ResponseWrapper;
import ru.opfr.notification.service.misc.ConstraintValidationExceptionService;

import javax.validation.ConstraintViolationException;


@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerController {
    private final ConstraintValidationExceptionService constraintValidationExceptionService;

    @Value("${app.api.version}")
    private String apiVersion;

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseWrapper> handleConstraintValidationException(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(ResponseWrapper.builder()
                .message(constraintValidationExceptionService.getConstraintValidationMessage(exception))
                .version(apiVersion)
                .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseWrapper> handleRuntimeException(Throwable exception) {
        if (constraintValidationExceptionService.isConstraintValidationExceptionInDepth(exception)) {
            ConstraintViolationException validationException =
                    constraintValidationExceptionService.findConstraintValidationExceptionInDepth(exception);
            return handleConstraintValidationException(validationException);
        }
        return ResponseEntity.internalServerError().body(ResponseWrapper.builder()
                .message(getExceptionMessage(exception))
                .version(apiVersion)
                .build());
    }

    private String getExceptionMessage(Throwable throwable) {
        return throwable.getMessage();
    }

}
