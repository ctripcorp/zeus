package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.executor.TaskWorker;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
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
    private TaskExecutor taskExecutor;
    @Resource
    private LocalInfoService localInfoService;
    @Resource
    private ConfigHandler configHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute() {
        Long workerSlbId = null;

        try {
            //1. getWorkerSlbId
            workerSlbId = localInfoService.getLocalSlbId();
            if (!configHandler.getEnable("agent.task.worker", workerSlbId, null, null, true)) {
                return;
            }

            //2. check slbId
            if (workerSlbId == null) {
                logger.warn("Can Not Found Slb by Local Ip. NginxStatusFetcher ignore task! Local Ip : " + LocalInfoPack.INSTANCE.getIp());
                return;
            }

            //3. Do work
            taskExecutor.execute(workerSlbId);
        } catch (Exception e) {
            logger.error("Task Worker Execute Fail." + e.getMessage(), e);
        }
    }
}
