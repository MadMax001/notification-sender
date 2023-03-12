package ru.opfr.notification.constraint;

import ru.opfr.notification.constraint.impl.NotEmptyByTypeValidator;
import ru.opfr.notification.model.NotificationTypeDictionary;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;

@Documented
@Constraint(validatedBy = NotEmptyByTypeValidator.class)
@Retention(RUNTIME)
@Target( { TYPE})
@Repeatable(NotNullByTypeList.class)
public @interface NotNullByType {
    String field();
    String typeField() default "type";
    NotificationTypeDictionary type() default MESSAGE;
    NotificationTypeDictionary[] types() default {};

    String message() default "";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
