package com.ctrip.zeus.task.check;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbDao;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbDo;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbEntity;
import com.ctrip.zeus.service.message.queue.consumers.SlbCheckStatusConsumer;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.util.CircularArray;
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
    public void testScheduledTask() throws Exception {
        //test consistent check
        StatusCheckCountSlbDo d1 = new StatusCheckCountSlbDo().setSlbId(1L).setCount(1).setDataSet("1").setDataSetTimestamp(System.currentTimeMillis());
        statusCheckCountSlbDao.insert(d1);
        Map<Long, List<GroupStatus>> data = new HashMap<>();
        List<GroupStatus> gs1 = new ArrayList<>();
        data.put(1L, gs1);
        gs1.add(generateGroupStatus(1L, false));

        List<GroupStatus> gs2 = new ArrayList<>();
        data.put(2L, gs2);
        gs2.add(generateGroupStatus(1L, false));
        gs2.add(generateGroupStatus(2L, true));

        slbCheckStatusRollingMachine.enable(true, new SlbCheckStatusConsumerInjector(data));

        slbCheckStatusRollingMachine.run();

        CircularArray<Integer> c1 = slbCheckStatusRollingMachine.getCheckFailureCount(1L);
        CircularArray<Integer> c2 = slbCheckStatusRollingMachine.getCheckFailureCount(2L);
        Assert.assertEquals(1, c1.getLast().intValue());
        Assert.assertEquals(1, c2.getLast().intValue());

        //test update rolling counter
        d1.setCount(2).setDataSet("1,1").setDataSetTimestamp(d1.getDataSetTimestamp() - 60000L);
        statusCheckCountSlbDao.updateDataSetById(d1, StatusCheckCountSlbEntity.UPDATESET_FULL);
        statusCheckCountSlbDao.updateCountBySlb(d1, StatusCheckCountSlbEntity.UPDATESET_FULL);

        slbCheckStatusRollingMachine.run();

        c1 = slbCheckStatusRollingMachine.getCheckFailureCount(1L);
        c2 = slbCheckStatusRollingMachine.getCheckFailureCount(2L);
        Assert.assertEquals(2, c1.getAll()[c1.size() - 2].intValue());
        Assert.assertEquals(1, c2.getLast().intValue());

    }

    @Test
    public void compress() throws IOException {
        String compressed = CompressUtils.compressToGzippedBase64String("1,2,3,4,5,6,7");
        String value = CompressUtils.decompressGzippedBase64String(compressed);
        Assert.assertEquals("1,2,3,4,5,6,7", value);
    }

    @Test
    public void migrate() throws Exception {
        GroupStatus e = generateGroupStatus(3L, false);
        //test new
        slbCheckStatusRollingMachine.migrate(Sets.newHashSet(3L), e);

        CircularArray<Integer> c3 = slbCheckStatusRollingMachine.getCheckFailureCount(3L);
        Assert.assertEquals(1, c3.getLast().intValue());

        //test add rel
        slbCheckStatusRollingMachine.migrate(Sets.newHashSet(3L, 4L), e);
        c3 = slbCheckStatusRollingMachine.getCheckFailureCount(3L);
        CircularArray<Integer> c4 = slbCheckStatusRollingMachine.getCheckFailureCount(4L);
        Assert.assertEquals(1, c3.getLast().intValue());
        Assert.assertEquals(1, c4.getLast().intValue());

        //test unchanged
        long timestamp = System.currentTimeMillis();
        slbCheckStatusRollingMachine.migrate(Sets.newHashSet(3L, 4L), e);
        for (StatusCheckCountSlbDo d : statusCheckCountSlbDao.findAllBySlb(new Long[]{3L, 4L}, StatusCheckCountSlbEntity.READSET_FULL)) {
            Assert.assertTrue(timestamp > d.getDataChangeLastTime().getTime());
        }

        //test remove
        slbCheckStatusRollingMachine.migrate(Sets.newHashSet(11L), e);
        CircularArray<Integer> c11 = slbCheckStatusRollingMachine.getCheckFailureCount(11L);
        Assert.assertEquals(1, c11.getLast().intValue());
        c3 = slbCheckStatusRollingMachine.getCheckFailureCount(3L);
        c4 = slbCheckStatusRollingMachine.getCheckFailureCount(4L);
        Assert.assertEquals(0, c3.getLast().intValue());
        Assert.assertEquals(0, c4.getLast().intValue());

        //test inconsistent group status
        e = generateGroupStatus(3L, true);
        slbCheckStatusRollingMachine.migrate(Sets.newHashSet(1L), e);
        c11 = slbCheckStatusRollingMachine.getCheckFailureCount(1L);
        Assert.assertEquals(0, c11.getLast().intValue());
    }

    @Test
    public void update() throws Exception {
        //init
        GroupStatus e = generateGroupStatus(5L, false);
        slbCheckStatusRollingMachine.migrate(Sets.newHashSet(5L, 6L), e);
        slbCheckStatusRollingMachine.migrate(Sets.newHashSet(5L, 6L), generateGroupStatus(6L, false));

        slbCheckStatusRollingMachine.update(e);

        CircularArray<Integer> c5 = slbCheckStatusRollingMachine.getCheckFailureCount(5L);
        CircularArray<Integer> c6 = slbCheckStatusRollingMachine.getCheckFailureCount(6L);
        Assert.assertEquals(2, c5.getLast().intValue());
        Assert.assertEquals(2, c6.getLast().intValue());

        e = generateGroupStatus(5L, true);
        slbCheckStatusRollingMachine.update(e);

        c5 = slbCheckStatusRollingMachine.getCheckFailureCount(5L);
        c6 = slbCheckStatusRollingMachine.getCheckFailureCount(6L);
        Assert.assertEquals(1, c5.getLast().intValue());
        Assert.assertEquals(1, c6.getLast().intValue());
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
                } catch (DalException e) {
                    Assert.assertFalse(true);
                }
            }
        }

        @Override
        public void consistentCheck(Set<Long> slbIds) {
            Set<Long> tmp = new HashSet<>(data.keySet());
            tmp.removeAll(slbIds);
            refresh(tmp);
        }
    }

}