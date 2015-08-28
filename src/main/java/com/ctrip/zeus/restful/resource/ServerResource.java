package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.entity.ServerStatus;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component
@Path("/op")
public class ServerResource {

    @Resource
    StatusService statusService;
    @Resource
    private BuildService buildService;
    @Resource
    private BuildInfoService buildInfoService;
    @Resource
    private NginxService nginxAgentService;
    @Resource
    private NginxService nginxService;
    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private NginxConfService nginxConfService;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ActivateService activateService;
    @Resource
    private TaskManager taskManager;
    @Resource
    private ActiveConfService activeConfService;


    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);


    @GET
    @Path("/upServer")
    @Authorize(name="upDownServer")
    public Response upServer(@Context HttpServletRequest request,@Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception{
        return serverOps(hh,ip,true);
    }

    @GET
    @Path("/downServer")
    @Authorize(name="upDownServer")
    public Response downServer(@Context HttpServletRequest request,@Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception{
        return serverOps(hh, ip , false);
    }

    private Response serverOps(HttpHeaders hh , String serverip , boolean up)throws Exception{
        //get slb by serverip
        List<Slb> slblist = slbRepository.listByGroupServer(serverip);
        AssertUtils.assertNotNull(slblist, "[UpServer/DownServer] Can not find slb by server ip :[" + serverip + "],Please check the configuration and server ip!");
        List<OpsTask> tasks = new ArrayList<>();
        for (Slb slb : slblist)
        {
            OpsTask task = new OpsTask();
            task.setIpList(serverip);
            task.setOpsType(TaskOpsType.SERVER_OPS);
            task.setTargetSlbId(slb.getId());
            task.setUp(up);
            tasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds,30000L);
        boolean isSuccess = true;
        String failCause = "";
        for (TaskResult taskResult : results){
            if (!taskResult.isSuccess()){
                isSuccess=false;
                failCause += taskResult.toString();
            }
        }
        if (!isSuccess){
            throw new Exception(failCause);
        }
        ServerStatus ss = new ServerStatus().setIp(serverip).setUp(statusService.getServerStatus(serverip));
        List<Group> groupList = groupRepository.listGroupsByGroupServer(serverip);

        if (groupList!=null)
        {
            for (Group group : groupList)
            {
                ss.addGroupName(group.getName());
            }
        }

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(ServerStatus.XML, ss)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(ServerStatus.JSON, ss)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/upMember")
    @Authorize(name="upDownMember")
    public Response upMember(@Context HttpServletRequest request,
                             @Context HttpHeaders hh,
                             @QueryParam("groupId") Long groupId,
                             @QueryParam("groupName") String groupName,
                             @QueryParam("ip") List<String> ips,
                             @QueryParam("batch") Boolean batch)throws Exception
    {
        Long _groupId = null;
        List<String> _ips = new ArrayList<>();
        if (groupId != null)
        {
            _groupId = groupId;
        }else if (groupName != null){
            _groupId = groupRepository.get(groupName).getId();
        }
        if (null == _groupId)
        {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (null != batch && batch.equals(true))
        {
            Group gp = groupRepository.getById(_groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers)
            {
                _ips.add(gs.getIp());
            }
        }else if (ips != null)
        {
            _ips.addAll(ips);
        }
        return memberOps(hh, _groupId, _ips , true);
    }

    @GET
    @Path("/downMember")
    @Authorize(name="upDownMember")
    public Response downMember(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("groupId") Long groupId,
                               @QueryParam("groupName") String groupName,
                               @QueryParam("ip") List<String> ips,
                               @QueryParam("batch") Boolean batch)throws Exception
    {
        Long _groupId = null;
        List<String> _ips = new ArrayList<>();

        if (groupId != null)
        {
            _groupId = groupId;
        }else if (groupName != null){
            _groupId = groupRepository.get(groupName).getId();
        }
        if (null == _groupId)
        {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (null != batch && batch.equals(true))
        {
            Group gp = groupRepository.getById(_groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers)
            {
                _ips.add(gs.getIp());
            }
        }else if (ips != null)
        {
            _ips.addAll(ips);
        }

        return memberOps(hh, _groupId, _ips,false);
    }


    private Response memberOps(HttpHeaders hh,Long groupId,List<String> ips,boolean up)throws Exception{

        StringBuilder sb = new StringBuilder();
        for (String ip : ips){
            sb.append(ip).append(";");
        }
        Set<Long> slbIds = activeConfService.getSlbIdsByGroupId(groupId);
        List<Slb> slbs = slbRepository.listByGroups(new Long[]{groupId});
        for (Slb slb : slbs){
            slbIds.add(slb.getId());
        }
        List<OpsTask>tasks = new ArrayList<>();
        for (Long slbId : slbIds){
            OpsTask task = new OpsTask();
            task.setTargetSlbId(slbId);
            task.setOpsType(TaskOpsType.MEMBER_OPS);
            task.setUp(up);
            task.setGroupId(groupId);
            task.setIpList(sb.toString());
            tasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds,30000L);
        for (TaskResult taskResult : results){
            if(!taskResult.isSuccess()){
                throw new Exception("Task Failed! Fail cause : "+taskResult.getFailCause());
            }
        }

        List<GroupStatus> statuses = groupStatusService.getGroupStatus(groupId);
        //ToDo set group name and slb name
        GroupStatus groupStatusList = new GroupStatus().setGroupId(groupId).setSlbName("");
        for (GroupStatus groupStatus : statuses)
        {
            groupStatusList.setSlbName(groupStatusList.getSlbName() + " " + groupStatus.getSlbName())
                            .setGroupName(groupStatus.getGroupName())
                            .setSlbId(groupStatus.getSlbId());
            for(GroupServerStatus b : groupStatus.getGroupServerStatuses())
            {
                groupStatusList.addGroupServerStatus(b);
            }
        }

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(GroupStatus.XML, groupStatusList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(GroupStatus.JSON, groupStatusList)).type(MediaType.APPLICATION_JSON).build();
        }
    }

}

