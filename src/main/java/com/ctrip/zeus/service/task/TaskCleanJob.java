package com.ctrip.zeus.service.task;

import com.ctrip.zeus.dal.core.OperationLogDao;
import com.ctrip.zeus.dal.core.OperationLogDo;
import com.ctrip.zeus.dal.core.TaskDao;
import com.ctrip.zeus.dal.core.TaskDo;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.unidal.dal.jdbc.DalException;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by fanqq on 2015/8/24.
 */
public class TaskCleanJob extends QuartzJobBean {

    private TaskDao taskDao ;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(new Date());
            rightNow.add(Calendar.DAY_OF_YEAR,-7);
            Date dt = rightNow.getTime();
            this.taskDao.deleteBeforeDatetime(new TaskDo().setCreateTime(dt));
        } catch (DalException e) {
            e.printStackTrace();
            logger.warn("clean Tasks fail! "+ e.getMessage());
        }
    }

    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

}
