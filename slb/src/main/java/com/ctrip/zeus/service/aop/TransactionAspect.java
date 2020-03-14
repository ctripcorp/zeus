package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.service.aop.util.ThreadLocalMybatisTransactionInfo;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
@Component
@Aspect
@Order
public class TransactionAspect implements Ordered {


    @Resource
    private DataSourceTransactionManager transactionManager;

    private static ThreadLocalMybatisTransactionInfo threadLocalData = new ThreadLocalMybatisTransactionInfo();
    DynamicBooleanProperty startTransaction = DynamicPropertyFactory.getInstance().getBooleanProperty("mybatis.transaction.enable", true);
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.ctrip.zeus.service..*Repository.*(..)) || " +
            "execution(* com.ctrip.zeus.service..*Service.*(..))")
    public Object validate(ProceedingJoinPoint point) throws Throwable {
        String objectName = point.getSignature().getDeclaringTypeName();
        String methodName = point.getSignature().getName();

        ThreadLocalMybatisTransactionInfo.MybatisTransactionInfo info = threadLocalData.get();
        boolean mybatisInTransaction = true;
        if (info.getStatus() == null || info.getStatus().isCompleted()) {
            mybatisInTransaction = false;
        }
        TransactionStatus txStatus = null;
        if (mybatisInTransaction) {
            logger.debug("In transaction,  [{}.{}] is called.", objectName, methodName);
        } else {
            logger.debug("Start transaction. [{}.{}] is called.", objectName, methodName);
            if (startTransaction.get()) {
                txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
                info.setStatus(txStatus);
            }
        }

        try {
            Object result = point.proceed();
            if (txStatus != null) {
                transactionManager.commit(txStatus);
            }
            return result;
        } catch (Throwable throwable) {
            if (txStatus != null) {
                transactionManager.rollback(txStatus);
            }
            throw throwable;
        }
    }

    @Override
    public int getOrder() {
        return AspectOrder.Transaction;
    }
}
