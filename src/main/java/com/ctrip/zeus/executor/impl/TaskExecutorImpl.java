package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.status.entity.UpdateStatusItem;
import com.ctrip.zeus.task.entity.OpsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

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
    private BuildInfoService buildInfoService;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private HashMap<String , OpsTask> serverOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateVsOps = new HashMap<>();
    private HashMap<Long , OpsTask> deactivateGroupOps = new HashMap<>();
    private HashMap<Long , OpsTask> deactivateVsOps = new HashMap<>();
    private HashMap<Long , OpsTask> activateSlbOps = new HashMap<>();
    private HashMap<Long , List<OpsTask>> memberOps = new HashMap<>();
    private HashMap<Long , List<OpsTask>> pullMemberOps = new HashMap<>();

    private List<OpsTask> tasks = null;

    @Override
    public void execute(Long slbId) {
        DistLock buildLock = null;
        List<DistLock> resLocks = new ArrayList<>();
        boolean lockflag = false;
        try {
            buildLock = dbLockFactory.newLock("TaskWorker_" + slbId);
            if (lockflag = buildLock.tryLock()) {
                fetchTask(slbId);
                List<Long> resources = getResources();
                for (Long res : resources){
                    DistLock resLock = dbLockFactory.newLock("TaskRes_"+res);
                    if (resLock.tryLock()){
                        resLocks.add(resLock);
                    }else {
                        throw new Exception("Get Resources Failed! ResourceId : " + res);
                    }
                }
                executeJob(slbId);
            } else {
                logger.warn("TaskWorker get lock failed! TaskWorker:" + slbId);
            }
        } catch (Exception e) {
            logger.warn("Executor Job Failed! TaskWorker: " + slbId, e);
        } finally {
            for (DistLock lock : resLocks){
                lock.unlock();
            }
            if (lockflag) {
                buildLock.unlock();
            }
        }
    }

    private void fetchTask(Long slbId) {
        try {
            tasks = taskService.getPendingTasks(slbId);
        }catch (Exception e){
            logger.warn("Task Executor get pending tasks failed! ", e);
        }
    }

    private void executeJob(Long slbId) throws Exception{
        HashMap<Long , Group> activatingGroups ;
        HashMap<Long , VirtualServer> activatingVses;
        Map<Long , VirtualServer> activatedVses = null;
        Slb activatingSlb ;
        Map<Long,VirtualServer> buildVirtualServer ;
        Set<Long> groupList ;

        //1. get pending tasks , if size == 0 return
        if (tasks.size()==0){
            return;
        }

        try {
            sortTaskData(slbId);
            //get activating Objects
            activatingGroups = getActivatingGroups(activateGroupOps);
            activatingSlb = getActivatingSlb(activateSlbOps,slbId);
            activatingVses = getActivatingVses(activateVsOps,deactivateVsOps,slbId);

            //memberOperations of unactivated groups
            preMemberOperation(slbId,memberOps);
            deactivateVsPreCheck(slbId ,deactivateVsOps,activatingGroups,activateGroupOps);
            groupList = getInfluencedGroups(activatingGroups);


            if (activatingSlb!=null){
                activatedVses = activateService.getActivatedVirtualServerBySlb(slbId);
            }

            buildVirtualServer = buildService.getNeedBuildVirtualServers(slbId,activatingVses,activatedVses,activatingGroups,groupList);
            if ( buildVirtualServer.size()==0 && activatingSlb==null&&deactivateVsOps.size()==0 ){
                throw new Exception("Not Found Related Virtual Server.");
            }
            for (Long vsId : deactivateVsOps.keySet()){
                if(buildVirtualServer.containsKey(vsId)){
                    buildVirtualServer.remove(vsId);
                }
            }
            Map<Long,List<Group>> groupMap = buildService.getInfluencedVsGroups(slbId,activatingGroups,buildVirtualServer,deactivateGroupOps.keySet());
            Set<String> allDownServers = getAllDownServer();
            Map<Long,Group> groups = new HashMap<>();
            Map<Long,Long> groupVs = new HashMap<>();
            for (Long vs : groupMap.keySet()){
                for (Group group : groupMap.get(vs)){
                    groups.put(group.getId(),group);
                    groupVs.put(group.getId(),vs);
                }
            }
            Set<String> allUpGroupServers = getAllUpGroupServers(buildVirtualServer.keySet(),groups);
            buildService.build(slbId,activatingSlb,buildVirtualServer,deactivateVsOps.keySet(),groupMap,allDownServers,allUpGroupServers);
            List<Long> vsIds = new ArrayList<>();
            for (VirtualServer vs : buildVirtualServer.values()){
                vsIds.add(vs.getId());
            }
            Integer slbVersion = getSlbVersion(slbId);
            List<SlbServer> slbServers = nginxService.getCurrentSlbServers(slbId,slbVersion);

            boolean needReload = false;
            if (activatingSlb!=null||activatingGroups.size()>0||deactivateGroupOps.size()>0
                    ||activatingVses.size()>0 || deactivateVsOps.size()>0){
                needReload = true;
            }

            List<NginxResponse> responses = nginxService.pushConf(slbServers,slbId,slbVersion,vsIds,needReload);
            for (NginxResponse response : responses){
                if (!response.getSucceed()){
                    throw new Exception("Push config Fail.Fail Response:"+ String.format(NginxResponse.JSON,response));
                }
            }

            if (!needReload) {
                //dyups
                boolean isFail = false;
                for (Long groupId : groupList){
                    Group group = groups.get(groupId);
                    if (group==null){
                        logger.warn("[dyups-group] Not found groupId:"+groupId +"in groupMap");
                        continue;
                    }
                    List<DyUpstreamOpsData> dyUpstreamOpsDataList = buildService.buildUpstream(slbId,buildVirtualServer,allDownServers,allUpGroupServers,group);
                    List<NginxResponse> dyopsResponses = nginxService.dyops(slbId,dyUpstreamOpsDataList);
                    for (NginxResponse response : dyopsResponses){
                        if (!response.getSucceed()){
                            isFail = true;
                            break;
                        }
                    }
                    if (isFail){
                        logger.warn("[dyups failed. try to reload all]");
                        responses = nginxService.loadAll(slbId,slbVersion);
                        for (NginxResponse response : responses){
                            if (!response.getSucceed()){
                                throw new Exception("[dyups failed. try to reload all] LoadAll Fail.Fail Response:"+ String.format(NginxResponse.JSON,response));
                            }
                        }
                        break;
                    }
                }
            }
            performTasks(slbId,groupVs,activatingGroups,activatingVses);
            updateVersion(slbId);
            setTaskResult(slbId,true,null);
        }catch (Exception e){
            // failed
            StringWriter out = new StringWriter(512);
            PrintWriter printWriter = new PrintWriter(out);
            e.printStackTrace(printWriter);
            String failCause = e.getMessage()+out.getBuffer().toString();
            setTaskResult(slbId, false,failCause.length() >1024 ? failCause.substring(0,1024):failCause);
            rollBack(slbId);
            throw e;
        }

    }

    private void deactivateVsPreCheck(Long slbId, HashMap<Long, OpsTask> deactivateVsOps,HashMap<Long , Group> activatingGroups,HashMap<Long, OpsTask> activateGroupOps)throws Exception{
        Set<Long> keySet = new HashSet<>(deactivateVsOps.keySet());
        for (Long id : keySet ){
            OpsTask task = deactivateVsOps.get(id);
            if (!activateService.isVsActivated(task.getSlbVirtualServerId(),slbId)){
                setTaskFail(task,"[Deactivate Vs] Vs is unactivated!");
                deactivateVsOps.remove(id);
            }
        }
        List<Long> groups = new ArrayList<>();
        for (Group group : activatingGroups.values()){
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()){
                if (deactivateVsOps.containsKey(gvs.getVirtualServer().getId())){
                    groups.add(group.getId());
                }
            }
        }
        for (Long groupId : groups){
            setTaskFail(activateGroupOps.get(groupId),"[Vs deactivate Pre Check] Activating Group While Related Vs is deactivating!");
            activateGroupOps.remove(groupId);
        }
    }

    private HashMap<Long, VirtualServer> getActivatingVses(HashMap<Long, OpsTask> activateVsOps,HashMap<Long, OpsTask> deactivateVsOps, Long slbId) {
        HashMap<Long,VirtualServer> result = new HashMap<>();
        Set<Long> vsIds = activateVsOps.keySet();
        List<Long> tmpIds = new ArrayList<>();
        List<Integer> versions = new ArrayList<>();
        for (Long vsId : vsIds){
            OpsTask task = activateVsOps.get(vsId);
            if (deactivateVsOps.containsKey(vsId)){
                setTaskFail(task ,"Activating and Deactivating Vs at same time! VsId:"+vsId);
                continue;
            }
            tmpIds.add(vsId);
            versions.add(task.getVersion());

//            VirtualServer vs = activateService.getActivatingVirtualServer(vsId,activateVsOps.get(vsId).getVersion());
//            if ( vs != null ){
//                if ( !vs.getSlbId().equals( slbId) ){
//                    setTaskFail(activateVsOps.get(vsId), "Activating Vs Fail! Not Found Vs[" + vsId + "] In Slb: " + slbId);
//                    continue;
//                }
//                result.put(vsId,vs);
//            }
        }
        List<VirtualServer> vses = activateService.getActivatingVirtualServers(tmpIds.toArray(new Long[]{}),versions.toArray(new Integer[]{}));
        for (VirtualServer vs : vses){
            if ( !vs.getSlbId().equals( slbId) ){
                setTaskFail(activateVsOps.get(vs.getId()), "Activating Vs Fail! Not Found Vs[" + vs.getId() + "] In Slb: " + slbId);
                continue;
            }
            result.put(vs.getId(),vs);
        }
        if (result.size() != tmpIds.size()){
            for (Long vsid : tmpIds){
                if (!result.containsKey(vsid)){
                    setTaskFail(activateVsOps.get(vsid), "Activating Vs Fail! Not Found Vs[" + vsid + "] In Slb: " + slbId);
                }
            }
        }
        return result;
    }

    private void updateVersion(Long slbId) throws Exception{
        try {
            int current = buildInfoService.getCurrentTicket(slbId);
            buildInfoService.updateTicket(slbId, current + 1);
        }catch (Exception e){
            throw new Exception("Update Version Fail!",e);
        }
    }

    private void rollBack(Long slbId) {
        try {
            int current = buildInfoService.getCurrentTicket(slbId);
            buildService.rollBackConfig(slbId,current);
            if (!nginxService.rollbackAllConf(slbId,getSlbVersion(slbId))){
                logger.error("[Rollback] Rollback config on disk fail. ");
            }
            buildInfoService.resetPaddingTicket(slbId);
        }catch (Exception e){
            logger.error("RollBack Fail!",e);
        }
    }

    private void performTasks(Long slbId,Map<Long,Long> groupVs,Map<Long,Group> activatingGroups,Map<Long,VirtualServer> activatingVses) throws Exception{
        try {
            for (OpsTask task :activateSlbOps.values()){
                if (!task.getStatus().equals(TaskStatus.DOING)){
                    continue;
                }
                activateService.activeSlb(task.getSlbId(), task.getVersion());
            }
            for (OpsTask task : activateVsOps.values()){
                if (!task.getStatus().equals(TaskStatus.DOING)){
                    continue;
                }
                VirtualServer vs = activatingVses.get(task.getSlbVirtualServerId());
                activateService.activeVirtualServer(task.getSlbVirtualServerId(),vs,task.getVersion(),slbId);
            }
            for (OpsTask task : deactivateVsOps.values()){
                if (!task.getStatus().equals(TaskStatus.DOING)){
                    continue;
                }
                activateService.deactiveVirtualServer(task.getSlbVirtualServerId(), slbId);
            }
            for (OpsTask task :activateGroupOps.values()){
                if (!task.getStatus().equals(TaskStatus.DOING)){
                    continue;
                }
                Group group = activatingGroups.get(task.getGroupId());
                activateService.activeGroup(task.getGroupId(),group,task.getVersion(),groupVs.get(task.getGroupId()),slbId);
            }
            for (OpsTask task :deactivateGroupOps.values()){
                if (!task.getStatus().equals(TaskStatus.DOING)){
                    continue;
                }

                activateService.deactiveGroup(task.getGroupId(), slbId);
            }
            for (OpsTask task :serverOps.values()){
                if (!task.getStatus().equals(TaskStatus.DOING)){
                    continue;
                }
                if (task.getUp()){
                    statusService.upServer(task.getIpList());
                }else {
                    statusService.downServer(task.getIpList());
                }
            }
            List<UpdateStatusItem> memberUpdates = new ArrayList<>();
            for (List<OpsTask> taskList :memberOps.values()){
                for (OpsTask task : taskList){
                    if (!task.getStatus().equals(TaskStatus.DOING)){
                        continue;
                    }
                    String [] ips = task.getIpList().split(";");
                    List<String>ipList = Arrays.asList(ips);
                    Long vsId = groupVs.get(task.getGroupId());
                    UpdateStatusItem item = new UpdateStatusItem();
                    item.setGroupId(task.getGroupId()).setVsId(vsId).setSlbId(slbId).setOffset(StatusOffset.MEMBER_OPS).setUp(task.getUp());
                    item.getIpses().addAll(ipList);
                    memberUpdates.add(item);
//                    statusService.updateStatus(slbId,vsId,task.getGroupId(),ipList,StatusOffset.MEMBER_OPS,task.getUp());
                }
            }
            statusService.updateStatus(memberUpdates);

            List<UpdateStatusItem> pullUpdates = new ArrayList<>();
            for (List<OpsTask> taskList :pullMemberOps.values()){
                for (OpsTask task : taskList){
                    if (!task.getStatus().equals(TaskStatus.DOING)){
                        continue;
                    }
                    String [] ips = task.getIpList().split(";");
                    List<String>ipList = Arrays.asList(ips);
                    Long vsId = groupVs.get(task.getGroupId());
                    UpdateStatusItem item = new UpdateStatusItem();
                    item.setGroupId(task.getGroupId()).setVsId(vsId).setSlbId(slbId).setOffset(StatusOffset.PULL_OPS).setUp(task.getUp());
                    item.getIpses().addAll(ipList);
                    pullUpdates.add(item);
//                    statusService.updateStatus(slbId,vsId,task.getGroupId(),ipList,StatusOffset.PULL_OPS,task.getUp());
                }
            }
            statusService.updateStatus(pullUpdates);
        }catch (Exception e){
            throw new Exception("Perform Tasks Fail! TargetSlbId:"+tasks.get(0).getTargetSlbId(),e);
        }
    }

    private void setTaskResult(Long slbId,boolean isSuc,String failCause) throws Exception{
        for (OpsTask task : tasks){
            if (task.getStatus().equals(TaskStatus.DOING)){
                if (isSuc)
                {
                    task.setStatus(TaskStatus.SUCCESS);
                }else {
                    task.setStatus(TaskStatus.FAIL);
                    task.setFailCause(failCause);
                    logger.warn("TaskFail","Task:"+String.format(OpsTask.JSON,task)+"FailCause:"+failCause);
                }
            }
        }
        try {
            taskService.updateTasks(tasks);
        } catch (Exception e) {
            logger.error("Task Update Failed! TargetSlbId:"+slbId,e);
            throw new Exception("Task Update Failed! TargetSlbId:"+slbId,e);
        }
    }

    private Set<String> getAllUpGroupServers(Set<Long> vsIds,Map<Long, Group> groups) throws Exception {
//        Set<String> allUpGroupServers = statusService.findAllUpGroupServersBySlbId(slbId);
        Set<String> memberOpsUpGroupServers = statusService.fetchGroupServersByVsIdsAndStatusOffset(vsIds.toArray(new Long[]{}), StatusOffset.MEMBER_OPS, true);
        Set<Long> tmpid = memberOps.keySet();
        for (Long gid : tmpid){
            Group groupTmp = groups.get(gid);
            if (groupTmp==null){
                throw new Exception("MemberOps: Group Not Found!");
            }
            List<OpsTask> taskList = memberOps.get(gid);
            for (OpsTask opsTask : taskList)
            {
                String ipList =opsTask.getIpList();
                String[]ips = ipList.split(";");
                for (GroupVirtualServer gvs : groupTmp.getGroupVirtualServers()){
                    if (!vsIds.contains(gvs.getVirtualServer().getId())){
                        continue;
                    }
                    if (opsTask.getUp()){
                        for (String ip : ips){
                            memberOpsUpGroupServers.add(gvs.getVirtualServer().getId()+"_"+gid+"_"+ip);
                        }
                    }else {
                        for (String ip : ips){
                            memberOpsUpGroupServers.remove(gvs.getVirtualServer().getId() + "_" + gid + "_" + ip);
                        }
                    }
                }
            }
        }
        Set<String> pullMemberOpsUpGroupServers = statusService.fetchGroupServersByVsIdsAndStatusOffset(vsIds.toArray(new Long[]{}), StatusOffset.PULL_OPS, true);
        tmpid = pullMemberOps.keySet();
        for (Long gid : tmpid){
            Group groupTmp = groups.get(gid);
            if (groupTmp==null){
                throw new Exception("PullOps: Group Not Found!");
            }
            List<OpsTask> taskList = pullMemberOps.get(gid);
            for (OpsTask opsTask : taskList)
            {
                String ipList =opsTask.getIpList();
                String[]ips = ipList.split(";");
                for (GroupVirtualServer gvs : groupTmp.getGroupVirtualServers()){
                    if (!vsIds.contains(gvs.getVirtualServer().getId())){
                        continue;
                    }
                    if (opsTask.getUp()){
                        for (String ip : ips){
                            pullMemberOpsUpGroupServers.add(gvs.getVirtualServer().getId()+"_"+gid+"_"+ip);
                        }
                    }else {
                        for (String ip : ips){
                            pullMemberOpsUpGroupServers.remove(gvs.getVirtualServer().getId() + "_" + gid + "_" + ip);
                        }
                    }
                }
            }
        }
        Set<String> result = new HashSet<>();
        result.addAll(memberOpsUpGroupServers);
        result.retainAll(pullMemberOpsUpGroupServers);
        return result;
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
            if (memberOps.get(gid).get(0).getStatus().equals(TaskStatus.DOING)){
                result.add(gid);
            }
        }
        for (Long gid : pullMemberOps.keySet()){
            if (pullMemberOps.get(gid).get(0).getStatus().equals(TaskStatus.DOING)){
                result.add(gid);
            }
        }
        for (String ip : serverOps.keySet()){
            Set<Long> groupIds = statusService.findGroupIdByIp(ip);
            result.addAll(groupIds);
        }
        result.addAll(deactivateGroupOps.keySet());

        return result;
    }

    private void preMemberOperation(Long slbId , HashMap<Long, List<OpsTask>> memberOps)throws Exception{
        try {
            Set<Long> memberGroups = new HashSet<>(memberOps.keySet());
            for (Long groupId : memberGroups){
                if (!activateService.isGroupActivated(groupId,null)){
                    List<OpsTask> tasksList = memberOps.get(groupId);
                    Set<Long> vsIds = virtualServerCriteriaQuery.queryByGroupIds(new Long[]{groupId});
                    for(OpsTask opsTask : tasksList){
                        String ips = opsTask.getIpList();
                        String[]iplist = ips.split(";");
                        for (Long vsid : vsIds){
                            statusService.updateStatus(slbId ,vsid, groupId, Arrays.asList(iplist),StatusOffset.MEMBER_OPS,opsTask.getUp());
                        }
                        opsTask.setStatus(TaskStatus.SUCCESS);
                    }
                    memberOps.remove(groupId);
                }
            }
            Set<Long> pullMemberGroups = new HashSet<>(pullMemberOps.keySet());
            for (Long groupId : pullMemberGroups){
                if (!activateService.isGroupActivated(groupId,null)){
                    List<OpsTask> tasksList = pullMemberOps.get(groupId);
                    Set<Long> vsIds = virtualServerCriteriaQuery.queryByGroupIds(new Long[]{groupId});
                    for(OpsTask opsTask : tasksList){
                        String ips = opsTask.getIpList();
                        String[]iplist = ips.split(";");
                        for (Long vsid : vsIds) {
                            statusService.updateStatus(slbId,vsid, groupId, Arrays.asList(iplist), StatusOffset.PULL_OPS, opsTask.getUp());
                        }
                        opsTask.setStatus(TaskStatus.SUCCESS);
                    }
                    pullMemberOps.remove(groupId);
                }
            }
        }catch (Exception e){
            throw new Exception("PreMemberOperation Fail!",e);
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

    private Integer getSlbVersion(Long slbId){
        if (activateSlbOps.get(slbId)!=null){
            OpsTask task = activateSlbOps.get(slbId);
            return task.getVersion()>0?task.getVersion():null;
        }
        return null;
    }

    private HashMap<Long, Group> getActivatingGroups(HashMap<Long , OpsTask> activateGroupOps) {
        HashMap<Long , Group> result = new HashMap<>();
        Set<Long> groupIds = activateGroupOps.keySet();
        List<Long> gids = new ArrayList<>();
        List<Integer> versions = new ArrayList<>();
        for (Long groupId : groupIds) {

            OpsTask task = activateGroupOps.get(groupId);
            if (deactivateGroupOps.containsKey(groupId)) {
                setTaskFail(task, "Deactivating this group at the same time!");
                continue;
            }
            gids.add(groupId);
            versions.add(task.getVersion());
//            Group group = activateService.getActivatingGroup(groupId, task.getVersion());
//            if (group == null) {
//                setTaskFail(task, "Get Archive Group Fail! GroupId:" + groupId + ";Version:" + task.getVersion());
//            } else {
//                result.put(groupId, group);
//            }
        }
        List<Group> groups = activateService.getActivatingGroups(gids.toArray(new Long[]{}),versions.toArray(new Integer[]{}));
        for (Group g : groups){
            result.put(g.getId(),g);
        }
        if (result.size() != gids.size()){
            for (Long gid : gids){
                if (!result.containsKey(gid)){
                    OpsTask task = activateGroupOps.get(gid);
                    setTaskFail(task, "Get Archive Group Fail! GroupId:" + gid + ";Version:" + task.getVersion());
                }
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
        pullMemberOps.clear();
        activateVsOps.clear();
        deactivateVsOps.clear();
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
                List<OpsTask> taskList = memberOps.get(task.getGroupId());
                if (taskList==null){
                    taskList = new ArrayList<>();
                    memberOps.put(task.getGroupId(),taskList);
                }
                taskList.add(task);
            }
            //tars member ops
            if (task.getOpsType().equals(TaskOpsType.PULL_MEMBER_OPS)){
                List<OpsTask> taskList = pullMemberOps.get(task.getGroupId());
                if (taskList==null){
                    taskList = new ArrayList<>();
                    pullMemberOps.put(task.getGroupId(),taskList);
                }
                taskList.add(task);
            }
            //deactivate
            if (task.getOpsType().equals(TaskOpsType.DEACTIVATE_GROUP)){
                deactivateGroupOps.put(task.getGroupId(),task);
            }
            //deactivate vs
            if (task.getOpsType().equals(TaskOpsType.DEACTIVATE_VS)){
                deactivateVsOps.put(task.getSlbVirtualServerId(),task);
            }
            //activate vs
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_VS)){
                activateVsOps.put(task.getSlbVirtualServerId(),task);
            }
        }
    }
    private void setTaskFail(OpsTask task,String failcause){
        task.setStatus(TaskStatus.FAIL);
        task.setFailCause(failcause);
        logger.warn("[Task Fail] OpsTask: Type["+task.getOpsType()+"],FailCause:"+failcause);
    }

    public  List<Long> getResources() {
        List<Long> resources = new ArrayList<>();
        Set<Long> tmp = new HashSet<>();
        for (OpsTask task : tasks){
            if (task.getResources()!=null){
                tmp.add(Long.parseLong(task.getResources()));
            }
            tmp.add(task.getTargetSlbId());
        }
        resources.addAll(tmp);
        Collections.sort(resources);
        return resources;
    }
}
