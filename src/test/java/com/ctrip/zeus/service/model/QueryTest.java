package com.ctrip.zeus.service.model;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2016/1/14.
 */
public class QueryTest extends AbstractServerTest {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;

    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    @Resource
    private EntityFactory entityFactory;

    private static AtomicInteger Counter = new AtomicInteger(6);

    @Before
    public void fillDb() throws Exception {
        if (Counter.get() == 6) {
            addSlbsAndVses();
            addGroups();
        }
    }

    @After
    public void clearDb() throws Exception {
        if (Counter.get() == 0) {
            IdVersion[] groups = new IdVersion[7];
            for (int i = 0; i < 7; i++) {
                groups[i] = new IdVersion(new Long(i + 1), 0);
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
    public void testGroupQuery() throws Exception {
        Counter.decrementAndGet();

        Set<Long> gIds = groupCriteriaQuery.queryAll();
        Assert.assertEquals(7, gIds.size());
        Assert.assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L}, gIds.toArray(new Long[7]));

        gIds = groupCriteriaQuery.queryByAppId("000000");
        Assert.assertEquals(7, gIds.size());
        Assert.assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L}, gIds.toArray(new Long[7]));

        Assert.assertEquals(1, groupCriteriaQuery.queryByName("testGroupOnVs1").longValue());

