package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.activate.ServerGroupService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.task.entity.OpsTask;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fanqq on 2015/7/31.
 */
@Component("taskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private TaskService taskService;
    @Resource
    private ActivateService activateService;
    @Resource
    private BuildService buildService;
    @Resource
    StatusService statusService;
    @Resource
    NginxService nginxService;
    @Resource
    ServerGroupService serverGroupService;
    @Resource
    private BuildInfoService buildInfoService;

    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private HashMap<String , OpsTask> serverOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long , OpsTask> deactivateGroupOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateSlbOps = new HashMap<>();
    private HashMap<Long , OpsTask> memberOps = new HashMap<>();

    private List<OpsTask> tasks = null;

    @Override
    public void execute(Long slbId) {
        DistLock buildLock = dbLockFactory.newLock( "TaskWorker_" + slbId );
        try {
//            buildLock.lock(lockTimeout.get());
            if (buildLock.tryLock()){
                executeJob(slbId);
            }
        }catch (Exception e){
            logger.warn("TaskWorker get lock failed! Or Executor Failed! TaskWorker: " + slbId);
        } finally{
            buildLock.unlock();
        }
    }

    private void executeJob(Long slbId) throws Exception{
        HashMap<Long , Group> activatingGroups ;
        Slb activatingSlb ;
        List<VirtualServer> buildVirtualServer ;
        Set<Long> groupList ;

        //1. get pending tasks , if size == 0 return
        try {
            tasks = taskService.getPendingTasks(slbId);
        }catch (Exception e){
            logger.warn("Task Executor get pending tasks failed! ", e);
            return;
        }
        if (tasks.size()==0){
            return;
        }

        try {
            sortTaskData(slbId);
            activatingGroups = getActivatingGroups(activateGroupOps);
            activatingSlb = getActivatingSlb(activateSlbOps,slbId);

            preMemberOperation(slbId,memberOps);
            groupList = getInfluencedGroups(activatingGroups);

            if (activatingSlb!=null){
                buildVirtualServer = activatingSlb.getVirtualServers();
            }else{
                buildVirtualServer = buildService.getNeedBuildVirtualServers(slbId,activatingGroups,groupList);
            }
            Map<Long,List<Group>> groupMap = buildService.getInfluencedVsGroups(slbId,activatingGroups,buildVirtualServer,deactivateGroupOps.keySet());
            Set<String> allDownServers = getAllDownServer();
            Map<Long,Group> groups = new HashMap<>();
            for (Long vs : groupMap.keySet()){
                for (Group group : groupMap.get(vs)){
                    groups.put(group.getId(),group);
                }
            }
            Set<String> allUpGroupServers = getAllUpGroupServers(slbId,groups,activatingGroups);
            buildService.build(slbId,activatingSlb,buildVirtualServer,groupMap,allDownServers,allUpGroupServers);
            List<Long> vsIds = new ArrayList<>();
            for (VirtualServer vs : buildVirtualServer){
                vsIds.add(vs.getId());
            }
            nginxService.writeALLToDisk(slbId,vsIds);
            if (activatingSlb!=null||activatingGroups.size()>0){
                nginxService.loadAll(slbId);
            }else {
                //dyups
                Collection<Group> dyupsGroupList = groups.values();
                for (Group group : dyupsGroupList){
                    List<DyUpstreamOpsData> dyUpstreamOpsDataList = buildService.buildUpstream(slbId,allDownServers,allUpGroupServers,group);
                    nginxService.dyops(slbId,dyUpstreamOpsDataList);
                }
            }
            performTasks(slbId);
            updateVersion(slbId);
            setTaskResult(slbId,true,null);
        }catch (Exception e){
            // failed
            setTaskResult(slbId,false,e.getMessage());
            rollBack(slbId);
            throw e;
        }

    }

    private void updateVersion(Long slbId) {
        try {
            int current = buildInfoService.getCurrentTicket(slbId);
            buildInfoService.updateTicket(slbId,current+1);
        }catch (Exception e){
            logger.error("Update Version Fail!",e);
        }
    }

    private void rollBack(Long slbId) {
        try {
            int current = buildInfoService.getCurrentTicket(slbId);
            buildService.rollBackConfig(slbId,current);
            buildInfoService.resetPaddingTicket(slbId);
        }catch (Exception e){
            logger.error("RollBack Fail!",e);
        }
    }

    private void performTasks(Long slbId) {
        try {
            for (OpsTask task : tasks){
                if (!task.getStatus().equals(TaskStatus.DOING)){
                    continue;
                }
                if (task.getOpsType().equals(TaskOpsType.ACTIVATE_SLB)){
                    activateService.activeSlb(task.getSlbId(),task.getVersion());
                }else if (task.getOpsType().equals(TaskOpsType.ACTIVATE_GROUP)){
                    activateService.activeGroup(task.getGroupId(),task.getVersion());
                }else if (task.getOpsType().equals(TaskOpsType.DEACTIVATE_GROUP)){
                    activateService.deactiveGroup(task.getGroupId(),slbId);
                }else if (task.getOpsType().equals(TaskOpsType.SERVER_OPS)){
                    if (task.getUp()){
                        statusService.upServer(task.getIpList());
                    }else {
                        statusService.downServer(task.getIpList());
                    }
                }else if (task.getOpsType().equals(TaskOpsType.MEMBER_OPS)){

                    String [] ips = task.getIpList().split(";");
                    List<String>ipList = Arrays.asList(ips);
                    if (task.getUp()){
                        statusService.upMember(slbId,task.getGroupId(), ipList);
                    }else {
                        statusService.upMember(slbId,task.getGroupId(), ipList);
                    }
                }
            }
        }catch (Exception e){
            logger.error("Perform Tasks Fail! TargetSlbId:"+tasks.get(0).getTargetSlbId());
        }

    }

    private void setTaskResult(Long slbId,boolean isSuc,String failCause) {
        for (OpsTask task : tasks){
            if (task.getStatus().equals(TaskStatus.DOING)){
                if (isSuc)
                {
                    task.setStatus(TaskStatus.SUCCESS);
                }else {
                    task.setStatus(TaskStatus.FAIL);
                    task.setFailCause(failCause);
                }
            }
        }
        try {
            taskService.updateTasks(tasks);
        } catch (Exception e) {
            logger.error("Task Update Failed! TargetSlbId:"+slbId,e);
        }
    }

    private Set<String> getAllUpGroupServers(Long slbId,Map<Long, Group> groups, HashMap<Long, Group> activatingGroups) throws Exception {
        Set<String> allUpGroupServers = statusService.findAllUpGroupServersBySlbId(slbId);
        Set<Long> tmpid = memberOps.keySet();
        for (Long gid : tmpid){
            Group groupTmp = activatingGroups.get(gid);
            if (groupTmp==null){
                groupTmp = groups.get(gid);
            }
            String ipList = memberOps.get(gid).getIpList();
            String[]ips = ipList.split(";");
            for (GroupVirtualServer gvs : groupTmp.getGroupVirtualServers()){
                if (!gvs.getVirtualServer().getSlbId().equals(slbId)){
                    continue;
                }
                if (memberOps.get(gid).getUp()){
                    for (String ip : ips){
                        allUpGroupServers.add(slbId+"_"+gvs.getVirtualServer().getId()+"_"+gid+"_"+ip);
                    }
                }else {
                    for (String ip : ips){
                        allUpGroupServers.remove(slbId + "_" + gvs.getVirtualServer().getId() + "_" + gid + "_" + ip);
                    }
                }
            }
        }
        return allUpGroupServers;
    }

    private Set<String> getAllDownServer() throws Exception{
        Set<String> allDownServers = statusService.findAllDownServers();
        Set<String> serverip = serverOps.keySet();
        for (String ip : serverip){
            if (allDownServers.contains(ip)&&serverOps.get(ip).getUp()){
                allDownServers.remove(ip);
            }else if (!allDownServers.contains(ip)&&!serverOps.get(ip).getUp()){
                allDownServers.add(ip);
            }
        }
        return allDownServers;
    }

    private Set<Long> getInfluencedGroups(HashMap<Long, Group> activatingGroups) throws Exception{
        Set<Long> result = new HashSet<>();
        result.addAll(activatingGroups.keySet());
        for (Long gid : memberOps.keySet()){
            if (memberOps.get(gid).getStatus().equals(TaskStatus.DOING)){
                result.add(gid);
            }
        }
        for (String ip : serverOps.keySet()){
            List<Long>groupIds = serverGroupService.findAllByIp(ip);
            result.addAll(groupIds);
        }
        result.addAll(deactivateGroupOps.keySet());

        return result;
    }

    private void preMemberOperation(Long slbId , HashMap<Long, OpsTask> memberOps) {
        try {
            Set<Long> memberGroups = memberOps.keySet();
            for (Long groupId : memberGroups){
                if (!activateService.isGroupActivated(groupId,slbId)){
                    String ips = memberOps.get(groupId).getIpList();
                    String[]iplist = ips.split(";");
                    if (memberOps.get(groupId).getUp()){
                        statusService.upMember(slbId , groupId, Arrays.asList(iplist));
                    }else {
                        statusService.downMember(slbId ,groupId, Arrays.asList(iplist));
                    }
                    memberOps.get(groupId).setStatus(TaskStatus.SUCCESS);
                    memberOps.remove(groupId);
                }
            }
        }catch (Exception e){
            logger.error("PreMemberOperation Fail!",e);
        }
    }

    private Slb getActivatingSlb(HashMap<Long, OpsTask> activateSlbOps,Long slbId) {
        if (activateSlbOps.get(slbId)!=null){
            OpsTask task = activateSlbOps.get(slbId);
            Slb slb = activateService.getActivatingSlb(slbId,task.getVersion());
            if (slb == null){
                setTaskFail(task,"Get Archive Slb Fail! SlbId:"+slbId+";Version:"+task.getVersion());
            }else {
                return slb;
            }
        }
        return null;
    }

    private HashMap<Long, Group> getActivatingGroups(HashMap<Long , OpsTask> activateGroupOps) {
        HashMap<Long , Group> result = new HashMap<>();
        Set<Long> groupIds = activateGroupOps.keySet();
        for (Long groupId : groupIds){

            OpsTask task = activateGroupOps.get(groupId);
            if (deactivateGroupOps.containsKey(groupId)){
                setTaskFail(task,"Deactivating this group at the same time!");
                continue;
            }
            Group group = activateService.getActivatingGroup(groupId,task.getVersion());
            if (group == null){
                setTaskFail(task,"Get Archive Group Fail! GroupId:"+groupId+";Version:"+task.getVersion());
            }else {
                result.put(groupId,group);
            }
        }
        return result;
    }

    private void sortTaskData(Long slbId){
        activateGroupOps.clear();
        activateSlbOps.clear();
        serverOps.clear();
        memberOps.clear();
        deactivateGroupOps.clear();
        for (OpsTask task : tasks){
            task.setStatus(TaskStatus.DOING);
            //Activate group
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_GROUP)){
                activateGroupOps.put(task.getGroupId(), task);
            }
            //activate slb
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_SLB)){
                activateSlbOps.put(task.getSlbId(), task);
            }
            // server ops
            if (task.getOpsType().equals(TaskOpsType.SERVER_OPS)){
                serverOps.put(task.getIpList(), task);
            }
            //member ops
            if (task.getOpsType().equals(TaskOpsType.MEMBER_OPS)){
                memberOps.put(task.getGroupId(),task);
            }
            if (task.getOpsType().equals(TaskOpsType.DEACTIVATE_GROUP)){
                deactivateGroupOps.put(task.getGroupId(),task);
            }
        }
    }
    private void setTaskFail(OpsTask task,String failcause){
        task.setStatus(TaskStatus.FAIL);
        task.setFailCause(failcause);
        logger.warn("[Task Fail] OpsTask: Type["+task.getOpsType()+"],FailCause:"+failcause);
    }
}
