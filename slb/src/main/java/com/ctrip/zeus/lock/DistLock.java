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
     * After the max attempt is reached, it will return regardless the result.
     */
    boolean tryLock();

    /**
     * Acquire the lock with timeout.
     *
     * If the lock is currently not available, the current thread sleeps and will retry after a certain delay.
     * Exception will be thrown if the lock cannot be acquired after timeout.
     *
     * @param timeout the time to wait for a lock
     * @throws Exception
     */
    void lock(int timeout) throws Exception;

    /**
     * Acquire the lock.
     *
     * The thread holds until the lock is acquired.
     */
    void lock();

    /**
     * Release the lock.
     */
    boolean unlock();

    String getKey();
}