package com.ctrip.zeus.service;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.util.ModelAssert;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/3/24.
 */
public class ModelServiceTest extends AbstractSpringTest {

    private static MysqlDbServer mysqlDbServer;

    @Resource
    private GroupRepository groupRepo;
    @Resource
    private SlbRepository slbRepo;
    @Resource
    private ArchiveService archiveService;

    private Slb defaultSlb;
    private Group testGroup;
    private long insertedTestGroupId;

    @BeforeClass
    public static void setUpDb() throws ComponentLookupException, ComponentLifecycleException {
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }

    @Before
    public void fillDb() throws Exception {
        addSlb();
        addGroups();
    }

    /********************* test SlbRepository *********************/

    @Test
    public void testListSlbs() throws Exception {
        List<Slb> list = slbRepo.list();
        Assert.assertTrue(list.size() >= 1);
    }

    @Test
    public void testGetSlb() throws Exception {
        Slb slb = slbRepo.get(defaultSlb.getName());
        ModelAssert.assertSlbEquals(defaultSlb, slb);
    }

    @Test
    public void testGetSlbBySlbServer() throws Exception {
        Slb slb = slbRepo.getBySlbServer(defaultSlb.getVips().get(0).getIp());
        ModelAssert.assertSlbEquals(defaultSlb, slb);
    }

    @Test
    public void testListSlbsByGroupServerAndGroupName() throws Exception {
        List<Slb> slbsByGroupServer = slbRepo.listByGroupServerAndGroup("10.2.6.201", null);
        Assert.assertEquals(1, slbsByGroupServer.size());
        List<Slb> slbsByGroupName = slbRepo.listByGroupServerAndGroup(null, testGroup.getId());
        Assert.assertEquals(1, slbsByGroupName.size());
        List<Slb> slbs = slbRepo.listByGroupServerAndGroup("10.2.6.201", testGroup.getId());
        Assert.assertEquals(1, slbs.size());
        ModelAssert.assertSlbEquals(defaultSlb, slbs.get(0));
    }

    @Test
    public void testListSlbsByGroups() throws Exception {
        List<Slb> slbs = slbRepo.listByGroups(new Long[] {testGroup.getId(), testGroup.getId() + 1});
        Assert.assertEquals(1, slbs.size());
    }

    @Test
    public void testListGroupSlbsByGroups() throws Exception {
        List<GroupSlb> groupSlbs = slbRepo.listGroupSlbsByGroups(new Long[] {testGroup.getId(), testGroup.getId() + 1});
        Assert.assertEquals(2, groupSlbs.size());
        for (GroupSlb as : groupSlbs) {
            Assert.assertNotNull(as.getVirtualServer());
        }
    }

    @Test
    public void testListGroupSlbsBySlb() throws Exception {
        List<GroupSlb> groupSlbs = slbRepo.listGroupSlbsBySlb(defaultSlb.getId());
        Assert.assertEquals(7, groupSlbs.size());
        for (GroupSlb as : groupSlbs) {
            Assert.assertNotNull(as.getVirtualServer());
        }
    }

    @Test
    public void testUpdateSlb() throws Exception {
        Slb originSlb = slbRepo.get(defaultSlb.getName());
        originSlb.setStatus("HANG");
        slbRepo.update(originSlb);
        Slb updatedSlb = slbRepo.get(defaultSlb.getName());
        ModelAssert.assertSlbEquals(originSlb, updatedSlb);
        Assert.assertEquals(originSlb.getVersion().intValue() + 1, updatedSlb.getVersion().intValue());
    }

    @Test
    public void testListGroupServersBySlb() throws Exception {
        List<String> groupServers = slbRepo.listGroupServersBySlb(defaultSlb.getName());
        Assert.assertEquals(testGroup.getGroupServers().size(), groupServers.size());

        List<String> groupServersRef = new ArrayList<>();
        for (GroupServer as : testGroup.getGroupServers()) {
            groupServersRef.add(as.getIp());
        }
        Assert.assertFalse(groupServersRef.retainAll(groupServers));
    }

