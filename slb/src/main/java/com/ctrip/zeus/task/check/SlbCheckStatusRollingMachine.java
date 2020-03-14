package com.ctrip.zeus.task.check;

import com.ctrip.zeus.dao.entity.StatusCheckCountSlb;
import com.ctrip.zeus.dao.entity.StatusCheckCountSlbExample;
import com.ctrip.zeus.dao.entity.StatusStatsGroupSlb;
import com.ctrip.zeus.dao.entity.StatusStatsGroupSlbExample;
import com.ctrip.zeus.dao.mapper.StatusCheckCountSlbMapper;
import com.ctrip.zeus.dao.mapper.StatusStatsGroupSlbMapper;
import com.ctrip.zeus.model.status.GroupServerStatus;
import com.ctrip.zeus.model.status.GroupStatus;
import com.ctrip.zeus.service.message.queue.consumers.SlbCheckStatusConsumer;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.CircularArray;
import com.ctrip.zeus.util.EnvHelper;
import com.google.common.base.Joiner;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import jersey.repackaged.com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2016/12/5.
 */
@Component("slbCheckStatusRollingMachine")
public class SlbCheckStatusRollingMachine extends AbstractTask {
    @Resource
    private StatusStatsGroupSlbMapper statusStatsGroupSlbMapper;
    @Resource
    private StatusCheckCountSlbMapper statusCheckCountSlbMapper;


    private SlbCheckStatusConsumer master;

    private final int bitmask;
    private final int failureId;

    private final static int HC = 0;
    private static final int CIRCULARARRAY_LEN = 10;

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

    public Map<Long, CircularArray<Integer>> getCheckFailureCount(Set<Long> slbIds) {
        Map<Long, CircularArray<Integer>> result = new HashMap<>();
        for (StatusCheckCountSlb e : statusCheckCountSlbMapper.selectByExample(new StatusCheckCountSlbExample())) {
            if (slbIds.contains(e.getSlbId())) {
                CircularArray<Integer> v = new CircularArray<>(CIRCULARARRAY_LEN, Integer.class);
                result.put(e.getSlbId(), v);

                for (String s : e.getDataSet().split(",")) {
                    v.add(Integer.parseInt(s));
                }
                v.add(e.getCount());
            }
        }
        return result;
    }

