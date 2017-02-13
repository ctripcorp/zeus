package com.ctrip.zeus.service;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.commit.entity.ConfSlbVersion;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.ConfFile;
import com.ctrip.zeus.nginx.entity.NginxConfEntry;
import com.ctrip.zeus.nginx.entity.Upstreams;
import com.ctrip.zeus.nginx.entity.Vhosts;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.util.CompressUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2016/4/22.
 */

/**
 * Test case coverage:
 * <p/>
 * 1.conf(1,5) -> 4.conf(1,2,5) //migrate group 1,5 (vs 1->4)
 * 1_5.conf(3) -> 1.conf(3) //deactivate vs 5
 * 2.conf(2) -> 4.conf(1,2,5) //migrate group 2 (vs 2->4)
 * 2_3.conf(9) -> 1_2.conf(9) // migrate group 9 (vs 3->1), stable in vs 2
 * 3.conf(4,7) -> 3.conf(4) //add vs 2 to group 7
 * 3.conf(4,7) -> 2_3.conf(7) //add vs 2 to group 7
 * 4.conf(8) -> remove // deactivate group 8
 * 6.conf(6) -> 6.conf(6) // stable
 */

public class MultiGvsAtSameSlbTest extends AbstractServerTest {
    @Resource
    private NginxConfSlbDao nginxConfSlbDao;
    @Resource
    private NginxConfDao nginxConfDao;
    @Resource
    private BuildInfoDao buildInfoDao;
    @Resource
    private BuildService buildService;
    @Resource
    private NginxConfService nginxConfService;
    @Resource
    private ConfVersionService confVersionService;

    private static boolean inited = false;

    @Before
    public void setCurrentVersion() throws Exception {
        if (inited) return;
        buildInfoDao.insert(new BuildInfoDo().setSlbId(1L).setPendingTicket(2).setCurrentTicket(1));
        nginxConfDao.insert(new NginxConfDo().setSlbId(1L).setVersion(1).setContent("nginx.conf"));

        NginxConfEntry currentConf = new NginxConfEntry().setUpstreams(new Upstreams()).setVhosts(new Vhosts());

        currentConf.getVhosts().addConfFile(new ConfFile().setName("1").setContent("location_1,3,5"));
        currentConf.getVhosts().addConfFile(new ConfFile().setName("2").setContent("location_2,9"));
        currentConf.getVhosts().addConfFile(new ConfFile().setName("3").setContent("location_4,7,9"));
        currentConf.getVhosts().addConfFile(new ConfFile().setName("4").setContent("location_8"));
        currentConf.getVhosts().addConfFile(new ConfFile().setName("5").setContent("location_3"));
        currentConf.getVhosts().addConfFile(new ConfFile().setName("6").setContent("location_6"));

        currentConf.getUpstreams().addConfFile(new ConfFile().setName("1").setContent("upstream_1,5"));
        currentConf.getUpstreams().addConfFile(new ConfFile().setName("1_5").setContent("upstream_3"));
        currentConf.getUpstreams().addConfFile(new ConfFile().setName("2").setContent("upstream_2"));
        currentConf.getUpstreams().addConfFile(new ConfFile().setName("2_3").setContent("upstream_9"));
        currentConf.getUpstreams().addConfFile(new ConfFile().setName("3").setContent("upstream_4,7"));
        currentConf.getUpstreams().addConfFile(new ConfFile().setName("4").setContent("upstream_8"));
        currentConf.getUpstreams().addConfFile(new ConfFile().setName("6").setContent("upstream_6"));

        nginxConfSlbDao.insert(new NginxConfSlbDo().setSlbId(1L).setVersion(1)
                .setContent(CompressUtils.compress(GenericSerializer.writeJson(currentConf))));
        confVersionService.addConfSlbVersion(new ConfSlbVersion().setSlbId(1L).setCurrentVersion(1L).setPreviousVersion(0L));
        inited = true;
    }

    @Test
    public void testBuildService() throws Exception {
        Slb slb = new Slb().setId(1L).addSlbServer(new SlbServer().setIp("127.0.0.1")).setVersion(1);
        Map<Long, VirtualServer> onlineVses = new HashMap<>();
        for (int i = 1; i < 7; i++) {
            VirtualServer vs = new VirtualServer().setId((long) i).addDomain(new Domain().setName("test" + i + "test.domain.com")).setPort("80");
            onlineVses.put(vs.getId(), vs);
        }
        Map<Long, List<Group>> vsGroups = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            vsGroups.put((long) i, new ArrayList<Group>());
        }
        vsGroups.remove(5L);
        vsGroups.get(4L).add(generateGroup(1L, new Long[]{4L}));
        vsGroups.get(4L).add(generateGroup(2L, new Long[]{4L}));
        vsGroups.get(1L).add(generateGroup(3L, new Long[]{1L}));
        vsGroups.get(3L).add(generateGroup(4L, new Long[]{3L}));
        vsGroups.get(4L).add(generateGroup(5L, new Long[]{4L}));
        vsGroups.get(6L).add(generateGroup(6L, new Long[]{6L}));
        Group g23 = generateGroup(7L, new Long[]{2L, 3L});
        vsGroups.get(2L).add(g23);
        vsGroups.get(3L).add(g23);
        Group g12 = generateGroup(9L, new Long[]{1L, 2L});
        vsGroups.get(1L).add(g12);
        vsGroups.get(2L).add(g12);
        buildService.build(slb, onlineVses, Sets.newHashSet(1L, 2L, 3L, 4L), Sets.newHashSet(5L), new HashMap<Long, List<TrafficPolicy>>(), vsGroups, new HashSet<String>(), new HashSet<String>());

