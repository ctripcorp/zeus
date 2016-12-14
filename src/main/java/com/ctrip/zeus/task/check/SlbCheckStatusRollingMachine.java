package com.ctrip.zeus.task.check;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.message.queue.consumers.SlbCheckStatusConsumer;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.CircularArray;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2016/12/5.
 */
@Component("slbCheckStatusRollingMachine")
public class SlbCheckStatusRollingMachine extends AbstractTask {
    @Resource
    private StatusCheckCountSlbDao statusCheckCountSlbDao;
    @Resource
    private StatsGroupSlbDao statsGroupSlbDao;

    private final static Logger logger = LoggerFactory.getLogger(SlbCheckStatusRollingMachine.class);

    private SlbCheckStatusConsumer master;

    private final int bitmask;
    private final int failureId;

    private final static int HC = 1;

    private final AtomicBoolean initFlag = new AtomicBoolean(false);
    private boolean enabled;

    public SlbCheckStatusRollingMachine() {
        this(0x1, HC, false);
    }

    public SlbCheckStatusRollingMachine(int bitmask, int failureId, boolean enabled) {
        this.bitmask = bitmask;
        this.failureId = failureId;
        this.enabled = enabled;
    }

    @Override
    public long getInterval() {
        return 10000;
    }

    public CircularArray<Integer> getCheckFailureCount(Long slbId) {
        try {
            StatusCheckCountSlbDo e = statusCheckCountSlbDao.findBySlb(slbId, StatusCheckCountSlbEntity.READSET_FULL);
            if (e == null) return null;

            CircularArray<Integer> result = new CircularArray<>(10, Integer.class);
            for (String s : e.getDataSet().split(",")) {
                result.add(Integer.parseInt(s));
            }
            result.add(e.getCount());
        } catch (DalException e) {
            logger.error("Db exception from listing status check count by slb.", e);
        }
        return null;
    }

