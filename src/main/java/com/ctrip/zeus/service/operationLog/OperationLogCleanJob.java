package com.ctrip.zeus.service.operationLog;

import com.ctrip.zeus.dal.core.OperationLogDao;
import com.ctrip.zeus.dal.core.OperationLogDo;
import com.ctrip.zeus.dal.core.OperationLogEntity;
import com.ctrip.zeus.nginx.RollingTrafficStatus;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.unidal.dal.jdbc.DalException;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by fanqq on 2015/7/21.
 */
@DisallowConcurrentExecution
public class OperationLogCleanJob extends QuartzJobBean {
    private OperationLogDao operationLogDao ;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(new Date());
            rightNow.add(Calendar.DAY_OF_YEAR,-7);
            Date dt = rightNow.getTime();
            this.operationLogDao.deleteBeforeDatetime(new OperationLogDo().setDatetime(dt));
        } catch (DalException e) {
            e.printStackTrace();
            logger.warn("clean operation log fail! "+ e.getMessage());
        }
    }
    public void setOperationLogDao(OperationLogDao operationLogDao) {
        this.operationLogDao = operationLogDao;
    }

}
