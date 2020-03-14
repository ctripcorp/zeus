package com.ctrip.zeus.lock;

import com.ctrip.zeus.dao.mapper.DistLockMapper;
import com.ctrip.zeus.lock.impl.LockScavenger;
import com.ctrip.zeus.lock.impl.MysqlDistLock;
import com.ctrip.zeus.startup.PreCheck;
import com.ctrip.zeus.util.S;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/4/23.
 */
@Component("dbLockFactory")
public class DbLockFactory implements PreCheck {
    @Resource
    private LockService lockService;

    @Resource
    private DataSourceTransactionManager mybatisTransactionManager;
    @Resource
    private DistLockMapper distLockMapper;

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


    public DistLockMapper getDistLockMapper() {
        return distLockMapper;
    }

    public DataSourceTransactionManager getMybatisTransactionManager() {
        return mybatisTransactionManager;
    }

    public LockScavenger getLockScavenger() {
        return lockScavenger;
    }

    @Override
    public boolean ready() {
        try {
            lockService.forceUnlockByServer(S.getIp());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
