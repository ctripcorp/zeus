package com.ctrip.zeus.lock;

/**
 * Created by zhoumy on 2015/4/8.
 */
public interface DistLock {

    /**
     * Try to acquire the lock.
     *
     * If the lock is currently not available, the current thread sleeps and will retry after a certain delay.
     * The number of attempts trying to get the lock is limited,
     * After the max attempt is reach, it will return regardless the result.
     *
     * @param key the lock key. It should be unique.
     */
    boolean tryLock(String key);

    /**
     * Try to acquire the lock. The lock will automatically expire after the timeout.
     *
     * If the lock is currently not available, the current thread sleeps and will retry after a certain delay.
     * The number of attempts trying to get the lock is limited,
     * After the max attempt is reach, it will return regardless the result.
     *
     * @param key the lock key. It should be unique.
     * @param timeout expire time interval. Permanent if negative number is set, in which case this call would be the same as tryLock(String key).
     */
    boolean tryLock(String key, int timeout);

    /**
     * Unlock the lock.
     *
     * If lock cannot be unlocked at the moment, the current thread sleeps and will retry after a certain delay.
     * The number of attempts trying to get the lock is limited,
     * After the max attempt is reach, it will return regardless the result.
     * If the lock does not exist, nothing will happen.
     *
     * @param key the lock key.
     */
    void unlock(String key);
}