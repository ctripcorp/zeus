package com.ctrip.zeus.service.model;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.util.ModelAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2016/1/20.
 */
public class ScenarioTest extends AbstractServerTest {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;

    private static AtomicInteger Counter = new AtomicInteger(2);

    @Before
    public void fillDb() throws Exception {
        if (Counter.get() == 2) {
            addSlbsAndVses();
            addGroups();
        }
    }

    @After
    public void clearDb() throws Exception {
        if (Counter.get() == 0) {
            IdVersion[] groups = new IdVersion[7];
            for (int i = 0; i < 7; i++) {
                groups[i] = new IdVersion(new Long(i), 0);
            }
            groupRepository.updateStatus(groups);
            for (Long i = 1L; i <= 7L; i++) {
                groupRepository.delete(i);
            }
            virtualServerRepository.updateStatus(new IdVersion[]{
                    new IdVersion(1L, 0), new IdVersion(2L, 0)});
            for (Long i = 1L; i <= 2L; i++) {
                virtualServerRepository.delete(i);
            }
            slbRepository.updateStatus(new IdVersion[]{new IdVersion(1L, 0)});
            slbRepository.delete(1L);
        }
    }

    @Test
    public void testMigrateGroup() throws Exception {
        Counter.decrementAndGet();
        Group testMigrate = generateGroup("testMigrate", 1L);
        groupRepository.add(testMigrate);

        Group ref = groupRepository.getById(testMigrate.getId());
        ModelAssert.assertGroupEquals(testMigrate, ref);

        VirtualServer vs = virtualServerRepository.getById(3L);
        testMigrate.getGroupVirtualServers().get(0).setVirtualServer(vs);
        groupRepository.update(testMigrate);

        ref = groupRepository.getById(testMigrate.getId());
        ModelAssert.assertGroupEquals(testMigrate, ref);
        ref = groupRepository.getByKey(new IdVersion(testMigrate.getId(), 1));
        Assert.assertEquals(1L, ref.getGroupVirtualServers().get(0).getVirtualServer().getId().longValue());

        groupRepository.delete(testMigrate.getId());
    }

    @Test
    public void testMigrateVs() throws Exception {
        Counter.decrementAndGet();

        VirtualServer testMigrate = new VirtualServer().setName("testMigrate.ctrip.com_80").setPort("80").setSsl(false).addDomain(new Domain().setName("testMigrate.ctrip.com"));
        testMigrate.getSlbIds().add(1L);
        virtualServerRepository.add(testMigrate);

        VirtualServer ref = virtualServerRepository.getById(testMigrate.getId());
        ModelAssert.assertVirtualServerEquals(testMigrate, ref);

        testMigrate.getSlbIds().set(0, 2L);
        virtualServerRepository.update(testMigrate);
        ref = virtualServerRepository.getById(testMigrate.getId());

        ModelAssert.assertVirtualServerEquals(ref, testMigrate);

        virtualServerRepository.delete(testMigrate.getId());
    }


    private void addSlbsAndVses() throws Exception {
        Slb default1 = new Slb().setName("default1").setStatus("TEST")
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

        IdVersion[] vses = new IdVersion[2];
        vses[0] = new IdVersion(vs1.getId(), vs1.getVersion());
        vses[1] = new IdVersion(vs2.getId(), vs2.getVersion());
        virtualServerRepository.updateStatus(vses);

        slbRepository.updateStatus(new IdVersion[]{new IdVersion(default1.getId(), default1.getVersion())});

        Slb default2 = new Slb().setName("default2").setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("127.0.0.1").setHostName("localhost"));
        slbRepository.add(default2);

        VirtualServer vs3 = new VirtualServer().setName("defaultSlbVs3").setSsl(false).setPort("80")
                .addDomain(new Domain().setName("defaultSlbVs3.ctrip.com"));
        vs3.getSlbIds().add(default2.getId());
        virtualServerRepository.add(vs3);
    }

    private void addGroups() throws Exception {
        List<Group> activated = new ArrayList<>();
        Group testGroupOnVs1 = generateGroup("testGroupOnVs1", 1L);
        groupRepository.add(testGroupOnVs1);
        activated.add(testGroupOnVs1);
        for (int i = 0; i < 6; i++) {
            Group group = generateGroup("testGroupOnVs2_" + i, 2L);
            groupRepository.add(group);
            if (i % 2 == 0) {
                activated.add(group);
            }
        }
        IdVersion[] groups = new IdVersion[activated.size()];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = new IdVersion(activated.get(i).getId(), activated.get(i).getVersion());
        }
        groupRepository.updateStatus(groups);
    }

    private Group generateGroup(String groupName, Long vsId) {
        return new Group().setName(groupName).setAppId("000000").setSsl(false)
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/" + groupName).setVirtualServer(new VirtualServer().setId(vsId)))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.201"))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.202"));
    }
}
