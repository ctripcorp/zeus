package com.ctrip.zeus.lock;

import com.ctrip.zeus.lock.impl.MysqlDistLock;
import com.ctrip.zeus.util.S;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.AbstractSpringTest;
import support.MysqlDbServer;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by zhoumy on 2015/4/14.
 */
public class DistLockTest extends AbstractSpringTest {
    private static MysqlDbServer mysqlDbServer;
    @Resource
    MysqlDistLock mysqlDistLock;

    @BeforeClass
    public static void setUpDb() throws ComponentLookupException, ComponentLifecycleException {
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }

    @Test
    public void testSequentialLocking() throws InterruptedException {
        final List<Boolean> report = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(5);
        new Thread() {
            public void run() {
                // slock tryLock in th1 should be true
                report.add(mysqlDistLock.tryLock("slock"));
                latch.countDown();
            }
        }.run();
        new Thread() {
            public void run() {
                // slock tryLock in th2 should be false
                report.add(mysqlDistLock.tryLock("slock"));
                // slock1 tryLock in th2 should be true
                report.add(mysqlDistLock.tryLock("slock1"));
                mysqlDistLock.unlock("slock1");
                latch.countDown();
            }
        }.run();
        new Thread() {
            public void run() {
                // slock1 tryLock with timeout 1s in th3 should be true
                report.add(mysqlDistLock.tryLock("slock1", 5));
                latch.countDown();
            }
        }.run();
        new Thread() {
            public void run() {
                // slock1 tryLock in th4 should be false
                report.add(mysqlDistLock.tryLock("slock1"));
                latch.countDown();
            }
        }.run();
        System.out.println("We wait 5s");
        Thread.sleep(5000);
        new Thread() {
            public void run() {
                // slock1 tryLock in th5 should be true
                report.add(mysqlDistLock.tryLock("slock1"));
                latch.countDown();
            }
        }.run();

        latch.await();
        Assert.assertEquals(6, report.size());
        Assert.assertTrue(report.get(0));
        Assert.assertFalse(report.get(1));
        Assert.assertTrue(report.get(2));
        Assert.assertTrue(report.get(3));
        Assert.assertFalse(report.get(4));
        Assert.assertTrue(report.get(5));
    }

    @Test
    public void testConcurrentLocking() throws ExecutionException, InterruptedException {
        final List<Boolean> report = new ArrayList<>();
        final int totalThreads = 20;
        ExecutorService es = Executors.newFixedThreadPool(totalThreads);

        List<Future<?>> fs = new ArrayList<>();
        for (int i = 0; i < totalThreads; i++) {
            fs.add(es.submit(new Runnable() {
                @Override
                public void run() {
                    report.add(mysqlDistLock.tryLock("clock"));
                }
            }));
        }
        for (Future f : fs) {
            f.get();
        }
        Assert.assertEquals(totalThreads, report.size());
        Assert.assertTrue(report.get(0));
        for (int i = 1; i < report.size(); i++) {
            Assert.assertFalse(report.get(i));
        }
        es.shutdown();
    }

    @AfterClass
    public static void tearDownDb() throws InterruptedException, ComponentLookupException, ComponentLifecycleException {
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }
}