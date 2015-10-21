package com.ctrip.zeus.clean;

import com.ctrip.zeus.executor.TaskWorker;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Created by fanqq on 2015/10/16.
 */
@DisallowConcurrentExecution
public class CleanDbJob extends QuartzJobBean {
    CleanDbManager cleanDbManager;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            cleanDbManager.run();
        }catch (Exception e){
            logger.warn("[clean db job] clean db exception."+e.getMessage(),e);
        }
    }
    public void setCleanDbManager(CleanDbManager cleanDbManager) {
        this.cleanDbManager = cleanDbManager;
    }
}
