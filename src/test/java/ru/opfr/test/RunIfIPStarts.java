package ru.opfr.test;

import org.junit.jupiter.api.extension.*;

import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({METHOD})
@ExtendWith(RunIfIPStartsCondition.class)
public @interface RunIfIPStarts {
    String value() default "";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
