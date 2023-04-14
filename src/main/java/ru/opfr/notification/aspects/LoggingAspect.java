package ru.opfr.notification.aspects;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.opfr.notification.aspects.service.LogService;

import static ru.opfr.notification.aspects.LogInfoMode.*;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final LogService logService;

    @Pointcut("@annotation(LogError) && args(object)")
    public void anyObjectInServiceBeanErrorPointcut(Object object) {}

    @Pointcut("@annotation(LogInfo) && args(object)")
    public void anyObjectInServiceBeanInfoPointcut(Object object) {}

    @AfterThrowing(pointcut = "anyObjectInServiceBeanErrorPointcut(object)", throwing = "ex")
    public void logError(JoinPoint joinPoint, Throwable ex, Object object) {
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

    @Before("anyObjectInServiceBeanInfoPointcut(object)")
    public void logInfoBefore(JoinPoint joinPoint, Object object) {
        LogInfo logInfo = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(LogInfo.class);
        if (isLogBeforeInvoking(logInfo.mode())) {
            logService.info(object.toString(), INPUT);
        }
    }


    @AfterReturning(pointcut = "anyObjectInServiceBeanInfoPointcut(object)", returning = "result")
    public void logInfoAfter(JoinPoint joinPoint, Object object, Object result) {
        LogInfo logInfo = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(LogInfo.class);
        if (isLogAfterInvoking(logInfo.mode())) {
            logService.info(result.toString(), OUTPUT);
        }
    }


    private boolean isLogBeforeInvoking(LogInfoMode mode) {
        return mode == ALL || mode == INPUT;
    }

    private boolean isLogAfterInvoking(LogInfoMode mode) {
        return mode == ALL || mode == OUTPUT;
    }

}
