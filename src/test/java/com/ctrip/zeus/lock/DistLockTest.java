package com.ctrip.zeus.lock;

import com.ctrip.zeus.AbstractServerTest;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhoumy on 2015/4/14.
 */
public class DistLockTest extends AbstractServerTest {

    @Resource
    private DbLockFactory dbLockFactory;

    @Test
    public void testBasic() {
        DistLock lock = dbLockFactory.newLock("testBasic");
        lock.lock();
        lock.unlock();
        try {
            lock.lock(500);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        lock.unlock();
        Assert.assertTrue(lock.tryLock());
        lock.unlock();
    }

    @Test
    public void testFunctions() throws InterruptedException {
        final List<Boolean> report = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(4);
        new Thread() {
            public void run() {
                report.add(dbLockFactory.newLock("slock").tryLock() == true);
                latch.countDown();
            }
        }.run();
        new Thread() {
            public void run() {
                report.add(dbLockFactory.newLock("slock").tryLock() == false);
                DistLock lock = dbLockFactory.newLock("slock1");
                report.add(lock.tryLock() == true);
                lock.unlock();
                latch.countDown();
            }
        }.run();
        new Thread() {
            public void run() {
                dbLockFactory.newLock("slock1").lock();
                report.add(true);
                latch.countDown();
            }
        }.run();
        new Thread() {
            public void run() {
                try {
                    dbLockFactory.newLock("slock1").lock(3);
                    report.add(false);
                } catch (Exception e) {
                    report.add(true);
                }
                latch.countDown();
            }
        }.run();

        latch.await();
        Assert.assertEquals(5, report.size());

        for (Boolean result : report) {
            Assert.assertTrue(result);
        }
    }

    @Test
    public void testConcurrentScenario() throws ExecutionException, InterruptedException {
        final List<Boolean> report = new ArrayList<>();
        final int totalThreads = 20;
        ExecutorService es = Executors.newFixedThreadPool(totalThreads);

        List<Future<?>> lockOne = new ArrayList<>();
        for (int i = 0; i < totalThreads; i++) {
            lockOne.add(es.submit(new Runnable() {
                @Override
                public void run() {
                    report.add(dbLockFactory.newLock("clock").tryLock());
                }
            }));
        }
        for (Future f : lockOne) {
            f.get();
        }
        es.shutdown();
        Assert.assertEquals(totalThreads, report.size());
        Assert.assertTrue(report.get(0));
        for (int i = 1; i < report.size(); i++) {
            Assert.assertFalse(report.get(i));
        }

        final List<Boolean> report2 = new ArrayList<>();
        final AtomicLong interval = new AtomicLong(0);
        new Thread() {
            public void run() {
                DistLock lock = dbLockFactory.newLock("wait");
                lock.lock();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    report2.add(false);
                }
                report2.add(true);
                lock.unlock();
            }
        }.run();
        new Thread() {
            public void run() {
                DistLock lock = dbLockFactory.newLock("wait");
                long start = System.nanoTime();
                lock.lock();
                report2.add(true);
                lock.unlock();
                interval.set(System.nanoTime() - start);
            }
        }.run();
        Assert.assertEquals(report2.size(), 2);
        Assert.assertTrue(report2.get(0));
        Assert.assertTrue(report2.get(1));
        Assert.assertTrue(interval.get() > 10000);
    }

    @Test
    public void testIncorrectExecution() {
        // Assume unlock cannot be done using different instance.
        DistLock lock = dbLockFactory.newLock("mistake");
        lock.lock();
        DistLock anotherLock = dbLockFactory.newLock("mistake");
        anotherLock.unlock();
        Assert.assertFalse(anotherLock.tryLock());
        lock.unlock();
        Assert.assertTrue(anotherLock.tryLock());
        anotherLock.unlock();
    }

//    @Test
//    public void testAbnormalLock() {
//        DistLock lock = dbLockFactory.newLock("abnormal");
//        lock.lock();
//        MysqlDistLock dbLock = (MysqlDistLock) lock;
//        dbLock.replaceDistLockDao(new DistLockDao());
//        if (!dbLock.unlock()) {
//            dbLock.replaceDistLockDao(distLockDao);
//            Assert.assertFalse(dbLockFactory.newLock("abnormal").tryLock());
//            try {
//                Thread.sleep(60 * 1000L);
//            } catch (InterruptedException e) {
//            }
//        } else {
//            Assert.assertTrue(false);
//        }
//        Assert.assertTrue(dbLockFactory.newLock("abnormal").tryLock());
//    }
}