package com.ctrip.zeus.executor.scheduler;

import com.ctrip.zeus.executor.ModelSnapshotBuilder;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

@Service("slbTaskStarter")
public class SlbTaskStarter {
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private ModelSnapshotBuilder modelSnapshotBuilder;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void startUp() {
        try {
            Set<Long> ids = slbCriteriaQuery.queryAll();
            for (Long id : ids) {
                taskScheduler.addIfNotExist(new SlbTaskWorker(id, modelSnapshotBuilder));
            }
        } catch (Exception e) {
            logger.error("Start Up Slb Failed.", e);
        }
    }
}
