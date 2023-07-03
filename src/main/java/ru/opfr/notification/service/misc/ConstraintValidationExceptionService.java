package ru.opfr.notification.service.misc;

import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ConstraintValidationExceptionService {
    public boolean isConstraintValidationExceptionInDepth(Throwable exception) {
        if (exception instanceof ConstraintViolationException)
            return true;
        if (Objects.isNull(exception.getCause()))
            return false;
        return isConstraintValidationExceptionInDepth(exception.getCause());
    }

    public ConstraintViolationException findConstraintValidationExceptionInDepth(Throwable exception) {
        if (exception instanceof ConstraintViolationException)
            return (ConstraintViolationException)exception;
        return findConstraintValidationExceptionInDepth(exception.getCause());
    }

    public String getConstraintValidationMessage(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
    }
}
