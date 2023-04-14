package ru.opfr.notification.aspects;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
        String message = joinPoint.getSignature().getDeclaringType().getSimpleName() +
                "." +
                joinPoint.getSignature().getName() +
                ": " +
                object.toString();
        logService.error(message, ex);
    }
}
