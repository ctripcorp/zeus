package com.ctrip.zeus.service.model;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.util.ModelAssert;
import org.junit.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhoumy on 2016/1/12.
 */
public class ServiceTest extends AbstractServerTest {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;

    @Resource
    private RSlbStatusDao rSlbStatusDao;
    @Resource
    private RVsStatusDao rVsStatusDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    private Map<String, Slb> slbTracker = new HashMap<>();
    private Map<String, Group> groupTracker = new HashMap<>();
    private Map<String, VirtualServer> vsTracker = new HashMap<>();

    @Before
    public void fillDb() throws Exception {
        testAddSlbAndVses();
        testAddGroup();
    }

    @After
    public void clearDb() throws Exception {
        testDeleteGroup();
        testDeleteVs();
        testDeleteSlb();
    }

    private void testAddSlbAndVses() throws Exception {
        Slb default1 = new Slb().setName("default").setStatus("TEST")
                .addVip(new Vip().setIp("127.0.25.93"))
                .addSlbServer(new SlbServer().setIp("127.0.25.93").setHostName("uat0358"))
                .addSlbServer(new SlbServer().setIp("127.0.25.94").setHostName("uat0359"));
        slbRepository.add(default1);

        VirtualServer vs1 = new VirtualServer().setName("defaultSlbVs1").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("defaultSlbVs1.ctrip.com"));
        vs1.getSlbIds().add(default1.getId());
        virtualServerRepository.add(vs1);