    private void addSlb() throws Exception {
        defaultSlb = new Slb().setName("default").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("10.2.25.93"))
                .addSlbServer(new SlbServer().setIp("10.2.25.93").setHostName("uat0358").setEnable(true))
                .addSlbServer(new SlbServer().setIp("10.2.25.94").setHostName("uat0359").setEnable(true))
                .addSlbServer(new SlbServer().setIp("10.2.25.95").setHostName("uat0360").setEnable(true))
                .addVirtualServer(new VirtualServer().setName("testsite1").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("s1.ctrip.com")))
                .addVirtualServer(new VirtualServer().setName("testsite2").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("s2b.ctrip.com")));
        slbRepo.add(defaultSlb);
    }

    private void deleteSlb() throws Exception {
        Assert.assertEquals(1, slbRepo.delete(defaultSlb.getId()));
    }

    /********************* test GroupRepository *********************/

    @Test
    public void testGetGroup() throws Exception {
        Group group = groupRepo.get(testGroup.getName());
        ModelAssert.assertGroupEquals(testGroup, group);
    }

    @Test
    public void testGetGroupByAppId() throws Exception {
        Group group = groupRepo.getByAppId(testGroup.getAppId());
        ModelAssert.assertGroupEquals(testGroup, group);
    }

    @Test
    public void testListGroups() throws Exception {
        List<Group> list = groupRepo.list();
        Assert.assertTrue(list.size() >= 7);
    }

    @Test
    public void testListLimitGroups() throws Exception {
        List<Group> list = groupRepo.listLimit(insertedTestGroupId, 3);
        Assert.assertTrue(list.size() == 3);
    }

    @Test
    public void testListGroupsBy() throws Exception {
        String slbName = "default";
        String virtualServerName = "testsite1";
        List<Group> list = groupRepo.list(slbName, virtualServerName);
        Assert.assertTrue(list.size() >= 6);
    }

    @Test
    public void testUpdateGroup() throws Exception {
        Group originGroup = groupRepo.get(testGroup.getName());
        originGroup.setAppId("921812");
        groupRepo.update(originGroup);
        Group updatedGroup = groupRepo.get(originGroup.getName());
        ModelAssert.assertGroupEquals(originGroup, updatedGroup);
        Assert.assertEquals(originGroup.getVersion().intValue() + 1, updatedGroup.getVersion().intValue());
    }

    @Test
    public void testListGroupsByGroupServer() throws Exception {
        List<String> groupNames = groupRepo.listGroupsByGroupServer(testGroup.getGroupServers().get(0).getIp());
        boolean containsTestGroup = false;
        for (String groupName : groupNames) {
            if (groupName.equals(testGroup.getName()))
                containsTestGroup = true;
        }
        Assert.assertTrue(containsTestGroup);
    }

    @Test
    public void testListGroupServersByGroup() throws Exception {
        List<String> groupServers = groupRepo.listGroupServerIpsByGroup(testGroup.getId());
        List<String> groupServersRef = new ArrayList<>();
        for (GroupServer as : testGroup.getGroupServers()) {
            groupServersRef.add(as.getIp());
        }
        Assert.assertFalse(groupServersRef.retainAll(groupServers));

    }

    private void addGroups() throws Exception {
        testGroup = generateGroup("testGroup", defaultSlb.getName(), defaultSlb.getVirtualServers().get(1));
        insertedTestGroupId = groupRepo.add(testGroup);
        Assert.assertTrue(insertedTestGroupId > 0);
        for (int i = 0; i < 6; i++) {
            Group group = generateGroup("testGroup" + i, defaultSlb.getName(), defaultSlb.getVirtualServers().get(0));
            groupRepo.add(group);
        }
    }

    private Group generateGroup(String groupName, String slbName, VirtualServer virtualServer) {
        return new Group().setName(groupName).setAppId("000000").setVersion(1).setSsl(false)
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupSlb(new GroupSlb().setSlbName(slbName).setPath("/").setVirtualServer(virtualServer))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.201"))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.202"));
    }

    private void deleteGroups() throws Exception {
        Assert.assertEquals(1, groupRepo.delete(testGroup.getId()));
        for (int i = 0; i < 6; i++) {
            Assert.assertEquals(1, groupRepo.delete(testGroup.getId() + i));
        }
    }

    /********************* test ArchiveService *********************/

    @Test
    public void testGetLatestGroupArchive() throws Exception {
        Archive archive = archiveService.getLatestGroupArchive(testGroup.getId());
        Assert.assertTrue(archive.getVersion() > 0);
    }

    @Test
    public void testGetLatestSlbArchive() throws Exception {
        Archive archive = archiveService.getLatestSlbArchive(defaultSlb.getId());
        Assert.assertTrue(archive.getVersion() > 0);
    }

    /********************* test end *********************/

    @After
    public void clearDb() throws Exception {
        deleteGroups();
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
}