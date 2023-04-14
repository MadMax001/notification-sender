package ru.opfr.notification.aspects;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.opfr.notification.aspects.service.LogService;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final LogService logService;

    @Pointcut("@annotation(LogError) && args(object)")
    public void anyObjectInServiceBeanPointcut(Object object) {}

    @AfterThrowing(pointcut = "anyObjectInServiceBeanPointcut(object)", throwing = "ex")
    public void serviceAndConstraintAfterThrowingLog(JoinPoint joinPoint, Throwable ex, Object object) {
        LogError logError = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(LogError.class);
        if (isAppropriateException(logError.values(), ex)) {
            String message = joinPoint.getSignature().getDeclaringType().getSimpleName() +
                    "." +
                    joinPoint.getSignature().getName() +
                    ": " +
                    object.toString();
            logService.error(message, ex);
        }
    }

    private boolean isAppropriateException(Class<? extends Throwable>[] values, Throwable ex) {
        if (values.length == 0)
            return true;
        for (Class<? extends Throwable> declaredExceptionClass : values) {
            if (declaredExceptionClass.isAssignableFrom(ex.getClass()))
                return true;
        }
        return false;
    }
}
