package com.ctrip.zeus.service.status;

import com.ctrip.zeus.dal.core.TaskDao;
import com.ctrip.zeus.util.S;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Created by fanqq on 2015/11/12.
 */
@DisallowConcurrentExecution
public class HealthCheckFetchJob extends QuartzJobBean {
    private HealthCheckStatusService healthCheckStatusService ;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            healthCheckStatusService.freshHealthCheckStatus();
        } catch (Exception e) {
            logger.error("[HealthCheckFetchJob] HealthCheckFetch Exception! ServerIp:" + S.getIp(), e);
        }
    }
    public void setHealthCheckStatusService(HealthCheckStatusService healthCheckStatusService) {
        this.healthCheckStatusService = healthCheckStatusService;
    }
}
