package com.ctrip.zeus.service;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.service.model.AppRepository;
import com.ctrip.zeus.service.model.SlbRepository;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/3/24.
 */
public class ModelServiceTest extends AbstractSpringTest {

    private static MysqlDbServer mysqlDbServer;

    @Resource
    private AppRepository appRepo;
    @Resource
    private SlbRepository slbRepo;

    private Slb defaultSlb;
    private App testApp;
    private long insertedTestAppId;

    @BeforeClass
    public static void setUpDb() throws ComponentLookupException, ComponentLifecycleException {
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }

    @Before
    public void fillDb() {
        addSlb();
        addApps();
    }

    /********************* test SlbRepository *********************/

    @Test
    public void testListSlbs() {
        try {
            List<Slb> list = slbRepo.list();
            Assert.assertTrue(list.size() >= 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSlb() {
        try {
            Slb slb = slbRepo.get(defaultSlb.getName());
            assertSlbEquals(defaultSlb, slb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSlbBySlbServer() {
        try {
            Slb slb = slbRepo.getBySlbServer(defaultSlb.getVips().get(0).getIp());
            assertSlbEquals(defaultSlb, slb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListSlbsByAppServerAndAppName() {
        try {
            List<Slb> slbs = slbRepo.listByAppServerAndAppName("10.2.6.201", "testApp");
            Assert.assertEquals(1, slbs.size());
            assertSlbEquals(defaultSlb, slbs.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateSlb() {
        try {
            Slb originSlb = slbRepo.get(defaultSlb.getName());
            originSlb.setStatus("HANG");
            slbRepo.update(originSlb);
            Slb updatedSlb = slbRepo.get(defaultSlb.getName());
            assertSlbEquals(originSlb, updatedSlb);
            Assert.assertEquals(originSlb.getVersion().intValue() + 1, updatedSlb.getVersion().intValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListAppServersBySlb() {
        try {
            List<String> appServers = slbRepo.listAppServersBySlb(defaultSlb.getName());
            Assert.assertEquals(testApp.getAppServers().size(), appServers.size());

            List<String> appServersRef = new ArrayList<>();
            for(AppServer as : testApp.getAppServers()) {
                appServersRef.add(as.getIp());
            }
            Assert.assertFalse(appServersRef.retainAll(appServers));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSlb() {
        defaultSlb = new Slb().setName("default").setVersion(1)
                .setNginxBin("/opt/app/nginx/sbin").setNginxConf("/opt/app/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("10.2.25.93"))
                .addSlbServer(new SlbServer().setIp("10.2.25.93").setHostName("uat0358").setEnable(true))
                .addSlbServer(new SlbServer().setIp("10.2.25.94").setHostName("uat0359").setEnable(true))
                .addSlbServer(new SlbServer().setIp("10.2.25.95").setHostName("uat0360").setEnable(true))
                .addVirtualServer(new VirtualServer().setName("testsite1").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("s1.ctrip.com")))
                .addVirtualServer(new VirtualServer().setName("testsite2").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("s2b.ctrip.com")));
        try {
            slbRepo.add(defaultSlb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSlb() {
        String slbName = "default";
        try {
            Assert.assertEquals(1, slbRepo.delete(slbName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /********************* test AppRepository *********************/

    @Test
    public void testGetApp() {
        try {
            App app = appRepo.get(testApp.getName());
            assertAppEquals(app, testApp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAppByAppId() {
        try {
            App app = appRepo.getByAppId(testApp.getAppId());
            assertAppEquals(app, testApp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListApps() {
        try {
            List<App> list = appRepo.list();
            Assert.assertTrue(list.size() >= 7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListLimitApps() {
        try {
            List<App> list = appRepo.listLimit(insertedTestAppId, 3);
            Assert.assertTrue(list.size() == 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListAppsBy() {
        String slbName = "default";
        String virtualServerName = "testsite1";
        try {
            List<App> list = appRepo.list(slbName, virtualServerName);
            Assert.assertTrue(list.size() >= 6);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateApp() {
        try {
            App originApp = appRepo.get(testApp.getName());
            originApp.setAppId("921812");
            appRepo.update(originApp);
            App updatedApp = appRepo.get(originApp.getName());
            assertAppEquals(originApp, updatedApp);
            Assert.assertEquals(originApp.getVersion().intValue() + 1, updatedApp.getVersion().intValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListAppsByAppServer() {
        try {
            List<String> appNames = appRepo.listAppsByAppServer(testApp.getAppServers().get(0).getIp());
            boolean containsTestApp = false;
            for (String appName : appNames) {
                if(appName.equals(testApp.getName()))
                    containsTestApp = true;
            }
            Assert.assertTrue(containsTestApp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListAppServersByApp() {
        try {
            List<String> appServers = appRepo.listAppServersByApp(testApp.getName());
            List<String> appServersRef = new ArrayList<>();
            for(AppServer as : testApp.getAppServers()) {
                appServersRef.add(as.getIp());
            }
            Assert.assertFalse(appServersRef.retainAll(appServers));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addApps() {
        testApp = generateApp("testApp", defaultSlb.getName(), defaultSlb.getVirtualServers().get(1));
        try {
            insertedTestAppId = appRepo.add(testApp);
            Assert.assertTrue(insertedTestAppId > 0);
            for(int i = 0; i < 6; i++) {
                App app = generateApp("testApp" + i, defaultSlb.getName(), defaultSlb.getVirtualServers().get(0));
                appRepo.add(app);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private App generateApp(String appName, String slbName, VirtualServer virtualServer) {
        return new App().setName(appName).setAppId("000000").setVersion(1)
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbName).setPath("/").setVirtualServer(virtualServer))
                .addAppServer(new AppServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.201"))
                .addAppServer(new AppServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.202"));
    }

    private void deleteApps() {
        String appName = "testApp";
        try {
            Assert.assertEquals(1, appRepo.delete(appName));
            for (int i = 0; i < 6; i++) {
                Assert.assertEquals(1, appRepo.delete(appName + i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /********************* test end *********************/

    @After
    public void clearDb() {
        deleteApps();
        deleteSlb();
    }

    @AfterClass
    public static void tearDownDb() throws InterruptedException, ComponentLookupException, ComponentLifecycleException {
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }

    private static void assertAppEquals(App origin, App another) {
        Assert.assertNotNull(another);
        Assert.assertEquals(origin.getName(), another.getName());
        Assert.assertEquals(origin.getAppServers().size(), another.getAppServers().size());
        Assert.assertEquals(origin.getAppSlbs().size(), another.getAppSlbs().size());
        Assert.assertEquals(origin.getHealthCheck().getUri(), another.getHealthCheck().getUri());
        Assert.assertEquals(origin.getLoadBalancingMethod().getType(), another.getLoadBalancingMethod().getType());
    }

    private static void assertSlbEquals(Slb origin, Slb another) {
        Assert.assertNotNull(another);
        Assert.assertEquals(origin.getName(), another.getName());
        Assert.assertEquals(origin.getNginxBin(), another.getNginxBin());
        Assert.assertEquals(origin.getNginxConf(), another.getNginxConf());
        Assert.assertEquals(origin.getNginxWorkerProcesses(), another.getNginxWorkerProcesses());
        Assert.assertEquals(origin.getStatus(), another.getStatus());
        Assert.assertEquals(origin.getSlbServers().size(), another.getSlbServers().size());
        Assert.assertEquals(origin.getVips().size(), another.getVips().size());
        Assert.assertEquals(origin.getVirtualServers().size(), another.getVirtualServers().size());
    }

    private static App parseApp(String appJsonData)  {
        App app = null;
        try {
            app = DefaultJsonParser.parse(App.class, appJsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return app;
    }

    private static Slb parseSlb(String slbJsonData) {
        Slb slb = null;
        try {
            slb = DefaultJsonParser.parse(Slb.class, slbJsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return slb;
    }
}