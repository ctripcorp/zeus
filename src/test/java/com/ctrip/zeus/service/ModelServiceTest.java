package com.ctrip.zeus.service;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.util.ModelAssert;
import org.junit.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/3/24.
 */
public class ModelServiceTest extends AbstractServerTest {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private ArchiveService archiveService;

    private Slb defaultSlb;
    private Group testGroup;
    private long insertedTestGroupId;

    @Before
    public void fillDb() throws Exception {
        addSlb();
        addGroups();
    }

    @After
    public void clearDb() throws Exception {
        deleteGroups();
        deleteSlb();
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
        List<Slb> slbsByGroupServer = slbRepository.listByGroupServer("10.2.6.201");
        Assert.assertEquals(1, slbsByGroupServer.size());
    }

    @Test
    public void testListSlbsByGroups() throws Exception {
        List<Slb> slbs = slbRepository.listByGroups(new Long[]{testGroup.getId(), testGroup.getId() + 1});
        Assert.assertEquals(1, slbs.size());
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
        Assert.assertEquals(2, newest.getVirtualServers().size());

        Slb updated = new Slb().setId(newest.getId()).setName("testUpdateSlb").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.111").setHostName("dev1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.121").setHostName("dev2"));
        slbRepository.update(updated);
        newest = slbRepository.get(s.getName());
        Assert.assertEquals(2, newest.getSlbServers().size());
        // Update slb should not update virtual servers
        Assert.assertEquals(2, newest.getVirtualServers().size());
        Assert.assertEquals(2, newest.getVersion().intValue());
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
        virtualServerRepository.delete(targetSlb.getVirtualServers().get(0).getId());
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
        Assert.assertEquals(0, virtualServerCriteriaQuery.queryBySlbId(defaultSlb.getId()).size());
    }

    /**
     * ****************** test GroupRepository ********************
     */

    @Test
    public void testGetGroup() throws Exception {
        Group group = groupRepository.get(testGroup.getName());
        ModelAssert.assertGroupEquals(group, testGroup);
    }