        VirtualServer vs2 = new VirtualServer().setName("defaultSlbVs2").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("defaultSlbVs2.ctrip.com"));
        vs2.getSlbIds().add(default1.getId());
        virtualServerRepository.add(vs2);

        // fill expected data
        ModelAssert.assertSlbEquals(default1, slbRepository.getByKey(new IdVersion(default1.getId(), default1.getVersion())));
        slbTracker.put("default", default1);

        vsTracker.put(vs1.getName(), vs1);
        vsTracker.put(vs2.getName(), vs2);

        for (VirtualServer vs : vsTracker.values()) {
            vsTracker.put(vs.getName(), vs);
            ModelAssert.assertVirtualServerEquals(vs, virtualServerRepository.getByKey(new IdVersion(vs.getId(), vs.getVersion())));
        }
    }

    @Test
    public void testGetSlb() throws Exception {
        Slb defaultSlb = slbTracker.get("default");
        Slb slb = slbRepository.getByKey(new IdVersion(defaultSlb.getId(), defaultSlb.getVersion()));
        Assert.assertNotNull(slb);
        Assert.assertTrue(slb.getSlbServers().size() == 2);
    }

    @Test
    public void testUpdateSlb() throws Exception {
        Slb defaultSlb = slbTracker.get("default");
        Slb slb = slbRepository.getByKey(new IdVersion(defaultSlb.getId(), defaultSlb.getVersion()));
        slb.getSlbServers().add(new SlbServer().setIp("127.0.25.95").setHostName("uat0360"));
        slbRepository.update(slb);

        ModelAssert.assertSlbEquals(slb, slbRepository.getByKey(new IdVersion(slb.getId(), slb.getVersion())));
        slbTracker.put("default", slb);
    }

    public void testDeleteSlb() throws Exception {
        Long[] slbIds = new Long[slbTracker.size()];
        int i = 0;
        for (Slb slb : slbTracker.values()) {
            slbIds[i] = slb.getId();
            slbRepository.delete(slb.getId());
            i++;
        }
        Assert.assertEquals(0, slbRepository.list(slbIds).size());
    }

    private void testAddGroup() throws Exception {
        VirtualServer vs = vsTracker.get("defaultSlbVs1");
        Group testAdd = generateGroup("testAdd", vs.getId());
        groupRepository.add(testAdd);
        groupTracker.put("testAdd", testAdd);

        // fill expected data
        testAdd.getGroupVirtualServers().get(0).setPriority(1000).setVirtualServer(vs);

        ModelAssert.assertGroupEquals(testAdd, groupRepository.getByKey(new IdVersion(testAdd.getId(), testAdd.getVersion())));
    }

    @Test
    public void testGetGroup() throws Exception {
        Group testAdd = groupTracker.get("testAdd");
        Group group = groupRepository.getByKey(new IdVersion(testAdd.getId(), testAdd.getVersion()));
        Assert.assertNotNull(group);
        Assert.assertTrue(group.getGroupVirtualServers().size() == 1);
        Assert.assertNotNull(group.getGroupVirtualServers().get(0).getVirtualServer());
        Assert.assertTrue(group.getGroupServers().size() == 2);
    }

    @Test
    public void testUpdateGroup() throws Exception {
        Group group = groupTracker.get("testAdd");
        VirtualServer vs = vsTracker.get("defaultSlbVs2");
        group.setAppId("921812").getGroupVirtualServers()
                .get(0).setPriority(-1000).setVirtualServer(new VirtualServer().setId(vs.getId()));
        groupRepository.update(group);
        groupTracker.put("testAdd", group);

        // fill expected data
        group.getGroupVirtualServers().get(0).setVirtualServer(vs);
        ModelAssert.assertGroupEquals(group, groupRepository.getByKey(new IdVersion(group.getId(), group.getVersion())));
    }

    public void testDeleteGroup() throws Exception {
        Group testAdd = groupTracker.get("testAdd");
        groupRepository.delete(testAdd.getId());
        Assert.assertEquals(0, groupRepository.list(new Long[]{testAdd.getId()}).size());
    }

    @Test
    public void testUpdateVs() throws Exception {
        Slb testVsSlb = new Slb().setName("testVsSlb").setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("127.0.0.1").setHostName("localhost"));
        slbRepository.add(testVsSlb);

        VirtualServer vs = vsTracker.get("defaultSlbVs2");
        vs.getSlbIds().set(0, testVsSlb.getId());
        virtualServerRepository.update(vs);
        ModelAssert.assertVirtualServerEquals(vs, virtualServerRepository.getByKey(new IdVersion(vs.getId(), vs.getVersion())));
    }

    public void testDeleteVs() throws Exception {
        Long[] vsIds = new Long[vsTracker.size()];
        int i = 0;
        for (VirtualServer vs : vsTracker.values()) {
            vsIds[i] = vs.getId();
            virtualServerRepository.delete(vs.getId());
            i++;
        }
        Assert.assertEquals(0, virtualServerRepository.listAll(vsIds).size());
    }

    @Test
    public void testActivateAndDeactivate() throws Exception {
        Slb slb = slbTracker.get("default");
        slbRepository.updateStatus(new IdVersion[]{new IdVersion(slb.getId(), slb.getVersion())});
        Assert.assertEquals(slb.getVersion().intValue(), rSlbStatusDao.findBySlb(slb.getId(), RSlbStatusEntity.READSET_FULL).getOnlineVersion());

        VirtualServer vs = vsTracker.get("defaultSlbVs1");
        virtualServerRepository.updateStatus(new IdVersion[]{new IdVersion(vs.getId(), vs.getVersion())});
        Assert.assertEquals(vs.getVersion().intValue(), rVsStatusDao.findByVs(vs.getId(), RVsStatusEntity.READSET_FULL).getOnlineVersion());

        Group group = groupTracker.get("testAdd");
        groupRepository.updateStatus(new IdVersion[]{new IdVersion(group.getId(), group.getVersion())});
        Assert.assertEquals(group.getVersion().intValue(), rGroupStatusDao.findByGroup(group.getId(), RGroupStatusEntity.READSET_FULL).getOnlineVersion());

        group.addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("192.168.55.3"));
        groupRepository.update(group);

        groupRepository.updateStatus(new IdVersion[]{new IdVersion(group.getId(), 0)});
        Assert.assertEquals(0, rGroupStatusDao.findByGroup(group.getId(), RGroupStatusEntity.READSET_FULL).getOnlineVersion());

        virtualServerRepository.updateStatus(new IdVersion[]{new IdVersion(vs.getId(), 0)});
        Assert.assertEquals(0, rVsStatusDao.findByVs(vs.getId(), RVsStatusEntity.READSET_FULL).getOnlineVersion());

        slbRepository.updateStatus(new IdVersion[]{new IdVersion(slb.getId(), 0)});
        Assert.assertEquals(0, rSlbStatusDao.findBySlb(slb.getId(), RSlbStatusEntity.READSET_FULL).getOnlineVersion());
    }

    private Group generateGroup(String groupName, Long vsId) {
        return new Group().setName(groupName).setAppId("000000").setSsl(false)
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/" + groupName).setVirtualServer(new VirtualServer().setId(vsId)))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("127.0.6.201"))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("127.0.6.202"));
    }
}
