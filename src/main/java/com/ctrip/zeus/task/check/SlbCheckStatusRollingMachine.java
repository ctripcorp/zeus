package com.ctrip.zeus.task.check;


import com.ctrip.zeus.dal.core.StatusCheckCountSlbDao;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbDo;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbEntity;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.CircularArray;
import com.ctrip.zeus.util.CompressUtils;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.io.IOException;
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
    private DbLockFactory dbLockFactory;

    private final static Logger logger = LoggerFactory.getLogger(SlbCheckStatusRollingMachine.class);

    private final Map<Long, CircularArray<Integer>> slbCheckFailureRollingCounter = new HashMap<>();
    private final Map<Long, TimestampWrapper> groupCacheBySlb = new HashMap<>();

    private final int bitmask;
    private final int failureId;

    private final static int HC = 1;

    private final AtomicBoolean initFlag = new AtomicBoolean(false);
    private boolean enabled;

    public SlbCheckStatusRollingMachine() {
        this(0x000F, HC, false);
    }

    public SlbCheckStatusRollingMachine(int bitmask, int failureId, boolean enabled) {
        this.bitmask = bitmask;
        this.failureId = failureId;
        this.enabled = enabled;
    }

    private void init() throws Exception {
        slbCheckFailureRollingCounter.clear();
        for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAll(StatusCheckCountSlbEntity.READSET_DATASET_EXCLUDED)) {
            CircularArray<Integer> v = new CircularArray<>(10);
            slbCheckFailureRollingCounter.put(e.getSlbId(), v);
            v.add(e.getCount());
        }
    }

    @Override
    public long getInterval() {
        return 10000;
    }

    public CircularArray<Integer> getCheckFailureCount(Long slbId) {
        return slbCheckFailureRollingCounter.get(slbId);
    }

    public Map<Long, CircularArray<Integer>> getCheckFailureCount() {
        return slbCheckFailureRollingCounter;
    }

    public void enable(boolean flag) {
        this.enabled = flag;
    }

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        if (!enabled) return;

        if (initFlag.compareAndSet(false, true)) {
            try {
                init();
                return;
            } catch (Exception e) {
                initFlag.compareAndSet(true, false);
                logger.error("Fail to initialize group check status by slb.", e);
                return;
            }
        }

        for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAll(StatusCheckCountSlbEntity.READSET_DATASET_EXCLUDED)) {
            CircularArray<Integer> v = slbCheckFailureRollingCounter.get(e.getSlbId());
            if (v == null) {
                v = new CircularArray<>(10);
                slbCheckFailureRollingCounter.put(e.getSlbId(), v);
            }
            v.add(e.getCount());
        }
    }

    @Override
    public void stop() {
        slbCheckFailureRollingCounter.clear();
    }

    protected int bitwiseStatus(GroupServerStatus ss) {
        int val3 = ss.getPull() ? 0 : 1;
        int val2 = ss.getServer() ? 0 : 1;
        int val1 = ss.getMember() ? 0 : 1;
        int val0 = ss.getHealthy() ? 0 : 1;
        return val0 | (val1 << 1) | (val2 << 2) | (val3 << 3);
    }

    public void migrate(Set<Long> prevSlbIds, Set<Long> currSlbIds, Set<Long> totalGroupIds, List<GroupStatus> groupStatuses) {
        Set<Long> failureGroupIds = new HashSet<>();
        for (GroupStatus groupStatus : groupStatuses) {
            totalGroupIds.add(groupStatus.getGroupId());
            for (GroupServerStatus ss : groupStatus.getGroupServerStatuses()) {
                int val = bitwiseStatus(ss);
                if ((bitmask & val) == failureId) {
                    failureGroupIds.add(groupStatus.getGroupId());
                    break;
                }
            }
        }

        Set<Long> obj = new HashSet<>();
        Map<Long, Set<Long>> updatedCache = new HashMap<>();
        for (Long slbId : prevSlbIds) {
            updatedCache.put(slbId, obj);
        }
        for (Long slbId : currSlbIds) {
            updatedCache.put(slbId, obj);
        }
        try {
            List<StatusCheckCountSlbDo> toUpdate = new ArrayList<>();
            updateIfExpired(updatedCache.keySet(), groupCacheBySlb);
            for (Long slbId : updatedCache.keySet()) {
                TimestampWrapper v = groupCacheBySlb.get(slbId);
                Set<Long> dataSet = v.unmodifiableDataSet();
                if (prevSlbIds.contains(slbId)) {
                    dataSet.removeAll(totalGroupIds);
                }
                if (currSlbIds.contains(slbId)) {
                    dataSet.addAll(failureGroupIds);
                }
                if (!dataSet.equals(v.groupIds)) {
                    updatedCache.put(slbId, dataSet);
                    toUpdate.add(new StatusCheckCountSlbDo().setSlbId(slbId).setCount(dataSet.size()).setDataChangeLastTime(v.timestamp)
                            .setDataSet(new String(CompressUtils.compress(Joiner.on(",").join(dataSet)))));
                }
            }
            int[] rc = statusCheckCountSlbDao.update((StatusCheckCountSlbDo[]) toUpdate.toArray(), StatusCheckCountSlbEntity.UPDATESET_FULL);
            for (int i = 0; i < rc.length; i++) {
                if (rc[i] == 1) {
                    long slbId = toUpdate.get(i).getSlbId();
                    groupCacheBySlb.get(slbId).update(updatedCache.get(slbId));
                }
            }
        } catch (DalException e) {
        } catch (IOException e) {
        }
    }

    public void update(Set<Long> slbIds, GroupStatus groupStatus) {
        Long groupId = groupStatus.getGroupId();
        boolean failed = false;
        for (GroupServerStatus ss : groupStatus.getGroupServerStatuses()) {
            int val = bitwiseStatus(ss);
            if ((bitmask & val) == failureId) {
                failed = true;
                break;
            }
        }

        try {
            updateIfExpired(slbIds, groupCacheBySlb);
            for (Long slbId : slbIds) {
                int rc = 0;
                TimestampWrapper v = groupCacheBySlb.get(slbId);
                Set<Long> dataSet = v.unmodifiableDataSet();
                if (failed && dataSet.add(groupId)) {
                    rc = statusCheckCountSlbDao.countIncrement(new StatusCheckCountSlbDo().setSlbId(slbId).setNum(1).setDataChangeLastTime(v.timestamp)
                            .setDataSet(new String(CompressUtils.compress(Joiner.on(",").join(dataSet)))), StatusCheckCountSlbEntity.UPDATESET_FULL);
                }
                if (!failed && dataSet.remove(groupId)) {
                    rc = statusCheckCountSlbDao.countDecrement(new StatusCheckCountSlbDo().setSlbId(slbId).setNum(1).setDataChangeLastTime(v.timestamp)
                            .setDataSet(new String(CompressUtils.compress(Joiner.on(",").join(dataSet)))), StatusCheckCountSlbEntity.UPDATESET_FULL);
                }
                if (rc == 1) {
                    groupCacheBySlb.get(slbId).update(dataSet);
                }
            }
        } catch (DalException e) {
        } catch (IOException e) {
        }
    }

    public void clear(Set<Long> slbIds, Long groupId) {
        try {
            updateIfExpired(slbIds, groupCacheBySlb);
            for (Long slbId : slbIds) {
                TimestampWrapper v = groupCacheBySlb.get(slbId);
                Set<Long> dataSet = v.unmodifiableDataSet();
                if (dataSet.remove(groupId)) {
                    int rc = statusCheckCountSlbDao.countDecrement(new StatusCheckCountSlbDo().setSlbId(slbId).setNum(1).setDataChangeLastTime(v.timestamp)
                            .setDataSet(new String(CompressUtils.compress(Joiner.on(",").join(dataSet)))), StatusCheckCountSlbEntity.UPDATESET_FULL);
                    if (rc == 1) {
                        v.update(dataSet);
                    }
                }
            }
        } catch (DalException e) {
        } catch (IOException e) {
        }
    }

    private void updateIfExpired(Set<Long> slbIds, Map<Long, TimestampWrapper> cache) {
        List<Long> expired = new ArrayList<>();
        try {
            for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAllBySlb(slbIds.toArray((Long[]) slbIds.toArray()), StatusCheckCountSlbEntity.READSET_DATASET_EXCLUDED)) {
                TimestampWrapper v = cache.get(e.getSlbId());
                if (v == null) expired.add(e.getSlbId());
                if (e.getDataChangeLastTime().compareTo(v.timestamp) > 0) expired.add(e.getSlbId());
            }

            for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAllBySlb((Long[]) expired.toArray(), StatusCheckCountSlbEntity.READSET_FULL)) {
                Set<Long> groupSet = new HashSet<>();
                if (e.getDataSet() != null) {
                    String dataset = new String(CompressUtils.decompress(e.getDataSet().getBytes()));
                    for (String s : dataset.split(",")) {
                        groupSet.add(Long.parseLong(s));
                    }
                }
            }
        } catch (DalException e) {
        } catch (IOException e) {
        }
    }

    private class TimestampWrapper {
        Set<Long> groupIds;
        Date timestamp;

        public TimestampWrapper(Set<Long> groupIds, Date timestamp) {
            this.groupIds = groupIds;
            this.timestamp = timestamp;
        }

        Set<Long> unmodifiableDataSet() {
            return new HashSet<>(groupIds);
        }

        void update(Set<Long> groupIds) {
            this.groupIds = groupIds;
            timestamp = new Date();
        }
    }
}