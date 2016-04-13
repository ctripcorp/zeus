package com.ctrip.zeus.lock;

import com.ctrip.zeus.dal.core.DistLockDao;
import com.ctrip.zeus.dal.core.DistLockDo;
import com.ctrip.zeus.dal.core.DistLockEntity;
import com.ctrip.zeus.lock.impl.MysqlDistLock;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.transaction.TransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

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

    @PostConstruct
    public void releaseDeadLocks() throws Exception {
        List<DistLockDo> check = distLockDao.getByServer(S.getIp(), DistLockEntity.READSET_FULL);
        for (DistLockDo d : check) {
            d.setServer("").setOwner(0L).setCreatedTime(System.currentTimeMillis());
        }
        distLockDao.updateByKey(check.toArray(new DistLockDo[check.size()]), DistLockEntity.UPDATESET_FULL);
    }
}
