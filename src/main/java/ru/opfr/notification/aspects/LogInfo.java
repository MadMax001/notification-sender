package ru.opfr.notification.aspects;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static ru.opfr.notification.aspects.LogInfoMode.ALL;

@Documented
@Retention(RUNTIME)
@Target({METHOD})
public @interface LogInfo {
    LogInfoMode mode() default ALL;
}