        NginxConfEntry expectedNextConf = new NginxConfEntry().setUpstreams(new Upstreams()).setVhosts(new Vhosts());
        Map<String, Integer> vhostIndex = new HashMap<>();
        Map<String, Integer> upstreamIndex = new HashMap<>();

        expectedNextConf.getVhosts().addConfFile(new ConfFile().setName("1").setContent("location_3,9"));
        expectedNextConf.getVhosts().addConfFile(new ConfFile().setName("2").setContent("location_7"));
        expectedNextConf.getVhosts().addConfFile(new ConfFile().setName("3").setContent("location_4,7"));
        expectedNextConf.getVhosts().addConfFile(new ConfFile().setName("4").setContent("location_1,2,5"));
        expectedNextConf.getVhosts().addConfFile(new ConfFile().setName("6").setContent("location_6"));

        expectedNextConf.getUpstreams().addConfFile(new ConfFile().setName("1").setContent("upstream_3"));
        expectedNextConf.getUpstreams().addConfFile(new ConfFile().setName("1_2").setContent("upstream_9"));
        expectedNextConf.getUpstreams().addConfFile(new ConfFile().setName("2_3").setContent("upstream_7"));
        expectedNextConf.getUpstreams().addConfFile(new ConfFile().setName("3").setContent("upstream_4"));
        expectedNextConf.getUpstreams().addConfFile(new ConfFile().setName("4").setContent("upstream_1,2,5"));
        expectedNextConf.getUpstreams().addConfFile(new ConfFile().setName("6").setContent("upstream_6"));

        for (int i = 0; i < expectedNextConf.getVhosts().getFiles().size(); i++) {
            vhostIndex.put(expectedNextConf.getVhosts().getFiles().get(i).getName(), i);
        }
        for (int i = 0; i < expectedNextConf.getUpstreams().getFiles().size(); i++) {
            upstreamIndex.put(expectedNextConf.getUpstreams().getFiles().get(i).getName(), i);
        }

        Assert.assertNotNull(nginxConfDao.findBySlbIdAndVersion(1L, 2, NginxConfEntity.READSET_FULL));
        NginxConfSlbDo d = nginxConfSlbDao.findBySlbAndVersion(1L, 2, NginxConfSlbEntity.READSET_FULL);
        Assert.assertNotNull(d);
        NginxConfEntry actualNextConf = DefaultJsonParser.parse(NginxConfEntry.class, CompressUtils.decompress(d.getContent()));
        Assert.assertEquals(expectedNextConf.getVhosts().getFiles().size(), actualNextConf.getVhosts().getFiles().size());
        Assert.assertEquals(expectedNextConf.getUpstreams().getFiles().size(), actualNextConf.getUpstreams().getFiles().size());

        for (ConfFile cf : actualNextConf.getVhosts().getFiles()) {
            if (cf.getName().equals("5")) Assert.assertTrue(false);

            Integer i = vhostIndex.get(cf.getName());
            if (i == null) Assert.assertTrue(false);
            System.out.println(cf.getName() + "#" + cf.getContent());

            if (cf.getName().equals("6")) {
                Assert.assertEquals(expectedNextConf.getVhosts().getFiles().get(i).getContent(), cf.getContent());
            }
        }
        for (ConfFile cf : actualNextConf.getUpstreams().getFiles()) {
            if (cf.getName().equals("1_5")) Assert.assertTrue(false);

            Integer i = upstreamIndex.get(cf.getName());
            if (i == null) Assert.assertTrue(false);
            System.out.println(cf.getName() + "#" + cf.getContent());

            if (cf.getName().equals("6")) {
                Assert.assertEquals(expectedNextConf.getUpstreams().getFiles().get(i).getContent(), cf.getContent());
            }
        }
    }

    @Test
    public void testFilterUpstreamsAndVhosts() throws Exception {
        NginxConfEntry filteredEntry = nginxConfService.getUpstreamsAndVhosts(1L, 1L, Lists.newArrayList(1L, 2L, 3L));
        Set<String> vhostFilename = Sets.newHashSet("1", "2", "3");
        Set<String> upstreamFilename = Sets.newHashSet("1", "1_5", "2", "2_3", "3");
        for (ConfFile cf : filteredEntry.getVhosts().getFiles()) {
            Assert.assertTrue(vhostFilename.remove(cf.getName()));
        }
        for (ConfFile cf : filteredEntry.getUpstreams().getFiles()) {
            Assert.assertTrue(upstreamFilename.remove(cf.getName()));
        }
        Assert.assertTrue(vhostFilename.isEmpty());
        Assert.assertTrue(upstreamFilename.isEmpty());
    }

    private Group generateGroup(Long groupId, Long[] vsIds) {
        Group group = new Group().setId(groupId).setName("group_" + groupId).setAppId("000000").setSsl(false)
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.201"))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.202"));
        for (Long id : vsIds) {
            VirtualServer vs = new VirtualServer().setId(id);
            vs.getSlbIds().add(1L);
            group.addGroupVirtualServer(new GroupVirtualServer().setPath("/" + groupId).setPriority(1000).setVirtualServer(vs));
        }
        return group;
    }
}
