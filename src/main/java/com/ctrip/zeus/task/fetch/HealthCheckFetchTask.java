package com.ctrip.zeus.task.fetch;

import com.ctrip.zeus.dal.core.GlobalJobDao;
import com.ctrip.zeus.dal.core.GlobalJobDo;
import com.ctrip.zeus.dal.core.GlobalJobEntity;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.status.HealthCheckStatusService;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by fanqq on 2016/1/5.
 */
@Component("healthCheckFetchTask")
public class HealthCheckFetchTask extends AbstractTask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private HealthCheckStatusService healthCheckStatusService;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private GlobalJobDao globalJobDao;

    private static DynamicIntProperty interval = DynamicPropertyFactory.getInstance().getIntProperty("fetch.health.check.interval", 5000);


    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        try {
            Long[] slbIds = entityFactory.getSlbIdsByIp(S.getIp(), SelectionMode.ONLINE_EXCLUSIVE);
            if (slbIds == null || slbIds.length < 1) {
                return;
            }
            DistLock lock = dbLockFactory.newLock("HealthCheckFetch_" + slbIds[0]);
            boolean flag = false;
            try {
                if (flag = lock.tryLock()) {
                    String key = "HealthCheckFetch_" + slbIds[0];
                    GlobalJobDo globalJobDo = globalJobDao.findByPK(key, GlobalJobEntity.READSET_FULL);
                    boolean needFetch = false;
                    if (globalJobDo == null) {
                        globalJobDo = new GlobalJobDo().setJobKey(key).setStartTime(new Date()).setStatus("DOING")
                                .setDataChangeLastTime(new Date()).setOwner(S.getIp());
                        globalJobDao.updateByPK(globalJobDo, GlobalJobEntity.UPDATESET_FULL);
                        needFetch = true;
                    } else if (!globalJobDo.getStatus().equals("DOING")) {
                        long last = globalJobDo.getFinishTime() == null ? 0 : globalJobDo.getFinishTime().getTime();
                        long time = System.currentTimeMillis() - last;
                        if (time > interval.get()) {
                            globalJobDo.setStartTime(new Date()).setStatus("DOING")
                                    .setDataChangeLastTime(new Date()).setOwner(S.getIp());
                            globalJobDao.updateByPK(globalJobDo, GlobalJobEntity.UPDATESET_FULL);
                            needFetch = true;
                        }
                    }
                    if (needFetch) {
                        healthCheckStatusService.freshHealthCheckStatus();
                        globalJobDo.setFinishTime(new Date()).setStatus("DONE")
                                .setDataChangeLastTime(new Date()).setOwner(S.getIp());
                        globalJobDao.updateByPK(globalJobDo, GlobalJobEntity.UPDATESET_FULL);
                    }
                }
            } finally {
                if (flag) {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            logger.error("[HealthCheckFetchJob] HealthCheckFetch Exception! ServerIp:" + S.getIp(), e);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 5000;
    }

}
