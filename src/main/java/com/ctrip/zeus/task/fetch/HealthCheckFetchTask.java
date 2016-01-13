package com.ctrip.zeus.task.fetch;

import com.ctrip.zeus.service.status.HealthCheckStatusService;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/1/5.
 */
@Component("healthCheckFetchTask")
public class HealthCheckFetchTask extends AbstractTask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private HealthCheckStatusService healthCheckStatusService ;
    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        try {
            healthCheckStatusService.freshHealthCheckStatus();
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