    public Map<Long, CircularArray<Integer>> getCheckFailureCount(Set<Long> slbIds) {
        Map<Long, CircularArray<Integer>> result = new HashMap<>();
        try {
            for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAll(StatusCheckCountSlbEntity.READSET_FULL)) {
                if (slbIds.contains(e.getSlbId())) {
                    CircularArray<Integer> v = new CircularArray<>(10, Integer.class);
                    result.put(e.getSlbId(), v);

                    for (String s : e.getDataSet().split(",")) {
                        v.add(Integer.parseInt(s));
                    }
                    v.add(e.getCount());
                }
            }
        } catch (DalException e) {
            logger.error("Db exception from listing status check count of all.", e);
        }
        return result;
    }

    public void enable(boolean flag, SlbCheckStatusConsumer master) {
        this.enabled = flag;
        this.master = master;
    }

    @Override
    public void start() {

    }

    @Override
    public void run() {
        if (!enabled) return;

        if (initFlag.compareAndSet(false, true)) {
            Set<Long> slbIds = new HashSet<>();
            try {
                for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAll(StatusCheckCountSlbEntity.READSET_SLB_ONLY)) {
                    slbIds.add(e.getSlbId());
                }
            } catch (DalException e) {
                logger.error("Db exception from listing slb-id from status collection.", e);
            }
            master.consistentCheck(slbIds);
        }

        long now = System.currentTimeMillis();
        try {
            List<StatusCheckCountSlbDo> list = statusCheckCountSlbDao.findAll(StatusCheckCountSlbEntity.READSET_FULL);
            Iterator<StatusCheckCountSlbDo> iter = list.iterator();
            while (iter.hasNext()) {
                StatusCheckCountSlbDo e = iter.next();
                if (now - e.getDataChangeLastTime().getTime() >= getInterval()) {
                    CircularArray<Integer> data = new CircularArray<>(10, Integer.class);
                    for (String s : e.getDataSet().split(",")) {
                        data.add(Integer.parseInt(s));
                    }
                    data.add(e.getCount());
                    e.setDataSet(Joiner.on(",").join(data.getAll()));
                } else {
                    iter.remove();
                }
            }
            statusCheckCountSlbDao.updateDataSetById(list.toArray(new StatusCheckCountSlbDo[list.size()]), StatusCheckCountSlbEntity.UPDATESET_FULL);
        } catch (DalException e) {
            logger.error("Db exception from maintain count array value.", e);
        }
    }

    @Override
    public void stop() {

    }

    protected int bitwiseStatus(GroupServerStatus ss) {
        int val3 = ss.getPull() ? 1 : 0;
        int val2 = ss.getServer() ? 1 : 0;
        int val1 = ss.getMember() ? 1 : 0;
        int val0 = ss.getHealthy() ? 1 : 0;
        return val0 | (val1 << 1) | (val2 << 2) | (val3 << 3);
    }

    public void migrate(Set<Long> targetSlbIds, GroupStatus groupStatus) {
        Long groupId = groupStatus.getGroupId();
        int statusValue = statusValue(groupStatus);

        Set<Long> affectedSlbIds = new HashSet<>();

        try {
            boolean shouldUpdate = false;
            List<StatsGroupSlbDo> removeList = new ArrayList<>();
            for (StatsGroupSlbDo e : statsGroupSlbDao.findAllByGroup(groupId, StatsGroupSlbEntity.READSET_FULL)) {
                if (!targetSlbIds.remove(e.getSlbId())) {
                    shouldUpdate = true;
                    affectedSlbIds.add(e.getSlbId());
                    removeList.add(e);
                    continue;
                }
                if (e.getValStatus() != statusValue) {
                    shouldUpdate = true;
                    affectedSlbIds.add(e.getSlbId());
                }
            }

            if (removeList.size() > 0) {
                statsGroupSlbDao.deleteById(removeList.toArray(new StatsGroupSlbDo[removeList.size()]));
            }
            if (shouldUpdate) {
                statsGroupSlbDao.updateStatusByGroup(new StatsGroupSlbDo().setGroupId(groupId).setValStatus(statusValue), StatsGroupSlbEntity.UPDATESET_FULL);
            }
            if (targetSlbIds.size() > 0) {
                affectedSlbIds.addAll(targetSlbIds);
                List<StatsGroupSlbDo> addList = new ArrayList<>();
                for (Long slbId : targetSlbIds) {
                    addList.add(new StatsGroupSlbDo().setGroupId(groupId).setSlbId(slbId).setValStatus(statusValue));
                }
                statsGroupSlbDao.insert(addList.toArray(new StatsGroupSlbDo[addList.size()]));
            }

            if (affectedSlbIds.size() > 0) {
                syncSlbCheckCount(affectedSlbIds.toArray(new Long[affectedSlbIds.size()]), 0);
            }
        } catch (DalException e) {
            logger.error("Db exception from migrating group status across slbs.", e);
        }
    }

    private void syncSlbCheckCount(Long[] slbIds, int statusValue) throws DalException {
        StatusCheckCountSlbDo[] checkCounts = new StatusCheckCountSlbDo[slbIds.length];
        List<StatsGroupSlbDo> countResult = statsGroupSlbDao.countBySlbAndStatus(slbIds, statusValue, StatsGroupSlbEntity.READSET_COUNT);
        for (int i = 0; i < checkCounts.length; i++) {
            StatsGroupSlbDo e = countResult.get(i);
            checkCounts[i] = new StatusCheckCountSlbDo().setSlbId(e.getSlbId()).setCount(e.getCount());
        }
        int[] returnedValue = statusCheckCountSlbDao.updateCountBySlb(checkCounts, StatusCheckCountSlbEntity.UPDATESET_FULL);
        for (int i = 0; i < returnedValue.length; i++) {
            if (returnedValue[i] == 0) {
                StatusCheckCountSlbDo e = checkCounts[i];
                e.setDataSet(e.getCount() + "").setDataSetTimestamp(System.currentTimeMillis());
                statusCheckCountSlbDao.insert(e);
            }
        }
    }

    public void update(GroupStatus groupStatus) {
        Long groupId = groupStatus.getGroupId();
        int statusValue = statusValue(groupStatus);
        try {
            List<StatsGroupSlbDo> updateList = statsGroupSlbDao.findAllByGroup(groupId, StatsGroupSlbEntity.READSET_FULL);
            Iterator<StatsGroupSlbDo> iter = updateList.iterator();
            List<Long> affectingSlbIds = new ArrayList<>();
            while (iter.hasNext()) {
                StatsGroupSlbDo e = iter.next();
                if (e.getValStatus() == statusValue) {
                    iter.remove();
                } else {
                    affectingSlbIds.add(e.getSlbId());
                }
            }
            if (updateList.size() > 0) {
                statsGroupSlbDao.updateStatusById(updateList.toArray(new StatsGroupSlbDo[updateList.size()]), StatsGroupSlbEntity.UPDATESET_FULL);
            }
            if (affectingSlbIds.size() > 0) {
                syncSlbCheckCount(affectingSlbIds.toArray(new Long[affectingSlbIds.size()]), 0);
            }
        } catch (DalException e) {
            logger.error("An unexpected error occurred when updating group status.", e);
        }
    }

    protected int statusValue(GroupStatus groupStatus) {
        boolean failed = false;
        for (GroupServerStatus ss : groupStatus.getGroupServerStatuses()) {
            int val = bitwiseStatus(ss);
            if ((bitmask & val) == failureId) {
                failed = true;
            }
        }
        return failed ? 0 : 1;
    }

    public void clear(Long groupId) {
        try {
            List<StatsGroupSlbDo> removeList = statsGroupSlbDao.findAllByGroup(groupId, StatsGroupSlbEntity.READSET_FULL);
            Long[] slbIds = new Long[removeList.size()];
            for (int i = 0; i < removeList.size(); i++) {
                slbIds[i] = removeList.get(i).getSlbId();
            }
            statsGroupSlbDao.deleteByGroup(new StatsGroupSlbDo().setGroupId(groupId));

            syncSlbCheckCount(slbIds, 0);
        } catch (DalException e) {
            logger.error("An unexpected error occurred when clearing group from slbs.", e);
        }
    }

    public void refresh(Long slbId, List<GroupStatus> groupStatuses) throws DalException {
        Map<Long, Integer> groupStatusValue = new HashMap<>();
        for (GroupStatus groupStatus : groupStatuses) {
            groupStatusValue.put(groupStatus.getGroupId(), statusValue(groupStatus));
        }
        List<StatsGroupSlbDo> updateList = new ArrayList<>();
        for (StatsGroupSlbDo e : statsGroupSlbDao.findAllByGroups(groupStatusValue.keySet().toArray(new Long[groupStatusValue.size()]), StatsGroupSlbEntity.READSET_FULL)) {
            if (slbId.equals(e.getSlbId())) {
                Integer v = groupStatusValue.remove(e.getGroupId());
                e.setValStatus(v);
                updateList.add(e);
            }
        }

        if (groupStatusValue.size() > 0) {
            StatsGroupSlbDo[] addList = new StatsGroupSlbDo[groupStatusValue.size()];
            int i = 0;
            for (Map.Entry<Long, Integer> e : groupStatusValue.entrySet()) {
                addList[i] = new StatsGroupSlbDo().setGroupId(e.getKey()).setSlbId(slbId).setValStatus(e.getValue());
            }
            statsGroupSlbDao.insert(addList);
        }
        statsGroupSlbDao.updateStatusById(updateList.toArray(new StatsGroupSlbDo[updateList.size()]), StatsGroupSlbEntity.UPDATESET_FULL);

        syncSlbCheckCount(new Long[]{slbId}, 0);
    }
}