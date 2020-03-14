package com.ctrip.zeus.lock.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhoumy on 2017/2/28.
 */
public class LockScavenger {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExecutorService scavenger;

    public LockScavenger() {
        this.scavenger = Executors.newSingleThreadExecutor();
    }

    public void collect(final MysqlDistLock lock) {
        scavenger.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(60 * 1000L);
                } catch (InterruptedException e) {
                }
                if (lock.unlock()) {
                    logger.info("Successfully unlock the lock " + lock.getKey() + ".");
                }
            }
        });
    }
}
