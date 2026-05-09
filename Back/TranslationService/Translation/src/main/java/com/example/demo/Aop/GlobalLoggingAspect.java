package com.example.demo.Aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j // Uses Slf4j for logging
public class GlobalLoggingAspect {

    // 1. Define Pointcuts for your specific packages
    @Pointcut("within(com.example.demo.controller..*)")
    public void controllerPointcut() {}

    @Pointcut("within(com.example.demo.service..*)")
    public void servicePointcut() {}

    @Pointcut("within(com.example.demo.Subscriber..*)")
    public void subscriberPointcut() {}

    // 2. The Logic (Advice) that runs around the methods
    @Around("controllerPointcut() || servicePointcut() || subscriberPointcut()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Log entry
        log.info("➔ [EXECUTION START] {}.{} | Args: {}", className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed(); // This runs the actual method code

            long duration = System.currentTimeMillis() - startTime;
            log.info("✓ [EXECUTION END] {}.{} | Time: {}ms", className, methodName, duration);

            return result;
        } catch (Throwable e) {
            // Log errors automatically
            log.error("✘ [EXECUTION FAILED] {}.{} | Error: {}", className, methodName, e.getMessage());
            throw e; // Rethrow so Spring's GlobalExceptionHandler can still handle it
        }
    }
}