    @Test
    public void testGetGroupByAppId() throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryByAppId(testGroup.getAppId());
        List<Group> list = groupRepository.list(groupIds.toArray(new Long[groupIds.size()]));
        Assert.assertEquals(7, list.size());
        ModelAssert.assertGroupEquals(list.get(0), testGroup);
    }

    @Test
    public void testListGroups() throws Exception {
//        List<Group> list = groupRepository.list();
//        Assert.assertTrue(list.size() >= 7);
    }

    @Test
    public void testListGroupsBy() throws Exception {
        List<Group> list = groupRepository.list(defaultSlb.getId());
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
        Assert.assertEquals(originGroup.getVersion().intValue(), updatedGroup.getVersion().intValue());
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
     * ****************** test VirtualServerRepository ********************
     */

    @Test
    public void testVirtualServerGet() throws Exception {
        virtualServerRepository.addVirtualServer(defaultSlb.getId(), new VirtualServer().setSlbId(defaultSlb.getId()).setName("www.testGet1.com").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("www.testGet1.com")));
        virtualServerRepository.addVirtualServer(defaultSlb.getId(), new VirtualServer().setSlbId(defaultSlb.getId()).setName("www.testGet2.com").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("www.testGet2.com")));
        Set<Long> vsIds = virtualServerCriteriaQuery.queryBySlbId(defaultSlb.getId());
        vsIds.retainAll(virtualServerCriteriaQuery.queryByDomain("www.testGet1.com"));
        Assert.assertEquals(1, vsIds.size());
        VirtualServer vs = virtualServerRepository.getById((Long) vsIds.toArray()[0]);
        Assert.assertEquals("www.testGet1.com", vs.getName());
        Assert.assertEquals("www.testGet1.com".toLowerCase(), vs.getDomains().get(0).getName());
        VirtualServer vs1 = virtualServerRepository.getById(vs.getId());
        ModelAssert.assertVirtualServerEquals(vs, vs1);
    }

    @Test
    public void testFindGroupsByVirtualServer() throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryByVsIds(new Long[]{defaultSlb.getVirtualServers().get(0).getId()});
        Assert.assertEquals(6, groupIds.size());
    }

    @Test
    public void testUpdateGroupVirtualServers() throws Exception {
        Group group = groupRepository.getById(testGroup.getId());
        Assert.assertEquals(1, group.getGroupVirtualServers().size());

        List<GroupVirtualServer> gvs = group.getGroupVirtualServers();
        gvs.get(0).setPath("/testUpdateGroupVs");
        gvs.add(new GroupVirtualServer().setPath("/testUpdateGroupVsNew").setVirtualServer(new VirtualServer().setId(defaultSlb.getVirtualServers().get(0).getId())));

        Set<Long> gvsIds = new HashSet<>();
        gvsIds.add(defaultSlb.getVirtualServers().get(0).getId());
        gvsIds.add(defaultSlb.getVirtualServers().get(1).getId());
        Assert.assertTrue(gvsIds.remove(gvs.get(0).getVirtualServer().getId()));
        Assert.assertTrue(gvsIds.remove(gvs.get(1).getVirtualServer().getId()));
        Assert.assertEquals(0, gvsIds.size());

        gvsIds.add(gvs.get(0).getVirtualServer().getId());
        gvsIds.add(gvs.get(1).getVirtualServer().getId());

        groupRepository.update(group);

        group = groupRepository.getById(testGroup.getId());
        gvs = group.getGroupVirtualServers();
        Assert.assertEquals(2, gvs.size());
        Assert.assertTrue(gvsIds.remove(gvs.get(0).getVirtualServer().getId()));
        Assert.assertTrue(gvsIds.remove(gvs.get(1).getVirtualServer().getId()));
        Assert.assertEquals(0, gvsIds.size());

        gvs.get(0).setVirtualServer(new VirtualServer().setId(gvs.get(1).getVirtualServer().getId()));
        try {
            groupRepository.update(group);
            Assert.assertTrue(false);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof ValidationException);
        }
        gvs.remove(gvs.get(1));
        groupRepository.update(group);
        gvs = groupRepository.getById(testGroup.getId()).getGroupVirtualServers();
        Assert.assertEquals(1, gvs.size());
    }

    @Test
    public void testVirtualServerModification() throws Exception {
        Slb raw = new Slb().setName("testUpdateVirtualServer").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.01").setHostName("dev1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.02").setHostName("dev2"))
                .addSlbServer(new SlbServer().setIp("10.1.1.03").setHostName("dev3"))
                .addVirtualServer(new VirtualServer().setName("www.nihao.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.nihao.com")))
                .addVirtualServer(new VirtualServer().setName("www.hello.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.hello.com")));
        slbRepository.add(raw);
        Slb created = slbRepository.get(raw.getName());

        Group group = generateGroup("testUpdateVirtualServerGroup", created, created.getVirtualServers().get(1));
        groupRepository.add(group);

        virtualServerRepository.addVirtualServer(created.getId(), new VirtualServer().setSlbId(created.getId()).setName("www.bonjour.com").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("www.bonjour.com")));
        virtualServerRepository.addVirtualServer(created.getId(), new VirtualServer().setSlbId(created.getId()).setName("www.hallo.com").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("www.hallo.com")));

        Set<Long> vsIds = virtualServerCriteriaQuery.queryBySlbId(created.getId());
        List<VirtualServer> virtualServers = virtualServerRepository.listAll(vsIds.toArray(new Long[vsIds.size()]));
        virtualServers.get(0).setName("www.nihao.com.cn");
        virtualServers.get(1).setName("www.hello.com.cn");
        for (VirtualServer virtualServer : virtualServers) {
            virtualServerRepository.update(virtualServer);
        }
        Slb updated = slbRepository.getById(created.getId());
        Group updatedGroup = groupRepository.get(group.getName());

        Assert.assertEquals(4, updated.getVirtualServers().size());
        Assert.assertEquals(4, virtualServerCriteriaQuery.queryBySlbId(created.getId()).size());

        Set<String> domainChecks = new HashSet<>();
        domainChecks.add(virtualServers.get(0).getName());
        domainChecks.add(virtualServers.get(1).getName());
        for (GroupVirtualServer groupVirtualServer : updatedGroup.getGroupVirtualServers()) {
            domainChecks.remove(groupVirtualServer.getVirtualServer().getName());
        }
        Assert.assertTrue(domainChecks.contains(virtualServers.get(0).getName()));

        groupRepository.delete(updatedGroup.getId());
        virtualServerRepository.delete(virtualServers.get(0).getId());
        virtualServerRepository.delete(virtualServers.get(1).getId());

        vsIds = virtualServerCriteriaQuery.queryBySlbId(created.getId());
        virtualServers = virtualServerRepository.listAll(vsIds.toArray(new Long[vsIds.size()]));
        Assert.assertEquals(2, virtualServers.size());
        Assert.assertEquals("www.bonjour.com", virtualServers.get(0).getName());
        Assert.assertEquals("www.hallo.com", virtualServers.get(1).getName());

        Slb s2 = new Slb().setName("testUpdateVirtualServer2").setVersion(1)
                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf")
                .setNginxWorkerProcesses(2).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.011").setHostName("dev1"))
                .addSlbServer(new SlbServer().setIp("10.1.1.012").setHostName("dev2"))
                .addSlbServer(new SlbServer().setIp("10.1.1.013").setHostName("dev3"))
                .addVirtualServer(new VirtualServer().setName("www.bonjour.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.bonjour.com")))
                .addVirtualServer(new VirtualServer().setName("www.hallo.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.hallo.com")))
                .addVirtualServer(new VirtualServer().setName("www.hello.com").setSsl(false).setPort("80")
                        .addDomain(new Domain().setName("www.hello.com")));
        slbRepository.add(s2);
        s2 = slbRepository.get(s2.getName());
        updated = slbRepository.getById(created.getId());
        Assert.assertEquals(updated.getVirtualServers().size(), 2);
        Assert.assertEquals(s2.getVirtualServers().size(), 3);
    }

    /**
     * ****************** test GroupMemberRepository ********************
     */

    @Test
    public void testListGroupServersByGroup() throws Exception {
        List<String> groupServers = new ArrayList<>();
        for (GroupServer groupServer : groupRepository.getById(testGroup.getId()).getGroupServers()) {
            groupServers.add(groupServer.getIp());
        }
        List<String> groupServersRef = new ArrayList<>();
        for (GroupServer as : testGroup.getGroupServers()) {
            groupServersRef.add(as.getIp());
        }
        Assert.assertFalse(groupServersRef.retainAll(groupServers));

    }

    @Test
    public void testFindGroupsByGroupServerIp() throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryByGroupServerIp(testGroup.getGroupServers().get(0).getIp());
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
    public void testGroupServerModification() throws Exception {
        List<GroupServer> groupMembers = testGroup.getGroupServers();
        groupMembers.get(0).setPort(8899);
        groupRepository.update(testGroup);

        List<GroupServer> updated = groupRepository.getById(testGroup.getId()).getGroupServers();
        ModelAssert.assertGroupServerEquals(groupMembers.get(0), updated.get(0));
        ModelAssert.assertGroupServerEquals(groupMembers.get(1), updated.get(1));

        GroupServer add = new GroupServer().setFailTimeout(70).setHostName("NET182").setIp("192.168.10.2").setMaxFails(0).setPort(80).setWeight(5);
        testGroup.addGroupServer(add);
        groupRepository.update(testGroup);
        updated = groupRepository.getById(testGroup.getId()).getGroupServers();
        Assert.assertEquals(3, updated.size());
        ModelAssert.assertGroupServerEquals(add, updated.get(2));

        testGroup.getGroupServers().remove(testGroup.getGroupServers().size() - 1);
        groupRepository.update(testGroup);
        updated = groupRepository.getById(testGroup.getId()).getGroupServers();
        Assert.assertEquals(2, updated.size());
        ModelAssert.assertGroupServerEquals(groupMembers.get(0), updated.get(0));
        ModelAssert.assertGroupServerEquals(groupMembers.get(1), updated.get(1));
    }

    /**
     * ****************** test ArchiveService ********************
     */

    @Test
    public void testGetLatestSlbArchive() throws Exception {
        Archive archive = archiveService.getLatestSlbArchive(defaultSlb.getId());
        Assert.assertTrue(archive.getVersion() > 0);
    }
}