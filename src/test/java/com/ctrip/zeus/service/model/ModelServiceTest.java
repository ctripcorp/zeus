package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.util.S;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.*;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.AbstractSpringTest;
import support.MysqlDbServer;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * Created by zhoumy on 2015/3/24.
 */
public class ModelServiceTest extends AbstractSpringTest {

    private static MysqlDbServer mysqlDbServer;

    @Resource
    private AppRepository repo;

    private App testApp;
    private long insertedTestAppId;

    private static App parseApp(String appJsonData)  {
        App app = null;
        try {
            app = DefaultJsonParser.parse(App.class, appJsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return app;
    }

    private void batchAdd() {
        for(int i = 0; i < 6; i++) {
            String appJsonData1 = "{\"name\": \"testApp" + i + "\", \"app-id\": \"000000\", \"version\": 1, \"app-slbs\": [{\"slb-name\": \"default\", \"path\": \"/\", \"virtual-server\": {\"name\": \"testsite1\", \"ssl\": false, \"port\": \"80\", \"domains\": [{\"name\": \"tests1.ctrip.com\"} ] } } ], \"health-check\": {\"intervals\": 5000, \"fails\": 1, \"passes\": 1, \"uri\": \"/domaininfo/OnService.html\"}, \"load-balancing-method\": {\"type\": \"roundrobin\", \"value\": \"test\"}, \"app-servers\": [{\"port\": 8080, \"weight\": 1, \"max-fails\": 2, \"fail-timeout\": 30, \"ip\": \"10.2.6.201\"}, {\"port\": 8080, \"weight\": 2, \"max-fails\": 2, \"fail-timeout\": 30, \"ip\": \"10.2.6.202\"} ] }";
            repo.add(parseApp(appJsonData1));
        }
    }

    @BeforeClass
    public static void setup() throws ComponentLookupException, ComponentLifecycleException {
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }

    @Before
    public void setUp() {
        String appJsonData = "{\"name\": \"testApp\", \"app-id\": \"000000\", \"version\": 1, \"app-slbs\": [{\"slb-name\": \"default\", \"path\": \"/\", \"virtual-server\": {\"name\": \"testsite2\", \"ssl\": false, \"port\": \"80\", \"domains\": [{\"name\": \"tests2.ctrip.com\"} ] } } ], \"health-check\": {\"intervals\": 5000, \"fails\": 1, \"passes\": 1, \"uri\": \"/domaininfo/OnService.html\"}, \"load-balancing-method\": {\"type\": \"roundrobin\", \"value\": \"test\"}, \"app-servers\": [{\"port\": 8080, \"weight\": 1, \"max-fails\": 2, \"fail-timeout\": 30, \"ip\": \"10.2.6.201\"}, {\"port\": 8080, \"weight\": 2, \"max-fails\": 2, \"fail-timeout\": 30, \"ip\": \"10.2.6.202\"} ] }";
        testApp = parseApp(appJsonData);
        insertedTestAppId = repo.add(testApp);
        Assert.assertTrue(insertedTestAppId > 0);

        batchAdd();
    }

    @Test
    public void testGet() {
        App app = repo.get(testApp.getName());
        assertAppEquals(app, testApp);
    }

    @Test
    public void testGetByAppId() {
        App app = repo.getByAppId(testApp.getAppId());
        assertAppEquals(app, testApp);
    }

    @Test
    public void testList() {
        AppList list = repo.list();
        Assert.assertTrue(list.getApps().size() >= 7);
    }

    @Test
    public void testListLimit() {
        AppList list = repo.listLimit(insertedTestAppId, 3);
        Assert.assertTrue(list.getApps().size() == 3);
    }

    @Test
    public void testListBy() {
        String slbName = "default";
        String virtualServerName = "testsite2";
        AppList list = repo.list(slbName, virtualServerName);
        Assert.assertTrue(list.getApps().size() >= 6);
    }

    @Test
    public void testUpdate() {
        App toUpdateApp = parseApp("{\"name\": \"testApp\", \"app-id\": \"921812\", \"version\": 1, \"app-slbs\": [{\"slb-name\": \"default\", \"path\": \"/\", \"virtual-server\": {\"name\": \"site1\", \"ssl\": false, \"port\": \"80\", \"domains\": [{\"name\": \"s1.ctrip.com\"} ] } } ], \"health-check\": {\"intervals\": 5000, \"fails\": 1, \"passes\": 1, \"uri\": \"/domaininfo/OnService.html\"}, \"load-balancing-method\": {\"type\": \"roundrobin\", \"value\": \"test\"}, \"app-servers\": [{\"port\": 8080, \"weight\": 1, \"max-fails\": 2, \"fail-timeout\": 30, \"ip\": \"10.2.6.201\"}, {\"port\": 8080, \"weight\": 2, \"max-fails\": 2, \"fail-timeout\": 30, \"ip\": \"10.2.6.202\"} ] }");
        repo.update(toUpdateApp);
        App updatedApp = repo.get(toUpdateApp.getName());
        assertAppEquals(toUpdateApp, updatedApp);
    }

    @After
    public void tearDown() {
        String appName = "testApp";
        Assert.assertEquals(insertedTestAppId, repo.delete(appName));
        for (int i = 0; i < 6; i++) {
            Assert.assertEquals(insertedTestAppId + i, repo.delete(appName + insertedTestAppId));
        }
    }

    @AfterClass
    public static void teardown() throws InterruptedException, ComponentLookupException, ComponentLifecycleException {
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }

    private static void assertAppEquals(App origin, App another) {
        Assert.assertNotNull(origin);
        Assert.assertEquals(origin.getName(), another.getName());
        Assert.assertEquals(origin.getAppServers().size(), another.getAppServers().size());
        Assert.assertEquals(origin.getAppSlbs().size(), another.getAppSlbs().size());
        Assert.assertEquals(origin.getHealthCheck().getUri(), another.getHealthCheck().getUri());
        Assert.assertEquals(origin.getLoadBalancingMethod().getType(), another.getLoadBalancingMethod().getType());
    }
}