package com.ctrip.zeus.lock;

import com.ctrip.zeus.dal.core.DistLockDao;
import com.ctrip.zeus.lock.impl.LockScavenger;
import com.ctrip.zeus.lock.impl.MysqlDistLock;
import com.ctrip.zeus.startup.PreCheck;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.transaction.TransactionManager;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/4/23.
 */
@Component("dbLockFactory")
public class DbLockFactory implements PreCheck {
    DynamicStringProperty dbName = DynamicPropertyFactory.getInstance().getStringProperty("dal.db-name", "zeus");

    @Resource
    private DistLockDao distLockDao;
    @Resource
    private TransactionManager transactionManager;
    @Resource
    private LockService lockService;

    private LockScavenger lockScavenger;

    public DbLockFactory() {
        try {
            lockScavenger = new LockScavenger();
        } catch (RuntimeException e) {
        } catch (Throwable t) {
        }
    }

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

    public LockScavenger getLockScavenger() {
        return lockScavenger;
    }

    @Override
    public boolean ready() {
        try {
            lockService.forceUnlockByServer(S.getIp());
            return true;
        } catch (DalException e) {
            return false;
        }
    }
}
