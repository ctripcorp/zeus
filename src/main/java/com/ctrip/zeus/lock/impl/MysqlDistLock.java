package com.ctrip.zeus.lock.impl;

import com.ctrip.zeus.dal.core.DistLockDao;
import com.ctrip.zeus.dal.core.DistLockDo;
import com.ctrip.zeus.dal.core.DistLockEntity;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.transaction.TransactionManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2015/4/9.
 */
public class MysqlDistLock implements DistLock {
    private static final int MAX_RETRIES = DynamicPropertyFactory.getInstance().getIntProperty("lock.retry.count", 3).get();
    private static final long SLEEP_INTERVAL = DynamicPropertyFactory.getInstance().getLongProperty("lock.sleep.interval", 300L).get();

    private final String key;
    private DistLockDao distLockDao;
    private final TransactionManager transactionManager;
    private final String resourceName;
    private LockScavenger lockScavenger;

    private AtomicBoolean state = new AtomicBoolean(false);
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public MysqlDistLock(String key, DbLockFactory dbLockFactory) {
        this.key = key;
        this.state.set(false);
        this.distLockDao = dbLockFactory.getDao();
        this.transactionManager = dbLockFactory.getTransactionManager();
        this.resourceName = dbLockFactory.getResourceName();
        this.lockScavenger = dbLockFactory.getLockScavenger();
    }

    @Override
    public boolean tryLock() {
        DistLockDo d = new DistLockDo().setLockKey(key)
                .setOwner(Thread.currentThread().getId()).setServer(S.getIp())
                .setCreatedTime(System.currentTimeMillis());
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                if (tryAddLock(d))
                    return true;
                retryDelay(key, i);
            } catch (DalException e) {
                retryDelay(key, i);
            }
        }
        logger.debug("Unable to create the lock " + key);
        return false;
    }

    @Override
    public void lock(int timeout) throws Exception {
        long end = System.currentTimeMillis() + timeout;
        DistLockDo d = new DistLockDo().setLockKey(key)
                .setOwner(Thread.currentThread().getId()).setServer(S.getIp())
                .setCreatedTime(System.currentTimeMillis());
        while (System.currentTimeMillis() < end) {
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
        DistLockDo d = new DistLockDo().setLockKey(key)
                .setOwner(Thread.currentThread().getId()).setServer(S.getIp())
                .setCreatedTime(System.currentTimeMillis());
        int count = 1;
        while (true) {
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
    public boolean unlock() {
        DistLockDo d = new DistLockDo().setLockKey(key);
        try {
            if (unlock(d)) return true;
        } catch (Exception e) {
            logger.warn("Fail to unlock the lock " + key + ".", e);
        }
        for (int i = 1; i < MAX_RETRIES; i++) {
            try {
                if (unlock(d)) return true;
                retryDelay(key, i);
            } catch (Exception e) {
                retryDelay(key, i);
                logger.warn("Fail to unlock the lock " + key + ".", e);
            }
        }
        logger.error("Abnormal unlock tries. Fail to unlock the lock " + key + ".");
        lockScavenger.collect(this);
        return false;
    }

    @Override
    public String getKey() {
        return key;
    }

    public static boolean isFree(DistLockDo d) {
        return d == null || ("".equals(d.getServer()) && d.getOwner() == 0L);
    }

    private boolean tryAddLock(DistLockDo d) throws DalException {
        if (compareAndSetState(false, true)) {
            boolean result = false;
            try {
                DistLockDo check = distLockDao.getByKey(d.getLockKey(), DistLockEntity.READSET_FULL);
                if (check == null || isFree(check)) {
                    transactionManager.startTransaction(resourceName);
                    try {
                        check = distLockDao.getByKeyForUpdate(d.getLockKey(), DistLockEntity.READSET_FULL);
                        if (check == null) {
                            distLockDao.insert(d);
                            transactionManager.commitTransaction();
                            result = true;
                        } else if (isFree(check)) {
                            result = distLockDao.obtainByKey(d, DistLockEntity.UPDATESET_UPDATE_OWNER) == 1;
                            transactionManager.commitTransaction();
                        } else {
                            transactionManager.commitTransaction();
                        }
                    } catch (Throwable throwable) {
                        result = false;
                        transactionManager.rollbackTransaction();
                        throw new DalException("Rollback tryAddLock transaction.", throwable);
                    }
                }
            } catch (DalException ex) {
                result = false;
                throw ex;
            } finally {
                if (!result) {
                    compareAndSetState(true, false);
                }
            }
            return result;
        }
        return false;
    }

    private boolean unlock(DistLockDo d) throws Exception {
        d.setServer("").setOwner(0L).setCreatedTime(System.currentTimeMillis());
        if (compareAndSetState(true, false)) {
            try {
                int count = distLockDao.updateByKey(d, DistLockEntity.UPDATESET_UPDATE_OWNER);
                if (count == 1) return true;
                DistLockDo check = distLockDao.getByKey(d.getLockKey(), DistLockEntity.READSET_FULL);
                if (check == null || isFree(check)) return true;
            } catch (Exception ex) {
                compareAndSetState(false, true);
                throw ex;
            }
            compareAndSetState(false, true);
            return false;
        } else {
            return true;
        }
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

    private boolean compareAndSetState(boolean expected, boolean updated) {
        boolean success = state.compareAndSet(expected, updated);
        if (!success) {
            logger.warn("Abnormal state - expected: " + expected + ", but was " + !expected + ".");
        }
        return success;
    }
}
