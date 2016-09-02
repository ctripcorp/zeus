package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.executor.TaskWorker;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2015/7/31.
 */
@Component("taskWorker")
public class TaskWorkerImpl implements TaskWorker {
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private TaskExecutor taskExecutor;
    @Resource
    private EntityFactory entityFactory;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute() {
        String selfIp = S.getIp();
        Long workerSlbId = null;

        try {
            //1. getWorkerSlbId
            workerSlbId = getWorkerSlbId();

            //2. check slbId
            if (workerSlbId == null) {
                logger.warn("Can Not Found Slb by Local Ip. NginxStatusFetcher ignore task! Local Ip : " + selfIp);
                return;
            }

            //3. Do work
            taskExecutor.execute(workerSlbId);
        } catch (Exception e) {
            logger.error("Task Worker Execute Fail." + e.getMessage(), e);
        }
    }

    private Long getWorkerSlbId() throws Exception {
        Long workerSlbId = null;
        String selfIp = LocalInfoPack.INSTANCE.getIp();
        Long[] slbIds = entityFactory.getSlbIdsByIp(selfIp, SelectionMode.ONLINE_FIRST);
        if (slbIds != null && slbIds.length > 0) {
            workerSlbId = slbIds[0];
        }
        return workerSlbId;
    }
}
