package com.ctrip.zeus.service.woker.impl;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.woker.TaskWorker;
import com.ctrip.zeus.util.S;
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

    @Override
    public void execute() {
        try {
            init();
        }catch (Exception e ){

        }

    }
    private void init()throws Exception{

        Slb slb = slbRepository.getBySlbServer(S.getIp());
        workerSlbId = slb.getId();
    }
}
