package com.ctrip.zeus.service.clean.impl;

import com.ctrip.zeus.clean.CleanDbManager;
import com.ctrip.zeus.dal.core.OperationLogDao;
import com.ctrip.zeus.dal.core.OperationLogDo;
import com.ctrip.zeus.service.clean.AbstractCleanFilter;
import com.ctrip.zeus.service.clean.CleanFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by fanqq on 2015/10/20.
 */
@Service("oprationLogCleanFilter")
public class OprationLogCleanFilter extends AbstractCleanFilter {
    @Resource
    private OperationLogDao operationLogDao;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void runFilter() throws Exception {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(new Date());
        rightNow.add(Calendar.DAY_OF_YEAR, -7);
        Date dt = rightNow.getTime();
        for (int i = 0; i < 20; i++) {
            int count = operationLogDao.deleteBeforeDatetime(new OperationLogDo().setDatetime(dt));
            logger.info("OprationLogCleanFilter Delete Job: delete rows " + count);
            if (count < 1000) {
                break;
            }
        }
    }
}
