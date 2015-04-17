package com.ctrip.zeus.lock.impl;

import com.ctrip.zeus.dal.core.DistLockDao;
import com.ctrip.zeus.dal.core.DistLockDo;
import com.ctrip.zeus.dal.core.DistLockEntity;
import com.ctrip.zeus.lock.DistLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zhoumy on 2015/4/9.
 */
@Component("mysqlDistLock")
public class MysqlDistLock implements DistLock {
    private static final int MAX_RETRIES = 3;
    private static final long SLEEP_INTERVAL = 500L;

    @Resource
    private DistLockDao distLockDao;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Lock dbWriteLock = new ReentrantLock();

    @Override
    public boolean tryLock(String key) {
        return tryLock(key, -1);
    }

    @Override
    public boolean tryLock(String key, int timeout) {
        DistLockDo d = new DistLockDo().setLockKey(key)
                .setTimeout(timeout).setCreatedTime(System.currentTimeMillis());
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                DistLockDo existed = distLockDao.getByKey(key, DistLockEntity.READSET_FULL);
                if (existed == null) {
                    if (tryAddLock(d))
                        return true;
                } else {
                    if (isExpired(existed)) {
                        if (tryReplaceExpiredLock(d))
                            return true;
                    }
                }
                retryDelay(key, i);
            } catch (DalException e) {
                retryDelay(key, i);
            }
        }
        logger.warn("Unable to create the lock " + key);
        return false;
    }

    private boolean tryAddLock(DistLockDo d) throws DalException {
        dbWriteLock.lock();
        if (distLockDao.getByKey(d.getLockKey(), DistLockEntity.READSET_FULL) == null) {
            distLockDao.insert(d);
            dbWriteLock.unlock();
            return true;
        } else {
            dbWriteLock.unlock();
        }
        return false;
    }

    private boolean tryReplaceExpiredLock(DistLockDo d) throws DalException {
        dbWriteLock.lock();
        DistLockDo existed = distLockDao.getByKey(d.getLockKey(), DistLockEntity.READSET_FULL);
        if (isExpired(existed)) {
            distLockDao.updateByKey(d, DistLockEntity.UPDATESET_FULL);
            dbWriteLock.unlock();
            return true;
        } else {
            dbWriteLock.unlock();
        }
        return false;
    }

    @Override
    public void unlock(String key) {
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

    public static boolean isExpired(DistLockDo distLock) {
        return distLock.getTimeout() > -1
                && (System.currentTimeMillis() > distLock.getCreatedTime() + distLock.getTimeout() * 1000);
    }

    private boolean unlock(DistLockDo d) throws DalException {
        dbWriteLock.lock();
        int count = distLockDao.deleteByKey(d);
        dbWriteLock.unlock();

        if (count == 1)
            return true;
        if (distLockDao.getByKey(d.getLockKey(), DistLockEntity.READSET_FULL) == null)
            return true;
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
}