    public void enable(boolean flag, SlbCheckStatusConsumer master) {
        this.enabled = EnvHelper.portal() && flag;
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
            for (StatusCheckCountSlb e : statusCheckCountSlbMapper.selectByExample(new StatusCheckCountSlbExample())) {
                slbIds.add(e.getSlbId());
            }
            master.consistentCheck(slbIds);
        }
        long now = System.currentTimeMillis();
        List<StatusCheckCountSlb> list = statusCheckCountSlbMapper.selectByExample(new StatusCheckCountSlbExample());
        Iterator<StatusCheckCountSlb> iter = list.iterator();
        while (iter.hasNext()) {
            StatusCheckCountSlb e = iter.next();
            if (now - e.getDataSetTimestamp() >= getInterval()) {
                CircularArray<Integer> data = new CircularArray<>(CIRCULARARRAY_LEN, Integer.class);
                for (String s : e.getDataSet().split(",")) {
                    data.add(Integer.parseInt(s));
                }
                data.add(e.getCount());
                e.setDataSet(Joiner.on(",").join(data.getAll()));
                e.setDataSetTimestamp(now);
            } else {
                iter.remove();
            }
        }
        if (list.size() > 0) {
            statusCheckCountSlbMapper.updateDataSetById(list);
        }
    }

    @Override
    public void stop() {

    }

    protected int bitwiseStatus(GroupServerStatus ss) {
        int v1=1;
        int v2=2;
        int v3=3;
        int val3 = ss.getPull() ? 1 : 0;
        int val2 = ss.getServer() ? 1 : 0;
        int val1 = ss.getMember() ? 1 : 0;
        int val0 = ss.getHealthy() ? 1 : 0;
        return val0 | (val1 << v1) | (val2 << v2) | (val3 << v3);
    }

    public void migrate(Set<Long> targetSlbIds, GroupStatus groupStatus) {
        Long groupId = groupStatus.getGroupId();
        int statusValue = statusValue(groupStatus);

        Set<Long> affectedSlbIds = new HashSet<>();

        boolean shouldUpdate = false;
        List<StatusStatsGroupSlb> removeList = new ArrayList<>();
        for (StatusStatsGroupSlb e : statusStatsGroupSlbMapper.selectByExample(new StatusStatsGroupSlbExample().createCriteria().andGroupIdEqualTo(groupId).example())) {
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
            List<Long> removeIdLongList = new ArrayList<>();
            for (StatusStatsGroupSlb e : removeList) {
                removeIdLongList.add(e.getId());
            }
            statusStatsGroupSlbMapper.deleteByExample(new StatusStatsGroupSlbExample().createCriteria().andIdIn(removeIdLongList).example());
        }
        if (shouldUpdate) {
            statusStatsGroupSlbMapper.updateByExampleSelective(StatusStatsGroupSlb.builder().valStatus(statusValue).build(),
                    new StatusStatsGroupSlbExample().createCriteria().andGroupIdEqualTo(groupId).example());
        }
        if (targetSlbIds.size() > 0) {
            affectedSlbIds.addAll(targetSlbIds);
            List<StatusStatsGroupSlb> addList = new ArrayList<>();
            for (Long slbId : targetSlbIds) {
                addList.add(StatusStatsGroupSlb.builder().groupId(groupId).slbId(slbId).valStatus(statusValue).build());
            }
            statusStatsGroupSlbMapper.batchInsert(addList);
        }

        if (affectedSlbIds.size() > 0) {
            syncSlbCheckCount(affectedSlbIds, 0);
        }
    }

    public void syncSlbCheckCount(Set<Long> slbIds, int statusValue) {
        StatusCheckCountSlb[] checkCounts = new StatusCheckCountSlb[slbIds.size()];
        List<Map<Long, Integer>> countResult = statusStatsGroupSlbMapper.countBySlbAndStatus(new ArrayList<>(slbIds), statusValue);

        List<Long> longSlbIds = new ArrayList<>(slbIds);
        int i = 0;

        for (; i < countResult.size(); i++) {
            Long slbId = longSlbIds.get(i);
            Integer count = 0;

            for (Map<Long, Integer> map : countResult) {
                if (slbId.equals(Long.parseLong(String.valueOf(map.get("slb_id"))))) {
                    count = Integer.parseInt(String.valueOf(map.get("count")));
                    break;
                }
            }
            checkCounts[i] = new StatusCheckCountSlb.Builder().slbId(slbId).count(count).build();
            slbIds.remove(slbId);
        }

        for (Long slbId : slbIds) {
            checkCounts[i] = new StatusCheckCountSlb.Builder().slbId(slbId).count(0).build();
            i++;
        }

        List<StatusCheckCountSlb> toBeInserted = new ArrayList<>();
        for (int j = 0; j < checkCounts.length; j++) {
            if (statusCheckCountSlbMapper.updateCountBySlb(checkCounts[j]) == 0) {
                toBeInserted.add(new StatusCheckCountSlb.Builder().
                        count(checkCounts[j].getCount()).
                        slbId(checkCounts[j].getSlbId()).
                        dataSet(checkCounts[j].getCount() + "").
                        dataSetTimestamp(System.currentTimeMillis()).build());
            }
        }
        if (toBeInserted.size() > 0) {
            statusCheckCountSlbMapper.batchInsert(toBeInserted);
        }
    }


    public void update(GroupStatus groupStatus) {
        Long groupId = groupStatus.getGroupId();
        int statusValue = statusValue(groupStatus);
        List<StatusStatsGroupSlb> updateList = statusStatsGroupSlbMapper.selectByExample(new StatusStatsGroupSlbExample().createCriteria().andGroupIdEqualTo(groupId).example());
        Iterator<StatusStatsGroupSlb> iter = updateList.iterator();
        Set<Long> affectingSlbIds = new HashSet<>();
        while (iter.hasNext()) {
            StatusStatsGroupSlb e = iter.next();
            if (e.getValStatus() == statusValue) {
                iter.remove();
            } else {
                e.setValStatus(statusValue);
                affectingSlbIds.add(e.getSlbId());
            }
        }
        if (updateList.size() > 0) {
            statusStatsGroupSlbMapper.batchUpdateStatus(updateList);
        }
        if (affectingSlbIds.size() > 0) {
            syncSlbCheckCount(affectingSlbIds, 0);
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
        List<StatusStatsGroupSlb> removeList = statusStatsGroupSlbMapper.selectByExample(new StatusStatsGroupSlbExample().createCriteria().andGroupIdEqualTo(groupId).example());
        Set<Long> slbIds = new HashSet<>(removeList.size());
        for (int i = 0; i < removeList.size(); i++) {
            slbIds.add(removeList.get(i).getSlbId());
        }
        statusStatsGroupSlbMapper.deleteByExample(new StatusStatsGroupSlbExample().createCriteria().andGroupIdEqualTo(groupId).example());
        syncSlbCheckCount(slbIds, failureId);
    }

    public void refresh(Long slbId, List<GroupStatus> groupStatuses) {
        Map<Long, Integer> groupStatusValue = new HashMap<>();
        for (GroupStatus groupStatus : groupStatuses) {
            groupStatusValue.put(groupStatus.getGroupId(), statusValue(groupStatus));
        }
        if (groupStatusValue.keySet().size() == 0) return;

        List<StatusStatsGroupSlb> updateList = new ArrayList<>();
        for (StatusStatsGroupSlb e : statusStatsGroupSlbMapper.selectByExample(new StatusStatsGroupSlbExample().createCriteria().andGroupIdIn(new ArrayList<>(groupStatusValue.keySet())).example())) {
            if (slbId.equals(e.getSlbId())) {
                Integer v = groupStatusValue.remove(e.getGroupId());
                if (v.equals(e.getValStatus())) {
                    updateList.add(e);
                }
            }
        }

        if (updateList.size() > 0) {
            statusStatsGroupSlbMapper.batchUpdateStatus(updateList);
        }

        if (groupStatusValue.size() > 0) {
            StatusStatsGroupSlb[] addList = new StatusStatsGroupSlb[groupStatusValue.size()];
            int i = 0;
            for (Map.Entry<Long, Integer> e : groupStatusValue.entrySet()) {
                addList[i] = new StatusStatsGroupSlb().builder().groupId(e.getKey()).slbId(slbId).valStatus(e.getValue()).build();
                i++;
            }
            statusStatsGroupSlbMapper.batchInsert(Arrays.asList(addList));
        }

        syncSlbCheckCount(Sets.newHashSet(slbId), 0);
    }
}