        IdVersion[] gKeyArray = groupCriteriaQuery.queryByIdAndMode(1L, SelectionMode.OFFLINE_FIRST);
        Assert.assertEquals(1, gKeyArray.length);
        Assert.assertEquals(new IdVersion(1L, 1), gKeyArray[0]);
    }

    @Test
    public void testVsQuery() throws Exception {
        Counter.decrementAndGet();

        Set<Long> vsIds = virtualServerCriteriaQuery.queryAll();
        Assert.assertEquals(3, vsIds.size());
        Assert.assertArrayEquals(new Long[]{1L, 2L, 3L}, vsIds.toArray(new Long[3]));

        Set<IdVersion> vsKeys = virtualServerCriteriaQuery.queryByDomain("defaultSlbVs3.ctrip.com");
        Assert.assertEquals(1, vsKeys.size());
        Assert.assertEquals(3L, vsKeys.iterator().next().getId().longValue());


        vsKeys = virtualServerCriteriaQuery.queryByIdsAndMode(new Long[]{1L, 2L, 3L}, SelectionMode.OFFLINE_FIRST);
        Assert.assertEquals(3, vsKeys.size());
        Long[] values = new Long[3];
        int i = 0;
        for (IdVersion e : vsKeys) {
            values[i] = e.getId();
            if (e.getId().equals(1L)) {
                Assert.assertEquals(2, e.getVersion().intValue());
            } else {
                Assert.assertEquals(1, e.getVersion().intValue());
            }
            i++;
        }
        Arrays.sort(values);
        Assert.assertArrayEquals(new Long[]{1L, 2L, 3L}, values);

        vsKeys = virtualServerCriteriaQuery.queryBySlbId(1L);
        Assert.assertEquals(2, vsKeys.size());
        vsKeys = virtualServerCriteriaQuery.queryBySlbId(3L);
        Assert.assertEquals(1, vsKeys.size());


        IdVersion[] vsKeyArray = virtualServerCriteriaQuery.queryByIdAndMode(2L, SelectionMode.ONLINE_EXCLUSIVE);
        Assert.assertEquals(1, vsKeyArray.length);
        Assert.assertEquals(new IdVersion(2L, 1), vsKeyArray[0]);
    }

    @Test
    public void testSlbQuery() throws Exception {
        Counter.decrementAndGet();

        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        Assert.assertEquals(3, slbIds.size());
        Assert.assertArrayEquals(new Long[]{1L, 2L, 3L}, slbIds.toArray(new Long[3]));

        slbIds = slbCriteriaQuery.queryByVs(new IdVersion(2L, 1));
        Assert.assertEquals(1, slbIds.size());
        Assert.assertArrayEquals(new Long[]{1L}, slbIds.toArray(new Long[1]));

        slbIds = slbCriteriaQuery.queryByVses(new IdVersion[]{new IdVersion(2L, 1), new IdVersion(3L, 1)});
        Assert.assertEquals(2, slbIds.size());
        Assert.assertArrayEquals(new Long[]{1L, 2L}, slbIds.toArray(new Long[2]));

        Assert.assertEquals(1L, slbCriteriaQuery.queryByName("default1").longValue());

        Set<IdVersion> sKeys = slbCriteriaQuery.queryBySlbServerIp("127.0.0.1");
        Assert.assertEquals(1, sKeys.size());
        Long[] values = new Long[1];
        int i = 0;
        for (IdVersion e : sKeys) {
            values[i] = e.getId();
            if (e.getId().equals(1L)) {
                Assert.assertEquals(2, e.getVersion().intValue());
            } else {
                Assert.assertEquals(1, e.getVersion().intValue());
            }
            i++;
        }
        Arrays.sort(values);
        Assert.assertEquals(1, sKeys.size());
        Assert.assertArrayEquals(new Long[]{2L}, values);

        IdVersion[] sKeyArray = slbCriteriaQuery.queryByIdAndMode(1L, SelectionMode.ONLINE_EXCLUSIVE);
        Assert.assertEquals(1, sKeyArray.length);
        Assert.assertEquals(new IdVersion(1L, 1), sKeyArray[0]);
    }

    @Test
    public void testBatchGetGroupInfo() throws Exception {
        Counter.decrementAndGet();

        ModelStatusMapping<Group> mapping = entityFactory.getGroupsByVsIds(new Long[]{1L, 2L, 3L});
        Assert.assertEquals(7, mapping.getOfflineMapping().size());
        Assert.assertEquals(4, mapping.getOnlineMapping().size());

        mapping = entityFactory.getGroupsByIds(new Long[]{1L, 2L, 3L});
        Assert.assertEquals(3, mapping.getOfflineMapping().size());
        Assert.assertEquals(2, mapping.getOnlineMapping().size());

        Long[] groupIds = entityFactory.getGroupIdsByGroupServerIp("127.0.6.201", SelectionMode.ONLINE_EXCLUSIVE);
        Assert.assertEquals(4, groupIds.length);
        Arrays.sort(groupIds);
        Assert.assertArrayEquals(new Long[]{1L, 2L, 4L, 6L}, groupIds);
    }

    @Test
    public void testBatchGetVsInfo() throws Exception {
        Counter.decrementAndGet();

        ModelStatusMapping<VirtualServer> mapping = entityFactory.getVsesByIds(new Long[]{1L, 2L});
        Assert.assertEquals(2, mapping.getOfflineMapping().size());
        Assert.assertEquals(2, mapping.getOnlineMapping().size());

        mapping = entityFactory.getVsesBySlbIds(1L);
        Assert.assertEquals(2, mapping.getOfflineMapping().size());
        Assert.assertEquals(2, mapping.getOnlineMapping().size());

        Long[] vsIds = entityFactory.getVsIdsBySlbId(1L, SelectionMode.ONLINE_EXCLUSIVE);
        Assert.assertEquals(2, vsIds.length);
    }

    @Test
    public void testBatchGetSlbInfo() throws Exception {
        Counter.decrementAndGet();

        Long[] slbIds = entityFactory.getSlbIdsByIp("127.0.25.93", SelectionMode.OFFLINE_EXCLUSIVE);
        Assert.assertEquals(0, slbIds.length);

        slbIds = entityFactory.getSlbIdsByIp("127.0.25.93", SelectionMode.ONLINE_FIRST);
        Assert.assertEquals(1, slbIds.length);

        ModelStatusMapping<Slb> mapping = entityFactory.getSlbsByIds(new Long[]{1L, 2L});
        Assert.assertEquals(2, mapping.getOfflineMapping().size());
        Assert.assertEquals(1, mapping.getOnlineMapping().size());
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

        Slb default0 = new Slb().setName("default0").setStatus("TEST")
                .addVip(new Vip().setIp("127.0.25.92"))
                .addSlbServer(new SlbServer().setIp("127.0.25.92").setHostName("uat0357"))
                .addSlbServer(new SlbServer().setIp("127.0.25.91").setHostName("uat0356"));
        slbRepository.add(default0);


        vs1.getSlbIds().add(default0.getId());
        vs1 = virtualServerRepository.update(vs1);
        virtualServerRepository.updateStatus(new IdVersion[]{new IdVersion(vs1.getId(), vs1.getVersion())});
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
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("127.0.6.201"))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("127.0.6.202"));
    }
}
