package com.ctrip.zeus.service.clean.impl;

import com.ctrip.zeus.dal.core.StatusHealthCheckDao;
import com.ctrip.zeus.dal.core.StatusHealthCheckDo;
import com.ctrip.zeus.service.clean.CleanFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by fanqq on 2015/12/17.
 */
@Service("statusHealthCheckCleanFilter")
public class StatusHealthCheckCleanFilter implements CleanFilter {
    @Resource
    private StatusHealthCheckDao statusHealthCheckDao;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void runFilter() throws Exception {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(new Date());
        rightNow.add(Calendar.HOUR_OF_DAY,-1);
        Date dt = rightNow.getTime();
        int count = statusHealthCheckDao.deleteByLastChangeTime(new StatusHealthCheckDo().setDataChangeLastTime(dt));
        logger.info("statusHealthCheck Delete Job: delete rows " + count);
    }

    @Override
    public int interval() {
        return 24;
    }
}
