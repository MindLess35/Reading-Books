package com.senla.readingbooks.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LoggingServiceAspect {

    @Pointcut("within(com.senla.readingbooks.service.impl.*.*ServiceImpl)")
    public void isServiceLayer() {
    }

    @Pointcut("execution(* com.senla.readingbooks.service.impl.*.*ServiceImpl.*(..))")
    public void isServiceMethods() {
    }

    @Around("isServiceLayer() && isServiceMethods()")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature methodSignature = joinPoint.getSignature();
        log.debug("Entering method: {} with arguments: {}", methodSignature, joinPoint.getArgs());

        Object result;
        long startTime = System.currentTimeMillis();
        try {
            result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - startTime;
            log.debug("Method {} executed successfully. Return value: {}. Time taken: {} ms",
                    methodSignature, result, timeTaken);

        } catch (Throwable ex) {
            long timeTaken = System.currentTimeMillis() - startTime;
            log.debug("Method {} threw an exception: {} - {}. Time taken: {} ms",
                    methodSignature, ex.getClass().getSimpleName(), ex.getMessage(), timeTaken);

            throw ex;
        }
        return result;
    }

}
