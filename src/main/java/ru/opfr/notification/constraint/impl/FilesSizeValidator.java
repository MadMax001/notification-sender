package ru.opfr.notification.constraint.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.exception.ApplicationRuntimeException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static ru.opfr.notification.ApplicationConstants.FILES_SIZE_TOO_LARGE;

@Component
public class FilesSizeValidator implements Validator {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSizeString;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isArray();
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (Objects.nonNull(target) && target.getClass().isArray()) {
            long maxFileSize = getDigitalPart(maxFileSizeString) *
                    calculateCoefficient(getStringSuffixPart(maxFileSizeString));

            MultipartFile[] files = (MultipartFile[]) target;
            if (files.length > 0) {
                boolean isLargeFile = Arrays.stream(files)
                        .anyMatch(file -> {
                            try {
                                return file.getBytes().length > maxFileSize;
                            } catch (IOException e) {
                                throw new ApplicationRuntimeException(e);
                            }
                        });
                if (isLargeFile) {
                    errors.rejectValue(null, FILES_SIZE_TOO_LARGE);
                }
            }
        }
    }

    private long calculateCoefficient(String suffix) {
        switch (suffix.toUpperCase()) {
            case "GB": return 1024 * 1024 * 1024L;
            case "MB": return 1024 * 1024L;
            case "KB": return 1024;
            default: return 1;
        }
    }
    private long getDigitalPart(String value) {
        return Long.parseLong(value.replaceAll("\\D",""));
    }
    private String getStringSuffixPart(String value) {
        return value.replaceAll("\\d","");
    }

}
