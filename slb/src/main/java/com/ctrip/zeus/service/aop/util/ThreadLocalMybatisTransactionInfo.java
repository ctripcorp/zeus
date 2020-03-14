package com.ctrip.zeus.service.aop.util;

import org.springframework.transaction.TransactionStatus;

public class ThreadLocalMybatisTransactionInfo extends ThreadLocal<ThreadLocalMybatisTransactionInfo.MybatisTransactionInfo> {
    @Override
    protected MybatisTransactionInfo initialValue() {
        return new MybatisTransactionInfo();
    }
    public static class MybatisTransactionInfo{
        TransactionStatus status = null;

        public TransactionStatus getStatus() {
            return status;
        }

        public MybatisTransactionInfo setStatus(TransactionStatus status) {
            this.status = status;
            return this;
        }
    }
}
