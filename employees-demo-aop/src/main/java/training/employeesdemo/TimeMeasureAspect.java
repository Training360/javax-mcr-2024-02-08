package training.employeesdemo;

import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
//@Slf4j
public class TimeMeasureAspect {

    @Around("execution(* training.employeesdemo.EmployeesService.*(..))")
    public Object measure(ProceedingJoinPoint joinpoint) throws Throwable {
        var start = System.currentTimeMillis();
        var result = joinpoint.proceed();
        var end = System.currentTimeMillis();
        log.info("Current time: {}, name: {}",
                (end - start), joinpoint.getSignature().getName()
                );
        return result;
    }
}
