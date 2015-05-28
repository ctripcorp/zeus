package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.StatusService;
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
import java.util.List;

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
    private SlbRepository slbClusterRepository;
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


    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);



    @GET
    @Path("/upServer")
    @Authorize(name="upDownServer")
    public Response upServer(@Context HttpServletRequest request,@Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception{
        String serverip = ip;

        //update status
        statusService.upServer(serverip);

        return serverOps(hh,serverip);

    }

    @GET
    @Path("/downServer")
    @Authorize(name="upDownServer")
    public Response downServer(@Context HttpServletRequest request,@Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception{
        String serverip = ip;
        //update status
        statusService.downServer(serverip);
        return serverOps(hh, serverip);
    }

    private Response serverOps(HttpHeaders hh , String serverip)throws Exception{
        //get slb by serverip
        List<Slb> slblist = slbClusterRepository.listByGroupServerAndGroup(serverip,null);
        AssertUtils.isNull(slblist,"[UpServer/DownServer] Can not find slb by server ip :["+serverip+"],Please check the configuration and server ip!");

        for (Slb slb : slblist)
        {
            Long slbId = slb.getId();
            int ticket = buildInfoService.getTicket(slbId);

            boolean buildFlag = false;
            DistLock buildLock = dbLockFactory.newLock(slbId + "_build");
            try{
                buildLock.lock(lockTimeout.get());
                buildFlag =buildService.build(slbId,ticket);
            }finally {
                buildLock.unlock();
            }
            if (buildFlag) {
                DistLock writeLock = dbLockFactory.newLock(slbId + "_writeAndReload");
                try {
                    writeLock.lock(lockTimeout.get());
                    //Push Service
                    nginxAgentService.writeAllAndLoadAll(slbId);
                } finally {
                    writeLock.unlock();
                }
            }

        }

        ServerStatus ss = new ServerStatus().setIp(serverip).setUp(statusService.getServerStatus(serverip));
        List<String> applist = groupRepository.listGroupsByGroupServer(serverip);

        if (applist!=null)
        {
            for (String name : applist)
            {
                ss.addGroupName(name);
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
    public Response upMember(@Context HttpServletRequest request,@Context HttpHeaders hh, @QueryParam("appName") String appName, @QueryParam("ip") String ip)throws Exception
    {
//        statusService.upMember(appName,ip);
//        return memberOps(hh, appName, ip);
        return null;
    }

    @GET
    @Path("/downMember")
    @Authorize(name="upDownMember")
    public Response downMember(@Context HttpServletRequest request,@Context HttpHeaders hh, @QueryParam("appName") String appName, @QueryParam("ip") String ip)throws Exception
    {
//        statusService.downMember(appName, ip);
//        return memberOps(hh, appName, ip);
        return null;
    }

    @GET
    @Path("/upMember")
    @Authorize(name="upDownMember")
    public Response upMemberByGroupId(@Context HttpServletRequest request,@Context HttpHeaders hh, @QueryParam("groupId") Long groupId, @QueryParam("ip") String ip)throws Exception
    {
        statusService.upMember(groupId,ip);
        return memberOps(hh, groupId, ip);
    }

    @GET
    @Path("/downMember")
    @Authorize(name="upDownMember")
    public Response downMemberByGroupId(@Context HttpServletRequest request,@Context HttpHeaders hh, @QueryParam("groupId") Long groupId, @QueryParam("ip") String ip)throws Exception
    {
        statusService.downMember(groupId, ip);
        return memberOps(hh, groupId, ip);
    }


    private Response memberOps(HttpHeaders hh,Long groupId,String ip)throws Exception{

        //get slb by appname and ip
        List<Slb> slblist = slbClusterRepository.listByGroupServerAndGroup(ip,groupId);
        AssertUtils.isNull(slblist,"Not find slb for GroupId ["+groupId+"] and ip ["+ip+"]");

        for (Slb slb : slblist) {
            Long slbId = slb.getId();
            //get ticket
            int ticket = buildInfoService.getTicket(slbId);

            boolean buildFlag = false;
            boolean dyopsFlag = false;
            List<DyUpstreamOpsData> dyUpstreamOpsDataList = null;
            DistLock buildLock = dbLockFactory.newLock("build_"+slbId);
            try{
                buildLock.lock(lockTimeout.get());
                buildFlag =buildService.build(slbId,ticket);
            }finally {
                buildLock.unlock();
            }
            if (buildFlag) {
                DistLock writeLock = dbLockFactory.newLock("writeAndReload_" + slbId);
                try {
                    writeLock.lock(lockTimeout.get());
                    //push
                    dyopsFlag=nginxAgentService.writeALLToDisk(slbId);
                    if (!dyopsFlag)
                    {
                        throw new Exception("write all to disk failed!");
                    }
                } finally {
                    writeLock.unlock();
                }
            }
            if (dyopsFlag){
                DistLock dyopsLock = dbLockFactory.newLock(slbId + "_" + groupId + "_dyops");
                try{
                    dyopsLock.lock(lockTimeout.get());
                    dyUpstreamOpsDataList = nginxConfService.buildUpstream(slb, groupId);
                    nginxAgentService.dyops(slbId, dyUpstreamOpsDataList);
                }finally {
                    dyopsLock.unlock();
                }
            }
        }

        List<GroupStatus> statuses = groupStatusService.getGroupStatus(groupId);
        //ToDo set group name and slb name
        GroupStatus groupStatusList = new GroupStatus().setGroupId(groupId).setSlbName("");
        for (GroupStatus groupStatus : statuses)
        {
            groupStatusList.setSlbName(groupStatusList.getSlbName() + " " + groupStatus.getSlbName());
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

