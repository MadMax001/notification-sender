package ru.opfr.notification.constraint;

import ru.opfr.notification.constraint.impl.FilesSizeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = FilesSizeValidator.class)
@Retention(RUNTIME)
@Target( { FIELD, PARAMETER})
public @interface FileSize {
    String message() default "";
    String max() default "";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
