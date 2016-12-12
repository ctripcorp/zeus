package com.ctrip.zeus.task.check;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbDao;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbDo;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbEntity;
import com.ctrip.zeus.service.message.queue.consumers.SlbCheckStatusConsumer;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.util.CompressUtils;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * Created by zhoumy on 2016/12/6.
 */
public class SlbCheckStatusRollingMachineTest extends AbstractServerTest {
    @Resource
    private StatusCheckCountSlbDao statusCheckCountSlbDao;
    @Resource
    private SlbCheckStatusRollingMachine slbCheckStatusRollingMachine;

    @Test
    public void updateIfExpired() throws Exception {
        statusCheckCountSlbDao.insert(new StatusCheckCountSlbDo().setSlbId(1L).setCount(1).setDataSet(slbCheckStatusRollingMachine.dataSetStringify(Sets.newHashSet(1L))));
        Map<Long, List<GroupStatus>> data = new HashMap<>();
        List<GroupStatus> gs1 = new ArrayList<>();
        data.put(1L, gs1);
        gs1.add(generateGroupStatus(1L, true));
        gs1.add(generateGroupStatus(2L, false));

        List<GroupStatus> gs2 = new ArrayList<>();
        data.put(2L, gs2);
        gs2.add(generateGroupStatus(1L, true));
        gs2.add(generateGroupStatus(2L, false));

        slbCheckStatusRollingMachine.enable(true, new SlbCheckStatusConsumerInjector(data));
        slbCheckStatusRollingMachine.updateIfExpired(Sets.newHashSet(1L, 2L));
        slbCheckStatusRollingMachine.run();

        List<Integer> c1 = slbCheckStatusRollingMachine.getCheckFailureCount(1L);
        List<Integer> c2 = slbCheckStatusRollingMachine.getCheckFailureCount(2L);
        Assert.assertEquals(1, c1.get(c1.size() - 1).intValue());
        Assert.assertEquals(1, c2.get(c2.size() - 1).intValue());

        Thread.sleep(1000L);
        statusCheckCountSlbDao.update(new StatusCheckCountSlbDo().setSlbId(1L).setCount(2).setDataSet(slbCheckStatusRollingMachine.dataSetStringify(Sets.newHashSet(1L, 2L))), StatusCheckCountSlbEntity.UPDATESET_FULL);
        slbCheckStatusRollingMachine.updateIfExpired(Sets.newHashSet(1L, 2L));
        slbCheckStatusRollingMachine.run();

        c1 = slbCheckStatusRollingMachine.getCheckFailureCount(1L);
        c2 = slbCheckStatusRollingMachine.getCheckFailureCount(2L);
        Assert.assertEquals(2, c1.get(c1.size() - 1).intValue());
        Assert.assertEquals(1, c2.get(c2.size() - 1).intValue());
    }

    @Test
    public void noNeedToUpdate() throws Exception {
        statusCheckCountSlbDao.insert(new StatusCheckCountSlbDo().setSlbId(6L).setCount(2).setDataSet(slbCheckStatusRollingMachine.dataSetStringify(Sets.newHashSet(8L, 9L))));
        Map<Long, List<GroupStatus>> data = new HashMap<>();
        List<GroupStatus> gs = new ArrayList<>();
        data.put(6L, gs);
        gs.add(generateGroupStatus(8L, false));
        gs.add(generateGroupStatus(9L, false));
        gs.add(generateGroupStatus(10L, true));

        SlbCheckStatusConsumerInjector consumer = new SlbCheckStatusConsumerInjector(data);
        slbCheckStatusRollingMachine.enable(true, consumer);
        slbCheckStatusRollingMachine.updateIfExpired(Sets.newHashSet(6L));
        slbCheckStatusRollingMachine.run();

        Thread.sleep(1000L);
        slbCheckStatusRollingMachine.updateIfExpired(Sets.newHashSet(6L));
    }

    @Test
    public void compress() throws IOException {
        String compressed = CompressUtils.compressToGzippedBase64String("1,2,3,4,5,6,7");
        String value = CompressUtils.decompressGzippedBase64String(compressed);
        Assert.assertEquals("1,2,3,4,5,6,7", value);
    }

