package com.ctrip.zeus.executor.scheduler;

import com.ctrip.zeus.executor.ModelSnapshotBuilder;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicDoubleProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SlbTaskWorker extends Thread {
    private ModelSnapshotBuilder builder;
    private Long targetSlbId;
    private Logger logger = LoggerFactory.getLogger(this.getName());
    private int skipTime = 0;
    private DynamicDoubleProperty weight = DynamicPropertyFactory.getInstance().getDoubleProperty("slb.task.worker.skip.interval", 1);

    SlbTaskWorker(Long slbId, ModelSnapshotBuilder builder) {
        this.targetSlbId = slbId;
        this.builder = builder;
    }

    public Long getTargetSlbId() {
        return targetSlbId;
    }

    public SlbTaskWorker setTargetSlbId(Long getTargetSlbId) {
        this.targetSlbId = getTargetSlbId;
        return this;
    }

    @Override
    public void run() {
        try {
            if (skipTime > 0) {
                skipTime--;
                logger.info("[slbTaskWorker=" + targetSlbId + "]Skip Executer.SkipTime:" + skipTime);
                return;
            }
            DynamicBooleanProperty enable = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.task.worker.enable." + targetSlbId, false);
            DynamicBooleanProperty enableAll = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.task.worker.enable.all", false);

            if (enable.get() || enableAll.get()) {
                builder.build(targetSlbId);
                skipTime += weight.get();
            }
        } catch (Exception e) {
            logger.warn("[slbTaskWorker=" + targetSlbId + "]Build Model Layer Failed.", e);
        }
    }
}
