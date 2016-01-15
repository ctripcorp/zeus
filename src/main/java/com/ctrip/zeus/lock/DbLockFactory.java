package com.ctrip.zeus.lock;

import com.ctrip.zeus.dal.core.DistLockDao;
import com.ctrip.zeus.lock.impl.MysqlDistLock;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.transaction.TransactionManager;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/4/23.
 */
@Component("dbLockFactory")
public class DbLockFactory {
    DynamicStringProperty dbName = DynamicPropertyFactory.getInstance().getStringProperty("dal.db-name", "zeus");

    @Resource
    private DistLockDao distLockDao;
    @Resource
    private TransactionManager transactionManager;

    public DistLock newLock(String name) {
        return new MysqlDistLock(name, this);
    }

    public DistLockDao getDao() {
        return distLockDao;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public String getResourceName() {
        return dbName.get();
    }
}
