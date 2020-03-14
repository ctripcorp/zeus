package com.ctrip.zeus.lock.impl;

import com.ctrip.zeus.dao.entity.DistLockExample;
import com.ctrip.zeus.dao.mapper.DistLockMapper;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2015/4/9.
 */
public class MysqlDistLock implements DistLock {
    private static final int MAX_RETRIES = DynamicPropertyFactory.getInstance().getIntProperty("lock.retry.count", 3).get();
    private static final long SLEEP_INTERVAL = DynamicPropertyFactory.getInstance().getLongProperty("lock.sleep.interval", 300L).get();

    private final String key;
    private LockScavenger lockScavenger;
    private final DistLockMapper distLockMapper;
    private final DataSourceTransactionManager mybatisDataSourceTransactionManager;

    private AtomicBoolean state = new AtomicBoolean(false);
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public MysqlDistLock(String key, DbLockFactory dbLockFactory) {
        this.key = key;
        this.state.set(false);
        this.lockScavenger = dbLockFactory.getLockScavenger();
        this.distLockMapper = dbLockFactory.getDistLockMapper();
        this.mybatisDataSourceTransactionManager = dbLockFactory.getMybatisTransactionManager();
    }

    @Override
    public boolean tryLock() {
        long now = System.nanoTime();
        com.ctrip.zeus.dao.entity.DistLock d = com.ctrip.zeus.dao.entity.DistLock.builder().lockKey(key).owner(Thread.currentThread().getId()).server(S.getIp()).createdTime(System.currentTimeMillis()).build();
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                if (tryAddLock(d)) {
                    logger.info("Successfully tryLock the key " + key + ". Cost " + (System.nanoTime() - now) / 1000000 + ".");
                    return true;
                }
                retryDelay(key, i);
            } catch (Exception e) {
                retryDelay(key, i);
            }
        }
        logger.debug("Unable to create the lock " + key);
        return false;
    }

    @Override
    public void lock(int timeout) throws Exception {
        long now = System.nanoTime();
        long end = System.currentTimeMillis() + timeout;
        com.ctrip.zeus.dao.entity.DistLock d = com.ctrip.zeus.dao.entity.DistLock.builder().
                lockKey(key).owner(Thread.currentThread().getId()).
                server(S.getIp()).
                createdTime(System.currentTimeMillis()).build();

        while (System.currentTimeMillis() < end) {
            try {
                if (tryAddLock(d)) {
                    logger.info("Successfully lock(timeout) the key " + key + ". Cost " + (System.nanoTime() - now) / 1000000 + ".");
                    return;
                }
                retryDelay(key, 1);
            } catch (Exception e) {
                retryDelay(key, 1);
            }
        }
        throw new Exception("Fail to get the lock " + key);
    }

    @Override
    public void lock() {
        long now = System.nanoTime();
        com.ctrip.zeus.dao.entity.DistLock d = com.ctrip.zeus.dao.entity.DistLock.builder().
                lockKey(key).
                owner(Thread.currentThread().getId()).
                server(S.getIp()).
                createdTime(System.currentTimeMillis()).build();

        int count = 1;
        while (true) {
            try {
                if (tryAddLock(d)) {
                    logger.info("Successfully lock the key " + key + ". Cost " + (System.nanoTime() - now) / 1000000 + ".");
                    return;
                }
                retryDelay(key, count);
            } catch (Exception e) {
                retryDelay(key, count);
            }
        }
    }

    @Override
    public boolean unlock() {
        com.ctrip.zeus.dao.entity.DistLock d = com.ctrip.zeus.dao.entity.DistLock.builder().lockKey(key).build();
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

    public static boolean isFree(com.ctrip.zeus.dao.entity.DistLock d) {
        return d == null || ("".equals(d.getServer()) && d.getOwner() == 0L);
    }

    private boolean tryAddLock(com.ctrip.zeus.dao.entity.DistLock d) throws Exception {
        if (compareAndSetState(false, true)) {
            boolean result = false;
            try {
                com.ctrip.zeus.dao.entity.DistLock check = distLockMapper.selectOneByExample(new DistLockExample().createCriteria().andLockKeyEqualTo(d.getLockKey()).example());
                if (check == null || isFree(check)) {
                    TransactionStatus txStatus = mybatisDataSourceTransactionManager.getTransaction(new DefaultTransactionDefinition());
                    try {
                        check = distLockMapper.selectOneByExample(new DistLockExample().createCriteria().andLockKeyEqualTo(d.getLockKey()).example());
                        com.ctrip.zeus.dao.entity.DistLock distLock = new com.ctrip.zeus.dao.entity.DistLock();
                        distLock.setLockKey(d.getLockKey());
                        distLock.setOwner(d.getOwner());
                        distLock.setServer(d.getServer());
                        distLock.setCreatedTime(d.getCreatedTime());
                        if (check == null) {
                            distLockMapper.insert(distLock);
                            mybatisDataSourceTransactionManager.commit(txStatus);
                            result = true;
                        } else if (isFree(check)) {
                            result = distLockMapper.updateByExampleSelective(distLock, new DistLockExample().createCriteria().andLockKeyEqualTo(d.getLockKey())
                                    .andOwnerEqualTo(0L).andServerEqualTo("").example()) == 1;
                            mybatisDataSourceTransactionManager.commit(txStatus);
                        } else {
                            mybatisDataSourceTransactionManager.commit(txStatus);
                        }
                    } catch (Throwable throwable) {
                        result = false;
                        mybatisDataSourceTransactionManager.rollback(txStatus);
                        throw throwable;
                    }
                }
            } catch (Exception ex) {
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

    private boolean unlock(com.ctrip.zeus.dao.entity.DistLock d) throws Exception {
        d.setServer("");
        d.setOwner(0L);
        d.setCreatedTime(System.currentTimeMillis());

        if (compareAndSetState(true, false)) {
            try {
                com.ctrip.zeus.dao.entity.DistLock lock = new com.ctrip.zeus.dao.entity.DistLock();
                lock.setServer(d.getServer());
                lock.setOwner(d.getOwner());
                lock.setLockKey(d.getLockKey());
                lock.setCreatedTime(d.getCreatedTime());
                int count = distLockMapper.updateByExampleSelective(lock, new DistLockExample().createCriteria().andLockKeyEqualTo(lock.getLockKey()).example());
                if (count == 1) return true;
                lock = distLockMapper.selectOneByExample(new DistLockExample().createCriteria().andLockKeyEqualTo(d.getLockKey()).example());
                if (lock == null || isFree(lock)) return true;
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
