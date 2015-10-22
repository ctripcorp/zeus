package com.ctrip.zeus;

import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.server.SlbAdminServer;
import com.ctrip.zeus.util.S;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.MysqlDbServer;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhoumy on 2015/10/22.
 */
public class GracefulShutdownTest {
    private static SlbAdminServer server;
    private static MysqlDbServer mysqlDbServer;
    private static CountDownLatch latch = new CountDownLatch(2);

    @BeforeClass
    public static void setup() throws Exception {
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();

        S.setPropertyDefaultValue("archaius.deployment.applicationId", "slb-admin");
        S.setPropertyDefaultValue("archaius.deployment.environment", "local");
        S.setPropertyDefaultValue("server.www.base-dir", new File("").getAbsolutePath() + "/src/main/www");
        S.setPropertyDefaultValue("server.temp-dir", new File("").getAbsolutePath() + "/target/temp");
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        S.setPropertyDefaultValue("server.spring.context-file", "test-spring-context.xml");
        System.setProperty("activate.writable", "false");
    }

    @Test
    public void testGracefulReleaseLock() throws Exception {
        server = new SlbAdminServer();
        server.start();

        Thread th1 = new Thread() {
            @Override
            public void run() {
                TestClient testClient = new TestClient();
                System.out.println(testClient.requestSleep(25000L));
                latch.countDown();
            }
        };
        Thread th2 = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(5000L);
                    server.close();
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        th1.start();
        th2.start();

        latch.await();
    }

    @AfterClass
    public static void teardown() throws InterruptedException, ComponentLifecycleException, ComponentLookupException {
        mysqlDbServer.stop();
        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }

    public class TestClient extends AbstractRestClient {
        public TestClient() {
            this("http://127.0.0.1:8099");
        }

        protected TestClient(String url) {
            super(url);
        }

        public String requestSleep(Long sleep) {
            return getTarget().path("/api/test/sleep").queryParam("interval", sleep).request(MediaType.APPLICATION_JSON)
                    .headers(getDefaultHeaders()).get(String.class);
        }
    }
}
