package com.ctrip.zeus.service.woker.impl;

import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.service.woker.TaskExecutor;
import com.ctrip.zeus.task.entity.OpsTask;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2015/7/29.
 */
@Component("taskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private TaskService taskService;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;

    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<String , OpsTask> serverOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateSlbOps = new HashMap<>();
    private HashMap<String , OpsTask> memberOps = new HashMap<>();
    private List<VirtualServer> buildVirtualServer = new ArrayList<>();
    private List<OpsTask> tasks = null;
    @Override
    public void execute(Long slbId) {
        DistLock buildLock = dbLockFactory.newLock( "TaskWorker_" + slbId );
        try {
            buildLock.lock(lockTimeout.get());
            executeJob(slbId);
        }catch (Exception e){
            logger.warn("TaskWorker get lock failed! TaskWorker: "+slbId);
        } finally{
            buildLock.unlock();
        }
    }

    private void executeJob(Long slbId){
        //1. get pending tasks , if size == 0 return
        try {
             tasks = taskService.getPendingTasks(slbId);
        }catch (Exception e){
            logger.warn("Task Executor get pending tasks failed! ", e);
            return;
        }
        if (tasks.size()==0) return;

        try {
            getTaskData(slbId);
        }catch (Exception e){
            // failed
        }

    }
    private void getTaskData(Long slbId){
        //2. get all tasks datas
        for (OpsTask task : tasks){
            //Activate group
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_GROUP)){
                boolean flag = false;
                try {
                    Group group = groupRepository.getById(task.getGroupId());
                    if (group==null){
                        task.setFailCause("Group Not Found!GroupId : "+ task.getGroupId());
                        task.setStatus(TaskStatus.FAIL);
                        continue;
                    }
                    for (GroupVirtualServer vs : group.getGroupVirtualServers()){
                        if(vs.getVirtualServer().getSlbId().equals(slbId)) {
                            buildVirtualServer.add(vs.getVirtualServer());
                            flag = true;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Get Group Fail! Can not read group from db!",e);
                    task.setFailCause("Get Group Fail!GroupId : "+ task.getGroupId());
                    task.setStatus(TaskStatus.FAIL);
                    continue;
                }
                if (!flag){
                    task.setFailCause("Task Info Error, Task target slbId is incorrect!GroupId : "+ task.getGroupId()+" SlbId:"+slbId);
                    task.setStatus(TaskStatus.FAIL);
                    continue;
                }
                activateGroupOps.put(task.getGroupId(),task);
            }
            //activate slb
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_SLB)){
                if (task.getSlbId().equals(slbId))
                {
                    try {
                        Slb slb = slbRepository.getById(slbId);
                        if (slb==null){
                            task.setFailCause("Slb Not Found!SlbId : "+ task.getSlbId());
                            task.setStatus(TaskStatus.FAIL);
                            continue;
                        }
                        for (VirtualServer vs : slb.getVirtualServers()){
                            buildVirtualServer.add(vs);
                        }
                    }catch (Exception e){
                        logger.error("Get Slb Fail! Can not read Slb from db!",e);
                        task.setFailCause("Slb Not Found!SlbId : "+ task.getSlbId());
                        task.setStatus(TaskStatus.FAIL);
                        continue;
                    }
                    activateSlbOps.put(task.getSlbId(),task);
                }
            }
            // server ops
            if (task.getOpsType().equals(TaskOpsType.SERVER_OPS)){
                boolean flag = false;
                try {
                    List<Group> groupList = groupRepository.listGroupsByGroupServer(task.getIpList());
                    if (groupList==null){
                        task.setFailCause("Not Found Group by server ip!Server Ip : "+ task.getIpList());
                        task.setStatus(TaskStatus.FAIL);
                        continue;
                    }

                    for (Group group : groupList){
                        for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()){
                            if (groupVirtualServer.getVirtualServer().getSlbId().equals(slbId)){
                                buildVirtualServer.add(groupVirtualServer.getVirtualServer());
                                flag = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    task.setFailCause("Can Not Found Group by server ip!Server Ip : "+ task.getIpList());
                    task.setStatus(TaskStatus.FAIL);
                    continue;
                }
                if (flag){
                    serverOps.put(task.getIpList(),task);
                }else{
                    task.setFailCause("Task Info Error, Task target slbId is incorrect!Server Ip : "+ task.getIpList()+" TargetSlbId:"+slbId);
                    task.setStatus(TaskStatus.FAIL);
                }
            }
            //member ops
            if (task.getOpsType().equals(TaskOpsType.MEMBER_OPS)){
                boolean flag = false;
                try {
                    Group group = groupRepository.getById(task.getGroupId());
                    if (group==null){
                        task.setFailCause("Group Not Found!GroupId : "+ task.getGroupId());
                        task.setStatus(TaskStatus.FAIL);
                        continue;
                    }
                    for (GroupVirtualServer vs : group.getGroupVirtualServers()){
                        if(vs.getVirtualServer().getSlbId().equals(slbId)) {
                            buildVirtualServer.add(vs.getVirtualServer());
                            flag = true;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Get Group Fail! Can not read group from db!",e);
                    task.setFailCause("Get Group Fail!GroupId : "+ task.getGroupId());
                    task.setStatus(TaskStatus.FAIL);
                    continue;
                }
                if (!flag){
                    task.setFailCause("Task Info Error, Task target slbId is incorrect!GroupId : "+ task.getGroupId()+" SlbId:"+slbId);
                    task.setStatus(TaskStatus.FAIL);
                    continue;
                }
                String[] iplist = task.getIpList().split(";");
                for (String ip : iplist)
                {
                    memberOps.put(task.getGroupId().toString()+"_"+ip,task);
                }
            }

        }
    }
}
