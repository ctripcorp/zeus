package com.ctrip.zeus.service.aop;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.transaction.TransactionManager;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
@Component
@Aspect
@Order
public class TransactionAspect {

    @Resource
    private TransactionManager transactionManager;

    DynamicStringProperty dbName = DynamicPropertyFactory.getInstance().getStringProperty("dal.db-name", "zeus");

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.ctrip.zeus.service..*Repository.*(..)) || " +
            "execution(* com.ctrip.zeus.service..*Service.*(..))")
    public Object validate(ProceedingJoinPoint point) throws Throwable {
        String objectName = point.getSignature().getDeclaringTypeName();
        String methodName = point.getSignature().getName();

//        long start = System.currentTimeMillis();

        boolean isInTransaction = transactionManager.isInTransaction();
        if (isInTransaction) {
            logger.info("In transaction,  [{}.{}] is called.", objectName, methodName);
        } else {
            logger.info("Start transaction. [{}.{}] is called.", objectName, methodName);
            transactionManager.startTransaction(dbName.get());
        }

        try {
            Object result = point.proceed();
            if (!isInTransaction) {
                logger.info("Commit transaction. [{}.{}] is called.", objectName, methodName);
                transactionManager.commitTransaction();
            }
            return result;
        } catch (Throwable throwable) {
            if (!isInTransaction) {
                logger.warn(String.format("Rollback transaction. [%s.%s] is called.", objectName, methodName));
                transactionManager.rollbackTransaction();
            }
            throw throwable;
        }
    }
}
