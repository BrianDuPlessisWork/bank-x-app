package za.co.entelect.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

//    @Around("execution(* za.co.entelect.service.*.*(..))")
//    public Object logMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = System.currentTimeMillis();
//        Object result = joinPoint.proceed();
//        long executedTime = System.currentTimeMillis() - start;
//        log.info("{} executed in {}ms", joinPoint.getSignature(), executedTime);
//        return result;
//    }
}