    @Test
    public void migrate() throws Exception {
        Map<Long, List<GroupStatus>> data = new HashMap<>();
        List<GroupStatus> gs1 = new ArrayList<>();
        data.put(3L, gs1);
        gs1.add(generateGroupStatus(3L, false));
        gs1.add(generateGroupStatus(4L, false));

        data.put(4L, new ArrayList<GroupStatus>());

        slbCheckStatusRollingMachine.enable(true, new SlbCheckStatusConsumerInjector(data));
        slbCheckStatusRollingMachine.run();

        List<Integer> c2 = slbCheckStatusRollingMachine.getCheckFailureCount(4L);
        Assert.assertEquals(0, c2.size());
        slbCheckStatusRollingMachine.migrate(Sets.newHashSet(3L), Sets.newHashSet(3L, 4L),
                Sets.newHashSet(3L, 4L), gs1);
        slbCheckStatusRollingMachine.run();

        List<Integer> c1 = slbCheckStatusRollingMachine.getCheckFailureCount(3L);
        c2 = slbCheckStatusRollingMachine.getCheckFailureCount(4L);
        Assert.assertEquals(2, c1.get(c1.size() - 1).intValue());
        Assert.assertEquals(2, c2.get(c2.size() - 1).intValue());
    }

    @Test
    public void update() throws Exception {
        Map<Long, List<GroupStatus>> data = new HashMap<>();
        List<GroupStatus> gs = new ArrayList<>();
        data.put(5L, gs);
        gs.add(generateGroupStatus(5L, false));
        gs.add(generateGroupStatus(6L, false));
        gs.add(generateGroupStatus(7L, true));

        slbCheckStatusRollingMachine.enable(true, new SlbCheckStatusConsumerInjector(data));
        slbCheckStatusRollingMachine.run();

        slbCheckStatusRollingMachine.update(Sets.newHashSet(5L), generateGroupStatus(5L, true));
        slbCheckStatusRollingMachine.run();

        List<Integer> c = slbCheckStatusRollingMachine.getCheckFailureCount(5L);
        Assert.assertEquals(1, c.get(c.size() - 1).intValue());
    }

    @Test
    public void bitwiseStatus() throws Exception {
        SlbCheckStatusRollingMachine m = new SlbCheckStatusRollingMachine();
        Assert.assertEquals(15, m.bitwiseStatus(new GroupServerStatus().setHealthy(true).setMember(true).setPull(true).setServer(true)));
        Assert.assertEquals(11, m.bitwiseStatus(new GroupServerStatus().setHealthy(true).setMember(true).setPull(true).setServer(false)));
        Assert.assertEquals(14, m.bitwiseStatus(new GroupServerStatus().setHealthy(false).setMember(true).setPull(true).setServer(true)));
        Assert.assertEquals(12, m.bitwiseStatus(new GroupServerStatus().setHealthy(false).setMember(false).setPull(true).setServer(true)));

        Assert.assertEquals(1, 15 & 0x1);
        Assert.assertEquals(1, 11 & 0x1);
        Assert.assertEquals(0, 14 & 0x1);
        Assert.assertEquals(0, 12 & 0x1);
    }

    GroupStatus generateGroupStatus(Long groupId, boolean goodHc) {
        return new GroupStatus().setGroupId(groupId).addGroupServerStatus(new GroupServerStatus().setServer(true).setHealthy(goodHc).setMember(true).setPull(true));
    }

    class SlbCheckStatusConsumerInjector extends SlbCheckStatusConsumer {
        private Map<Long, List<GroupStatus>> data;

        public SlbCheckStatusConsumerInjector(Map<Long, List<GroupStatus>> data) {
            this.data = data;
        }

        @Override
        public void refresh(Set<Long> slbIds) {
            for (Long slbId : slbIds) {
                try {
                    slbCheckStatusRollingMachine.refresh(slbId, data.get(slbId));
                } catch (IOException e) {
                    Assert.assertTrue(false);
                } catch (DalException e) {
                    Assert.assertTrue(false);
                }
            }
        }
    }

}