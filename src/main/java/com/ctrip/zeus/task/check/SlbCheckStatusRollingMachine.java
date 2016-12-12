package com.ctrip.zeus.task.check;


import com.ctrip.zeus.dal.core.StatusCheckCountSlbDao;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbDo;
import com.ctrip.zeus.dal.core.StatusCheckCountSlbEntity;
import com.ctrip.zeus.service.message.queue.consumers.SlbCheckStatusConsumer;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.CircularArray;
import com.ctrip.zeus.util.CompressUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
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

    private final static Logger logger = LoggerFactory.getLogger(SlbCheckStatusRollingMachine.class);

    private final Map<Long, CircularArray<Integer>> slbCheckFailureRollingCounter = new HashMap<>();
    private final Map<Long, DataSetTimeWrapper> groupCacheBySlb = new HashMap<>();

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

    private void init() throws Exception {
        slbCheckFailureRollingCounter.clear();
        for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAll(StatusCheckCountSlbEntity.READSET_DATASET_EXCLUDED)) {
            CircularArray<Integer> v = new CircularArray<>(10, Integer.class);
            slbCheckFailureRollingCounter.put(e.getSlbId(), v);
            v.add(e.getCount());
        }
    }

    @Override
    public long getInterval() {
        return 10000;
    }

    public List<Integer> getCheckFailureCount(Long slbId) {
        CircularArray<Integer> values = slbCheckFailureRollingCounter.get(slbId);
        if (values == null)
            return new ArrayList<>();
        ArrayList<Integer> result = Lists.newArrayList(slbCheckFailureRollingCounter.get(slbId).getAll());
        try {
            StatusCheckCountSlbDo d = statusCheckCountSlbDao.findBySlb(slbId, StatusCheckCountSlbEntity.READSET_DATASET_EXCLUDED);
            result.add(d.getCount());
        } catch (DalException e) {
        }
        return result;
    }

    public Map<Long, List<Integer>> getCheckFailureCount() {
        Map<Long, List<Integer>> result = new HashMap<>();
        try {
            for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAll(StatusCheckCountSlbEntity.READSET_DATASET_EXCLUDED)) {
                ArrayList<Integer> v = Lists.newArrayList(slbCheckFailureRollingCounter.get(e.getSlbId()).getAll());
                result.put(e.getSlbId(), v);
                v.add(e.getCount());
            }
        } catch (DalException e) {
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
                v = new CircularArray<>(10, Integer.class);
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
        int val3 = ss.getPull() ? 1 : 0;
        int val2 = ss.getServer() ? 1 : 0;
        int val1 = ss.getMember() ? 1 : 0;
        int val0 = ss.getHealthy() ? 1 : 0;
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
        Map<Long, Set<Long>> tmpGroupCache = new HashMap<>();
        for (Long slbId : prevSlbIds) {
            tmpGroupCache.put(slbId, obj);
        }
        for (Long slbId : currSlbIds) {
            tmpGroupCache.put(slbId, obj);
        }
        try {
            List<StatusCheckCountSlbDo> toUpdate = new ArrayList<>();
            updateIfExpired(tmpGroupCache.keySet());
            for (Long slbId : tmpGroupCache.keySet()) {
                DataSetTimeWrapper v = groupCacheBySlb.get(slbId);
                Set<Long> dataSet = v.unmodifiableDataSet();
                if (prevSlbIds.contains(slbId)) {
                    dataSet.removeAll(totalGroupIds);
                }
                if (currSlbIds.contains(slbId)) {
                    dataSet.addAll(failureGroupIds);
                }
                if (!dataSet.equals(v.groupIds)) {
                    tmpGroupCache.put(slbId, dataSet);
                    toUpdate.add(new StatusCheckCountSlbDo().setSlbId(slbId).setCount(dataSet.size())
                            .setDataSet(dataSetStringify(dataSet)));
                }
            }


            statusCheckCountSlbDao.update(toUpdate.toArray(new StatusCheckCountSlbDo[toUpdate.size()]), StatusCheckCountSlbEntity.UPDATESET_FULL);
            for (StatusCheckCountSlbDo e : toUpdate) {
                groupCacheBySlb.get(e.getSlbId()).update(tmpGroupCache.get(e.getSlbId()));
            }
        } catch (DalException e) {
            logger.error("An unexpected error occurred when migrating group status count.", e);
        } catch (IOException e) {
            logger.error("An unexpected error occurred when migrating group status count.", e);
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
            updateIfExpired(slbIds);
            for (Long slbId : slbIds) {
                DataSetTimeWrapper v = groupCacheBySlb.get(slbId);
                Set<Long> dataSet = v.unmodifiableDataSet();


                if (failed && dataSet.add(groupId)) {
                    statusCheckCountSlbDao.countIncrement(new StatusCheckCountSlbDo().setSlbId(slbId).setNum(1)
                            .setDataSet(dataSetStringify(dataSet)), StatusCheckCountSlbEntity.UPDATESET_FULL);
                }
                if (!failed && dataSet.remove(groupId)) {
                    statusCheckCountSlbDao.countDecrement(new StatusCheckCountSlbDo().setSlbId(slbId).setNum(1)
                            .setDataSet(dataSetStringify(dataSet)), StatusCheckCountSlbEntity.UPDATESET_FULL);
                }
                v.update(dataSet);
            }
        } catch (DalException e) {
            logger.error("An unexpected error occurred when updating group status count.", e);
        } catch (IOException e) {
            logger.error("An unexpected error occurred when updating group status count.", e);
        }
    }

    protected String dataSetStringify(Set<Long> dataSet) throws IOException {
        return CompressUtils.compressToGzippedBase64String(Joiner.on(",").join(dataSet));
    }

    public void clear(Set<Long> slbIds, Long groupId) {
        try {
            updateIfExpired(slbIds);
            for (Long slbId : slbIds) {
                DataSetTimeWrapper v = groupCacheBySlb.get(slbId);
                Set<Long> dataSet = v.unmodifiableDataSet();


                if (dataSet.remove(groupId)) {
                    statusCheckCountSlbDao.countDecrement(new StatusCheckCountSlbDo().setSlbId(slbId).setNum(1)
                            .setDataSet(dataSetStringify(dataSet)), StatusCheckCountSlbEntity.UPDATESET_FULL);
                    v.update(dataSet);
                }
            }
        } catch (DalException e) {
            logger.error("An unexpected error occurred when clearing group from slbs.", e);
        } catch (IOException e) {
            logger.error("An unexpected error occurred when clearing group from slbs.", e);
        }
    }

    protected void updateIfExpired(Set<Long> slbIds) {
        List<Long> expired = new ArrayList<>();
        try {
            for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAllBySlb(slbIds.toArray(slbIds.toArray(new Long[slbIds.size()])), StatusCheckCountSlbEntity.READSET_DATASET_EXCLUDED)) {
                DataSetTimeWrapper v = groupCacheBySlb.get(e.getSlbId());
                if (v == null) {
                    expired.add(e.getSlbId());
                    continue;
                }
                if (e.getDataChangeLastTime().compareTo(v.timestamp) > 0) expired.add(e.getSlbId());
            }

            for (StatusCheckCountSlbDo e : statusCheckCountSlbDao.findAllBySlb(expired.toArray(expired.toArray(new Long[expired.size()])), StatusCheckCountSlbEntity.READSET_FULL)) {
                Set<Long> groupSet = new HashSet<>();
                if (e.getDataSet() != null) {
                    String dataset = CompressUtils.decompressGzippedBase64String(e.getDataSet());
                    if (!dataset.isEmpty()) {
                        for (String s : dataset.split(",")) {
                            groupSet.add(Long.parseLong(s));
                        }
                    }
                }
                groupCacheBySlb.put(e.getSlbId(), new DataSetTimeWrapper(groupSet, e.getDataChangeLastTime()));
            }

            if (!groupCacheBySlb.keySet().containsAll(slbIds)) {
                HashSet<Long> diff = new HashSet<>(slbIds);
                master.refresh(diff);
            }
        } catch (DalException e) {
        } catch (IOException e) {
        }
    }

    public void refresh(Long slbId, List<GroupStatus> groupStatuses) throws IOException, DalException {
        Set<Long> failureGroupIds = new HashSet<>();
        for (GroupStatus groupStatus : groupStatuses) {
            for (GroupServerStatus ss : groupStatus.getGroupServerStatuses()) {
                int val = bitwiseStatus(ss);
                if ((bitmask & val) == failureId) {
                    failureGroupIds.add(groupStatus.getGroupId());
                    break;
                }
            }
        }
        statusCheckCountSlbDao.insertOrUpdate(new StatusCheckCountSlbDo().setSlbId(slbId).setCount(failureGroupIds.size())
                .setDataSet(dataSetStringify(failureGroupIds)));
        groupCacheBySlb.put(slbId, new DataSetTimeWrapper(failureGroupIds, new Date()));
    }

    private class DataSetTimeWrapper {
        Set<Long> groupIds;
        Date timestamp;

        public DataSetTimeWrapper(Set<Long> groupIds, Date timestamp) {
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