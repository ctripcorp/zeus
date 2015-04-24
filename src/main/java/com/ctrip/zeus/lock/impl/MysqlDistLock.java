package com.ctrip.zeus.lock.impl;

import com.ctrip.zeus.dal.core.DistLockDao;
import com.ctrip.zeus.dal.core.DistLockDo;
import com.ctrip.zeus.dal.core.DistLockEntity;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalException;

/**
 * Created by zhoumy on 2015/4/9.
 */
public class MysqlDistLock implements DistLock {
    private static final int MAX_RETRIES = 3;
    private static final long SLEEP_INTERVAL = 500L;

    private final String key;
    private volatile boolean state;

    private DistLockDao distLockDao = DbLockFactory.getDao();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public MysqlDistLock(String key) {
        this.key = key;
        this.state = false;
    }

    @Override
    public boolean tryLock() {
        DistLockDo d = new DistLockDo().setLockKey(key).setCreatedTime(System.currentTimeMillis());
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                if (tryAddLock(d))
                    return true;
                retryDelay(key, i);
            } catch (DalException e) {
                retryDelay(key, i);
            }
        }
        logger.warn("Unable to create the lock " + key);
        return false;
    }

    @Override
    public void lock(int timeout) throws Exception {
        long end = System.currentTimeMillis() + timeout;
        DistLockDo d = new DistLockDo().setLockKey(key).setCreatedTime(System.currentTimeMillis());
        while(System.currentTimeMillis() < end) {
            try {
                if (tryAddLock(d))
                    return;
                retryDelay(key, 1);
            } catch (DalException e) {
                retryDelay(key, 1);
            }
        }
        throw new Exception("Fail to get the lock " + key);
    }

    @Override
    public void lock() {
        DistLockDo d = new DistLockDo().setLockKey(key).setCreatedTime(System.currentTimeMillis());
        int count = 1;
        while (true){
            try {
                if (tryAddLock(d))
                    return;
                retryDelay(key, count);
            } catch (DalException e) {
                retryDelay(key, count);
            }
        }
    }

    @Override
    public void unlock() {
        try {
            DistLockDo d = new DistLockDo().setLockKey(key);
            if (unlock(d))
                return;
            for (int i = 1; i < MAX_RETRIES; i++) {
                try {
                    if (unlock(d))
                        return;
                    retryDelay(key, i);
                } catch (DalException e) {
                    retryDelay(key, i);
                }
            }
        } catch (DalException e) {
            logger.warn("Fail to unlock the lock " + key);
        }
    }

    private boolean tryAddLock(DistLockDo d) throws DalException {
        if (compareAndSetState(false, true)) {
            if (distLockDao.getByKey(d.getLockKey(), DistLockEntity.READSET_FULL) == null) {
                distLockDao.insert(d);
                return true;
            }
            compareAndSetState(true, false);
        }
        return false;
    }

    private boolean unlock(DistLockDo d) throws DalException {
        if (compareAndSetState(true, false)) {
            int count = distLockDao.deleteByKey(d);
            if (count == 1)
                return true;
            if (distLockDao.getByKey(d.getLockKey(), DistLockEntity.READSET_FULL) == null)
                return true;
            compareAndSetState(false, true);
        }
        return false;
    }

    private void retryDelay(String key, int attemptCount) {
        if (attemptCount > 0) {
            try {
                Thread.sleep(attemptCount * SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                logger.debug("Fail to sleep between locking retries with the lock named " + key);
            }
        }
    }

    private final boolean compareAndSetState(boolean expected, boolean updated) {
        if (state == expected) {
            state = updated;
            return true;
        }
        return false;
    }
}
