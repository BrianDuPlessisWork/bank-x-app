package za.co.entelect.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import za.co.entelect.annotation.testAnnotation;
import za.co.entelect.dto.Customer;
import za.co.entelect.entity.CustomerEntity;

@Aspect
@Component
public class ApplicationAspect {

    @Pointcut("@annotation(za.co.entelect.annotation.testAnnotation)")
    public void executeAnnotated(){}

    @Before("executeAnnotated() && args(customer)")
    public void testBeforeAdvice(JoinPoint joinPoint, CustomerEntity customer){
        System.out.println("Before Advice is working. "+ customer.getName());
    }

    @AfterReturning(value = "executeAnnotated()", returning = "customer")
    public void testAfterAdvice(Customer customer){
        System.out.println("After Advice is working. " + customer.getName());
    }

    @Around("executeAnnotated()")
    public void testAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executedTime = System.currentTimeMillis() - start;
        System.out.println(joinPoint.getSignature() + " executed in " + executedTime + "ms");
    }
}
