package com.ctrip.zeus.task.clean;

import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.nginx.handler.NginxConfOpsService;
import com.ctrip.zeus.task.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("cleanRollbackFileTask")
public class CleanRollbackFileTask extends AbstractTask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private NginxConfOpsService nginxConfOpsService;
    @Resource
    private ConfigHandler configHandler;

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        try {
            if (!configHandler.getEnable("clean.rollback.file.task", null, null, null, true)) {
                return;
            }
            logger.info("[CleanRollbackFileTask] clean RollbackFile task started.");
            nginxConfOpsService.cleanRollbackFiles();
            logger.info("[CleanRollbackFileTask] clean RollbackFile task finished.");
        } catch (Exception e) {
            logger.warn("[CleanRollbackFileTask] clean RollbackFile exception." + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 60000 * 30;
    }

}
