package com.ctrip.zeus.service;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
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
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private GroupMemberRepository groupMemberRepository;
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

    /**
     * ****************** test SlbRepository ********************
     */

    @Test
    public void testListSlbs() throws Exception {
        List<Slb> list = slbRepository.list();
        Assert.assertTrue(list.size() >= 1);
    }

    @Test
    public void testGetSlb() throws Exception {
        Slb slb = slbRepository.get(defaultSlb.getName());
        ModelAssert.assertSlbEquals(defaultSlb, slb);
    }

    @Test
    public void testGetSlbBySlbServer() throws Exception {
        Slb slb = slbRepository.getBySlbServer(defaultSlb.getVips().get(0).getIp());
        ModelAssert.assertSlbEquals(defaultSlb, slb);
    }

    @Test
    public void testGetSlbByVirtualServer() throws Exception {
        Slb slb = slbRepository.getByVirtualServer(defaultSlb.getVirtualServers().get(0).getId());
        ModelAssert.assertSlbEquals(defaultSlb, slb);
    }

    @Test
    public void testListSlbsByGroupServerAndGroup() throws Exception {
        List<Slb> slbsByGroupServer = slbRepository.listByGroupServerAndGroup("10.2.6.201", null);
        Assert.assertEquals(1, slbsByGroupServer.size());
        List<Slb> slbsByGroupName = slbRepository.listByGroupServerAndGroup(null, testGroup.getId());
        Assert.assertEquals(1, slbsByGroupName.size());
        List<Slb> slbs = slbRepository.listByGroupServerAndGroup("10.2.6.201", testGroup.getId());
        Assert.assertEquals(1, slbs.size());
        ModelAssert.assertSlbEquals(defaultSlb, slbs.get(0));
    }

    @Test
    public void testListSlbsByGroups() throws Exception {
        List<Slb> slbs = slbRepository.listByGroups(new Long[]{testGroup.getId(), testGroup.getId() + 1});
        Assert.assertEquals(1, slbs.size());
    }

    @Test
    public void testListGroupVirtualServerByGroups() throws Exception {
        List<GroupVirtualServer> groupVirtualServers = virtualServerRepository.listGroupVsByGroups(new Long[]{testGroup.getId(), testGroup.getId() + 1});
        Assert.assertEquals(2, groupVirtualServers.size());
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers) {
            Assert.assertNotNull(groupVirtualServer.getVirtualServer());
        }
    }

    @Test
    public void testListGroupVirtualServersBySlb() throws Exception {
        List<GroupVirtualServer> groupVirtualServers = virtualServerRepository.listGroupVsBySlb(defaultSlb.getId());
        Assert.assertEquals(7, groupVirtualServers.size());
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers) {
            Assert.assertNotNull(groupVirtualServer.getVirtualServer());
        }
    }

    @Test
    public void testUpdateSlb() throws Exception {
        Slb s = new Slb().setName("testUpdateSlb").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.11").setHostName("dev1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.12").setHostName("dev2"))
                .addSlbServer(new SlbServer().setIp("10.1.1.13").setHostName("dev3"))
                .addVirtualServer(new VirtualServer().setName("www.nihao.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.nihao.com")))
                .addVirtualServer(new VirtualServer().setName("www.hello.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.hello.com")));
        slbRepository.add(s);
        Slb newest = slbRepository.get(s.getName());
        s.setId(newest.getId());
        Assert.assertEquals(2, s.getVirtualServers().size());

        Slb updated = new Slb().setId(newest.getId()).setName("testUpdateSlb").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.11").setHostName("dev1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.12").setHostName("dev2"));
        slbRepository.update(updated);
        newest = slbRepository.get(s.getName());
        Assert.assertEquals(2, newest.getSlbServers().size());
        // Update slb should not update virtual servers
        Assert.assertEquals(2, newest.getVirtualServers().size());
        Assert.assertEquals(2, newest.getVersion().intValue());
    }

    @Test
    public void testUpdateVirtualServer() throws Exception {
        Slb s = new Slb().setName("testUpdateVirtualServer").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.11").setHostName("dev1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.12").setHostName("dev2"))
                .addSlbServer(new SlbServer().setIp("10.1.1.13").setHostName("dev3"))
                .addVirtualServer(new VirtualServer().setName("www.nihao.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.nihao.com")))
                .addVirtualServer(new VirtualServer().setName("www.hello.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.hello.com")));
        slbRepository.add(s);
        s = slbRepository.get(s.getName());

        virtualServerRepository.addVirtualServer(s.getId(), new VirtualServer().setSlbId(s.getId()).setName("www.bonjour.com").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("www.bonjour.com")));
        virtualServerRepository.addVirtualServer(s.getId(), new VirtualServer().setSlbId(s.getId()).setName("www.hallo.com").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("www.hallo.com")));

        List<VirtualServer> virtualServers = s.getVirtualServers();
        virtualServers.get(0).setName("www.nihao.com.cn");
        virtualServers.get(1).setName("www.hello.com.cn");
        virtualServerRepository.updateVirtualServers(virtualServers.toArray(new VirtualServer[virtualServers.size()]));
        Slb updated = slbRepository.get(s.getName());
        Assert.assertEquals(updated.getVirtualServers().size(), 4);

        virtualServerRepository.deleteVirtualServer(virtualServers.get(0).getId());
        virtualServerRepository.deleteVirtualServer(virtualServers.get(1).getId());

        updated = slbRepository.get(s.getName());
        Assert.assertEquals(updated.getVirtualServers().size(), 2);
        Assert.assertEquals(updated.getVirtualServers().get(0).getName(), "www.bonjour.com");
        Assert.assertEquals(updated.getVirtualServers().get(1).getName(), "www.hallo.com");

        Slb s2 = new Slb().setName("testUpdateVirtualServer2").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.11").setHostName("dev1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.12").setHostName("dev2"))
                .addSlbServer(new SlbServer().setIp("10.1.1.13").setHostName("dev3"))
                .addVirtualServer(new VirtualServer().setName("www.bonjour.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.bonjour.com")))
                .addVirtualServer(new VirtualServer().setName("www.hallo.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.hallo.com")))
                .addVirtualServer(new VirtualServer().setName("www.hello.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.hello.com")));
        slbRepository.add(s2);
        s2 = slbRepository.get(s2.getName());
        updated = slbRepository.get(s.getName());
        Assert.assertEquals(updated.getVirtualServers().size(), 2);
        Assert.assertEquals(s2.getVirtualServers().size(), 3);
    }

    @Test
    public void testListGroupServersBySlb() throws Exception {
        List<String> groupServers = groupMemberRepository.listGroupServersBySlb(defaultSlb.getId());
        Assert.assertEquals(testGroup.getGroupServers().size(), groupServers.size());

        List<String> groupServersRef = new ArrayList<>();
        for (GroupServer as : testGroup.getGroupServers()) {
            groupServersRef.add(as.getIp());
        }
        Assert.assertFalse(groupServersRef.retainAll(groupServers));
    }

    @Test(expected = ValidationException.class)
    public void testSlbRemovable() throws Exception {
        slbRepository.delete(defaultSlb.getId());
    }

    @Test(expected = ValidationException.class)
    public void testSlbModifiable() throws Exception {
        Slb targetSlb = slbRepository.get(defaultSlb.getName());
        VirtualServer vs = new VirtualServer().setName("testnew").setSsl(false).setPort("80").addDomain(new Domain().setName("s1new.ctrip.com"));
        targetSlb.addVirtualServer(vs);
        try {
            slbRepository.update(targetSlb);
        } catch (Exception e) {
            Assert.assertTrue("valid update action throws exception.", false);
        }
        targetSlb.getVirtualServers().remove(0);
        slbRepository.update(targetSlb);
    }

    private void addSlb() throws Exception {
        defaultSlb = new Slb().setName("default").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("10.2.25.93"))
                .addSlbServer(new SlbServer().setIp("10.2.25.93").setHostName("uat0358"))
                .addSlbServer(new SlbServer().setIp("10.2.25.94").setHostName("uat0359"))
                .addSlbServer(new SlbServer().setIp("10.2.25.95").setHostName("uat0360"))
                .addVirtualServer(new VirtualServer().setName("testsite1").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("s1.ctrip.com")))
                .addVirtualServer(new VirtualServer().setName("testsite2").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("s2b.ctrip.com")));
        slbRepository.add(defaultSlb);
        defaultSlb = slbRepository.get(defaultSlb.getName());
    }

    private void deleteSlb() throws Exception {
        Assert.assertEquals(1, slbRepository.delete(defaultSlb.getId()));
    }

    /**
     * ****************** test GroupRepository ********************
     */

    @Test
    public void testGetGroup() throws Exception {
        Group group = groupRepository.get(testGroup.getName());
        ModelAssert.assertGroupEquals(testGroup, group);
    }

    @Test
    public void testGetGroupByAppId() throws Exception {
        Group group = groupRepository.getByAppId(testGroup.getAppId());
        ModelAssert.assertGroupEquals(testGroup, group);
    }

    @Test
    public void testListGroups() throws Exception {
        List<Group> list = groupRepository.list();
        Assert.assertTrue(list.size() >= 7);
    }

    @Test
    public void testListGroupsBy() throws Exception {
        String virtualServerName = "testsite1";
        List<Group> list = groupRepository.list(defaultSlb.getId(), virtualServerName);
        Assert.assertTrue(list.size() >= 6);
    }

    @Test
    public void testUpdateGroup() throws Exception {
        Group originGroup = groupRepository.get(testGroup.getName());
        originGroup.setAppId("921812");
        originGroup.getGroupVirtualServers().get(0).setPriority(9);
        groupRepository.update(originGroup);
        Group updatedGroup = groupRepository.getById(originGroup.getId());
        ModelAssert.assertGroupEquals(originGroup, updatedGroup);
        Assert.assertEquals(originGroup.getVersion().intValue() + 1, updatedGroup.getVersion().intValue());
    }

    @Test
    public void testListGroupsByGroupServer() throws Exception {
        Long[] groupIds = groupMemberRepository.findGroupsByGroupServerIp(testGroup.getGroupServers().get(0).getIp());
        boolean containsTestGroup = false;
        for (Long groupId : groupIds) {
            if (groupId.equals(testGroup.getId())) {
                containsTestGroup = true;
                break;
            }
        }
        Assert.assertTrue(containsTestGroup);
    }

    @Test
    public void testListGroupServersByGroup() throws Exception {
        List<String> groupServers = groupMemberRepository.listGroupServerIpsByGroup(testGroup.getId());
        List<String> groupServersRef = new ArrayList<>();
        for (GroupServer as : testGroup.getGroupServers()) {
            groupServersRef.add(as.getIp());
        }
        Assert.assertFalse(groupServersRef.retainAll(groupServers));

    }

    private void addGroups() throws Exception {
        testGroup = generateGroup("testGroup", defaultSlb, defaultSlb.getVirtualServers().get(1));
        insertedTestGroupId = groupRepository.add(testGroup).getId();
        // set virtual server full information
        testGroup.getGroupVirtualServers().get(0).setPriority(1000).setVirtualServer(defaultSlb.getVirtualServers().get(1));
        testGroup.getGroupVirtualServers().get(0).getVirtualServer().setSlbId(defaultSlb.getId());
        Assert.assertTrue(insertedTestGroupId > 0);
        for (int i = 0; i < 6; i++) {
            Group group = generateGroup("testGroup" + i, defaultSlb, defaultSlb.getVirtualServers().get(0));
            groupRepository.add(group);
        }
    }

    private Group generateGroup(String groupName, Slb slb, VirtualServer virtualServer) {
        return new Group().setName(groupName).setAppId("000000").setVersion(1).setSsl(false)
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/" + groupName).setVirtualServer(new VirtualServer().setSlbId(slb.getId()).setId(virtualServer.getId())))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.201"))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.202"));
    }

    private void deleteGroups() throws Exception {
        Assert.assertEquals(1, groupRepository.delete(testGroup.getId()));
        for (int i = 1; i <= 6; i++) {
            Assert.assertEquals(1, groupRepository.delete(testGroup.getId() + i));
        }
    }

    /**
     * ****************** test ArchiveService ********************
     */

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

    /**
     * ****************** test end ********************
     */

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