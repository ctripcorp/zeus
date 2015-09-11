package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.executor.TaskWorker;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;   

/**
 * Created by fanqq on 2015/7/31.
 */
@Component("taskWorker")
public class TaskWorkerImpl implements TaskWorker {
    private static Long workerSlbId = null;
    @Resource
    SlbRepository slbRepository;
    @Resource
    TaskExecutor taskExecutor;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static int initFailCount = 0;
    @Override
    public void execute() {
        try {
            //1. init
            init();
        }catch (Exception e ){
            logger.error("Task Worker Init Fail."+e.getMessage(),e);
        }
        //2. execute
        if (workerSlbId!=null){
            taskExecutor.execute(workerSlbId);
        }else {
            logger.error("Task Worker Start Fail. WorkerSlbId is null!");
        }
    }

    private void init()throws Exception{
        Slb slb = slbRepository.getBySlbServer(S.getIp());
        if (slb != null && slb.getId()!=null){
            workerSlbId = slb.getId();
            initFailCount = 0;
        }else{
            if (++initFailCount > 3){
                workerSlbId = null;
            }
            logger.error("Can Not Found Slb by Local Ip. TaskExecutor is not working!");
        }
    }
}
