package com.ctrip.zeus.service.woker.impl;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.woker.TaskExecutor;
import com.ctrip.zeus.service.woker.TaskWorker;
import com.ctrip.zeus.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2015/7/28.
 */
@Component("taskWorker")
public class TaskWorkerImpl implements TaskWorker {
    private static Long workerSlbId = null;
    @Resource
    SlbRepository slbRepository;
    @Resource
    TaskExecutor taskExecutor;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute() {
        try {
            //1. init
            init();

            //2. execute
            taskExecutor.execute(workerSlbId);

        }catch (Exception e ){
            logger.error("Task Worker Execute Fail."+e.getMessage(),e);
        }

    }
    private void init()throws Exception{
        if (workerSlbId != null) return;
        Slb slb = slbRepository.getBySlbServer(S.getIp());
        if (slb == null){
            logger.error("Can Not Found Slb by Local Ip. TaskExecutor is not working!");
            return;
        }
        workerSlbId = slb.getId();
    }
}
