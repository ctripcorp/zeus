package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.task.entity.OpsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fanqq on 2015/8/3.
 */
@Component("executorData")
public class ExecutorData {
    private HashMap<String , OpsTask> serverOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateSlbOps = new HashMap<>();
    private HashMap<Long , OpsTask> memberOps = new HashMap<>();
    private List<OpsTask> tasks = null;
    HashMap<Long , Group> activatedGroups = new HashMap<>();
    Slb activatedSlb = null;
    List<VirtualServer> buildVirtualServer = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void setTasks(List<OpsTask> tasks){
        this.tasks = new ArrayList<>(tasks);
        for (OpsTask task : tasks){
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_GROUP)){
                activateGroupOps.put(task.getGroupId(), task);
            }
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_SLB)){
                activateSlbOps.put(task.getSlbId(), task);
            }
            if (task.getOpsType().equals(TaskOpsType.SERVER_OPS)){
                serverOps.put(task.getIpList(), task);
            }
            if (task.getOpsType().equals(TaskOpsType.MEMBER_OPS)){
                memberOps.put(task.getGroupId(),task);
            }
        }
    }
    public void setTaskFail(OpsTask task,String failcause){
        task.setStatus(TaskStatus.FAIL);
        task.setFailCause(failcause);
        logger.warn("[Task Fail] OpsTask: Type["+task.getOpsType()+"],FailCause:"+failcause);
    }
}
