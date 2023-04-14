package ru.opfr.notification.aspects.logging;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.opfr.notification.aspects.logging.service.LogService;

import java.util.stream.Stream;

import static ru.opfr.notification.aspects.logging.LogType.*;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final LogService logService;

    @Pointcut("@annotation(Log) && args(object)")
    public void logPointcut(Object object) {}

    @AfterThrowing(pointcut = "logPointcut(object)", throwing = "ex")
    public void LogError(JoinPoint joinPoint, Throwable ex, Object object) {
        Log logError = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Log.class);
        if (isAppropriateException(logError, ex)) {
            String message = joinPoint.getSignature().getDeclaringType().getSimpleName() +
                    "." +
                    joinPoint.getSignature().getName() +
                    ": " +
                    object.toString();
            logService.error(message, ex);
        }
    }


    private boolean isAppropriateException(Log log, Throwable ex) {
        if (!isErrorLog(log))
            return false;
        if (isAnyErrors(log))
            return true;
        for (Class<? extends Throwable> declaredExceptionClass : log.errors()) {
            if (declaredExceptionClass.isAssignableFrom(ex.getClass()))
                return true;
        }
        return false;
    }

    private boolean isErrorLog(Log log) {
        return isAppropriateLogType(log, ERROR);
    }

    private boolean isAnyErrors(Log log) {
        return log.errors().length == 0;
    }

    private boolean isAppropriateLogType(Log log, LogType type) {
        return log.what().length == 0 ||
                Stream.of(log.what()).anyMatch(element -> element == ALL || element == type);
    }

    @Before("logPointcut(object)")
    public void logInfoBefore(JoinPoint joinPoint, Object object) {
        Log logInfo = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Log.class);
        if (isLogBeforeInvoking(logInfo)) {
            logService.info(object.toString(), INPUT);
        }
    }


    @AfterReturning(pointcut = "logPointcut(object)", returning = "result")
    public void logInfoAfter(JoinPoint joinPoint, Object object, Object result) {
        Log logInfo = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Log.class);
        if (isLogAfterInvoking(logInfo)) {
            logService.info(result.toString(), OUTPUT);
        }
    }

    private boolean isLogBeforeInvoking(Log logInfo) {
        return isAppropriateLogType(logInfo, INPUT);
    }

    private boolean isLogAfterInvoking(Log logInfo) {
        return isAppropriateLogType(logInfo, OUTPUT);
    }



}
