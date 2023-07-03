package ru.opfr.notification.constraint.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.constraint.FileSize;
import ru.opfr.notification.exception.ApplicationRuntimeException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Component
public class FilesSizeValidator implements ConstraintValidator<FileSize, MultipartFile[]> {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSizeFromProperties;

    private String maxParam;

    @Override
    public void initialize(FileSize constraintAnnotation) {
        this.maxParam = constraintAnnotation.max();
        if (maxParam.isEmpty())
            maxParam = maxFileSizeFromProperties;
    }

    @Override
    public boolean isValid(MultipartFile[] files, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.nonNull(files)) {
            long maxFileSize = getDigitalPart(maxParam) *
                    calculateCoefficient(getStringSuffixPart(maxParam));


            if (files.length > 0) {
                boolean isLargeFile = Arrays.stream(files)
                        .anyMatch(file -> {
                            try {
                                return file.getBytes().length > maxFileSize;
                            } catch (IOException e) {
                                throw new ApplicationRuntimeException(e);
                            }
                        });
                return !isLargeFile;
            }
        }
        return true;
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
