package com.ctrip.zeus.service.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalNotFoundException;

/**
 * Created by zhoumy on 2015/4/3.
 */
@Aspect
@Component
public class DalNotFoundAspect {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.ctrip.zeus..*Dao.*(..))")
    public Object allowNullValue(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DalNotFoundException ex) {
            logger.info("Caught DalNotFoundException, return null instead.");
            return null;
        }
    }
}
