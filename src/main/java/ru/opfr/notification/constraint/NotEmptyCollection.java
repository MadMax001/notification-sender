package ru.opfr.notification.constraint;

import ru.opfr.notification.constraint.impl.NotEmptyCollectionValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = NotEmptyCollectionValidator.class)
@Retention(RUNTIME)
@Target( { FIELD})
public @interface NotEmptyCollection {
    String message() default "Empty collection";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
