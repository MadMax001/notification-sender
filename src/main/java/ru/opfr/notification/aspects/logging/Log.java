package ru.opfr.notification.aspects.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({METHOD})
public @interface Log {
    LogType[] goals() default {};
    Class<? extends Throwable>[] errors() default {};
}
