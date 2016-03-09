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
import org.unidal.dal.jdbc.DalException;

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
    private static DynamicIntProperty forceGet = DynamicPropertyFactory.getInstance().getIntProperty("fetch.health.check.force.getGlobalJob", 50);


    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        try {
            String ip = S.getIp();
            Long[] slbIds = entityFactory.getSlbIdsByIp(S.getIp(), SelectionMode.ONLINE_EXCLUSIVE);
            if (slbIds == null || slbIds.length < 1) {
                logger.warn("[HealthCheckFetchTask] Not belong to any slb.Task is skipped.ip:" + ip);
                return;
            }
            String key = "HealthCheckFetch_" + slbIds[0];
            DistLock lock = dbLockFactory.newLock(key);
            boolean flag = false;
            try {
                if (flag = lock.tryLock()) {

                    if (getTicket(key, interval.get(), 3)) {
                        healthCheckStatusService.freshHealthCheckStatus();
                        commitTicket(key, 3);
                    }
                }
            } catch (Exception e) {
                throw e;
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

    private boolean getTicket(String key, int interval, int retry) {
        if (retry <= 0) {
            return false;
        }
        try {
            logger.info("HealthCheckFetchTask", "Start get ticket. retry:" + retry);
            GlobalJobDo globalJobDo = globalJobDao.findByPK(key, GlobalJobEntity.READSET_FULL);
            if (globalJobDo == null) {
                globalJobDo = new GlobalJobDo().setJobKey(key).setStartTime(new Date()).setStatus("DOING")
                        .setDataChangeLastTime(new Date()).setFinishTime(new Date()).setOwner(S.getIp());
                globalJobDao.insert(globalJobDo);
                return true;
            } else {
                if (!globalJobDo.getStatus().equals("DOING")) {
                    long last = globalJobDo.getFinishTime() == null ? 0 : globalJobDo.getFinishTime().getTime();
                    long time = System.currentTimeMillis() - last;
                    if (time >= interval) {
                        globalJobDo.setStartTime(new Date()).setStatus("DOING")
                                .setDataChangeLastTime(new Date()).setOwner(S.getIp());
                        globalJobDao.updateByPK(globalJobDo, GlobalJobEntity.UPDATESET_FULL);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    long last = globalJobDo.getFinishTime() == null ? 0 : globalJobDo.getFinishTime().getTime();
                    long time = System.currentTimeMillis() - last;
                    if (time >= forceGet.get() * interval) {
                        globalJobDo.setStartTime(new Date()).setStatus("DOING")
                                .setDataChangeLastTime(new Date()).setOwner(S.getIp());
                        globalJobDao.updateByPK(globalJobDo, GlobalJobEntity.UPDATESET_FULL);
                        return true;
                    } else {
                        return false;
                    }
                }
            }

        } catch (Exception e) {
            retry--;
            logger.info("[HealthCheckFetchJob]getTicket retry.retry:" + retry, e);
            return getTicket(key, interval, retry);
        }
    }

    private void commitTicket(String key, int retry) {
        if (retry < 0) {
            return;
        }
        logger.info("HealthCheckFetchTask", "Start commit ticket. retry:" + retry);

        GlobalJobDo globalJobDo = new GlobalJobDo().setJobKey(key).setFinishTime(new Date()).setStatus("DONE")
                .setDataChangeLastTime(new Date()).setOwner(S.getIp());
        try {
            globalJobDao.updateByPK(globalJobDo, GlobalJobEntity.UPDATESET_FULL);
        } catch (DalException e) {
            retry--;
            logger.info("[HealthCheckFetchJob]commitTicket retry.retry:" + retry, e);
            commitTicket(key, retry);
        }
    }

}
