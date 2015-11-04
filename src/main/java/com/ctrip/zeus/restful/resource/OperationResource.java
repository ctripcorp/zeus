package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.dal.core.StatusGroupServerDao;
import com.ctrip.zeus.dal.core.StatusGroupServerDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateInstaller;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.entity.ServerStatus;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.ctrip.zeus.util.AssertUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component
@Path("/op")
public class OperationResource {

    @Resource
    StatusService statusService;
    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private TaskManager taskManager;
    @Resource
    private ActiveConfService activeConfService;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private StatusGroupServerDao statusGroupServerDao;
    @Resource
    private CertificateService certificateService;
    @Resource
    private CertificateInstaller certificateInstaller;

    @GET
    @Path("/clean")
    public Response clean(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        statusGroupServerDao.deleteByUp(new StatusGroupServerDo().setUp(false));
        return Response.status(200).entity("Suc").type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/upServer")
    @Authorize(name = "upDownServer")
    public Response upServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        return serverOps(hh, ip, true);
    }

    @GET
    @Path("/downServer")
    @Authorize(name = "upDownServer")
    public Response downServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        return serverOps(hh, ip, false);
    }

    private Response serverOps(HttpHeaders hh, String serverip, boolean up) throws Exception {
        //get slb by serverip
        List<Slb> slblist = slbRepository.listByGroupServer(serverip);
        AssertUtils.assertNotNull(slblist, "[UpServer/DownServer] Can not find slb by server ip :[" + serverip + "],Please check the configuration and server ip!");
        List<OpsTask> tasks = new ArrayList<>();
        for (Slb slb : slblist) {
            OpsTask task = new OpsTask();
            task.setIpList(serverip);
            task.setOpsType(TaskOpsType.SERVER_OPS);
            task.setTargetSlbId(slb.getId());
            task.setUp(up);
            tasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds, 30000L);
        boolean isSuccess = true;
        String failCause = "";
        for (TaskResult taskResult : results) {
            if (!taskResult.isSuccess()) {
                isSuccess = false;
                failCause += taskResult.toString();
            }
        }
        if (!isSuccess) {
            throw new Exception(failCause);
        }
        ServerStatus ss = new ServerStatus().setIp(serverip).setUp(statusService.getServerStatus(serverip));
        List<Group> groupList = groupRepository.listGroupsByGroupServer(serverip);

        if (groupList != null) {
            for (Group group : groupList) {
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
    @Authorize(name = "upDownMember")
    public Response upMember(@Context HttpServletRequest request,
                             @Context HttpHeaders hh,
                             @QueryParam("groupId") Long groupId,
                             @QueryParam("groupName") String groupName,
                             @QueryParam("ip") List<String> ips,
                             @QueryParam("batch") Boolean batch) throws Exception {
        Long _groupId = null;
        List<String> _ips = new ArrayList<>();
        if (groupId != null) {
            _groupId = groupId;
        } else if (groupName != null) {
            _groupId = groupCriteriaQuery.queryByName(groupName);
        }
        if (null == _groupId) {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (null != batch && batch.equals(true)) {
            Group gp = groupRepository.getById(_groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            _ips.addAll(ips);
        }
        return memberOps(hh, _groupId, _ips, true, TaskOpsType.MEMBER_OPS);
    }

    @GET
    @Path("/downMember")
    @Authorize(name = "upDownMember")
    public Response downMember(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("groupId") Long groupId,
                               @QueryParam("groupName") String groupName,
                               @QueryParam("ip") List<String> ips,
                               @QueryParam("batch") Boolean batch) throws Exception {
        Long _groupId = null;
        List<String> _ips = new ArrayList<>();

        if (groupId != null) {
            _groupId = groupId;
        } else if (groupName != null) {
            _groupId = groupCriteriaQuery.queryByName(groupName);
        }
        if (null == _groupId) {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (null != batch && batch.equals(true)) {
            Group gp = groupRepository.getById(_groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            _ips.addAll(ips);
        }

        return memberOps(hh, _groupId, _ips, false, TaskOpsType.MEMBER_OPS);
    }


    @GET
    @Path("/pullIn")
    @Authorize(name = "upDownMember")
    public Response pullIn(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @QueryParam("groupId") Long groupId,
                           @QueryParam("groupName") String groupName,
                           @QueryParam("ip") List<String> ips,
                           @QueryParam("batch") Boolean batch) throws Exception {
        Long _groupId = null;
        List<String> _ips = new ArrayList<>();
        if (groupId != null) {
            _groupId = groupId;
        } else if (groupName != null) {
            _groupId = groupCriteriaQuery.queryByName(groupName);
        }
        if (null == _groupId) {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (null != batch && batch.equals(true)) {
            Group gp = groupRepository.getById(_groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            _ips.addAll(ips);
        }
        return memberOps(hh, _groupId, _ips, true, TaskOpsType.PULL_MEMBER_OPS);
    }

    @GET
    @Path("/pullOut")
    @Authorize(name = "upDownMember")
    public Response pullOut(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("groupId") Long groupId,
                            @QueryParam("groupName") String groupName,
                            @QueryParam("ip") List<String> ips,
                            @QueryParam("batch") Boolean batch) throws Exception {
        Long _groupId = null;
        List<String> _ips = new ArrayList<>();

        if (groupId != null) {
            _groupId = groupId;
        } else if (groupName != null) {
            _groupId = groupCriteriaQuery.queryByName(groupName);
        }
        if (null == _groupId) {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (null != batch && batch.equals(true)) {
            Group gp = groupRepository.getById(_groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            _ips.addAll(ips);
        }

        return memberOps(hh, _groupId, _ips, false, TaskOpsType.PULL_MEMBER_OPS);
    }

    @POST
    @Path("/uploadcerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Authorize(name = "uploadCerts")
    public Response uploadCerts(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @FormDataParam("cert") InputStream cert,
                                @FormDataParam("key") InputStream key,
                                @QueryParam("domain") String domain,
                                @QueryParam("vsId") Long vsId,
                                @QueryParam("ip") List<String> ips) throws Exception {
        if (domain == null || domain.isEmpty()) {
            throw new ValidationException("Domain info is required.");
        }
        if (vsId == null && (ips != null || ips.size() > 0)) {
            throw new ValidationException("vsId is required when running certificate grayscale test.");
        }
        // simply upload file, init state
        if (vsId == null) {
            certificateService.upload(cert, key, domain, CertificateConfig.ONBOARD);
            return responseHandler.handle("Certificates uploaded. Virtual server creation is permitted.", hh.getMediaType());
        }
        // update certificate or run grayscale test
        Set<Long> check = virtualServerCriteriaQuery.queryByDomain(domain);
        if (!check.contains(vsId))
            throw new ValidationException("VsId and domain mismatched.");
        ips = configureIps(vsId, ips);
        Long certId = certificateService.upload(cert, key, domain, CertificateConfig.ONBOARD);
        certificateService.command(vsId, ips, certId);
        certificateService.install(vsId);
        return responseHandler.handle("Certificates uploaded. Re-activate the virtual server to take effect.", hh.getMediaType());
    }

    @POST
    @Path("/dropcerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Authorize(name = "dropcerts")
    public Response uploadCerts(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("vsId") Long vsId,
                                @QueryParam("ip") List<String> ips) throws Exception {
        if (vsId == null && (ips == null || ips.size() == 0))
            throw new ValidationException("vsId and ip addresses are required.");
        certificateService.recall(vsId, ips);
        certificateService.uninstallIfRecalled(vsId);
        return responseHandler.handle("Certificates dropped successfully. Re-activate the virtual server to take effect.", hh.getMediaType());
    }

    @POST
    @Path("/installcerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Authorize(name = "installCerts")
    public Response installCerts(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("vsId") Long vsId,
                                 @QueryParam("certId") Long certId) throws Exception {
        if (vsId == null || certId == null)
            throw new ValidationException("vsId and certId are required.");
        String domain = certificateInstaller.localInstall(vsId, certId);
        return responseHandler.handle("Certificates with domain " + domain + " are installed successfully.", hh.getMediaType());
    }

    @POST
    @Path("/uninstallcerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Authorize(name = "uninstallCerts")
    public Response uninstallCerts(@Context HttpServletRequest request,
                                   @Context HttpHeaders hh,
                                   @QueryParam("vsId") Long vsId) throws Exception {
        if (vsId == null)
            throw new ValidationException("vsId and certId are required.");
        certificateInstaller.localUninstall(vsId);
        return responseHandler.handle("Certificates for vsId " + vsId + " are uninstalled.", hh.getMediaType());
    }

    private Response memberOps(HttpHeaders hh, Long groupId, List<String> ips, boolean up, String type) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String ip : ips) {
            sb.append(ip).append(";");
        }
        Set<Long> slbIds = activeConfService.getSlbIdsByGroupId(groupId);
        Set<Long> slbs = slbCriteriaQuery.queryByGroups(new Long[]{groupId});
        slbIds.addAll(slbs);

        List<OpsTask> tasks = new ArrayList<>();
        for (Long slbId : slbIds) {
            OpsTask task = new OpsTask();
            task.setTargetSlbId(slbId);
            task.setOpsType(type);
            task.setUp(up);
            task.setGroupId(groupId);
            task.setIpList(sb.toString());
            tasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds, 30000L);
        for (TaskResult taskResult : results) {
            if (!taskResult.isSuccess()) {
                throw new Exception("Task Failed! Fail cause : " + taskResult.getFailCause());
            }
        }

        List<GroupStatus> statuses = groupStatusService.getGroupStatus(groupId);
        //ToDo set group name and slb name
        GroupStatus groupStatusList = new GroupStatus().setGroupId(groupId).setSlbName("");
        for (GroupStatus groupStatus : statuses) {
            groupStatusList.setSlbName(groupStatusList.getSlbName() + " " + groupStatus.getSlbName())
                    .setGroupName(groupStatus.getGroupName())
                    .setSlbId(groupStatus.getSlbId());
            for (GroupServerStatus b : groupStatus.getGroupServerStatuses()) {
                groupStatusList.addGroupServerStatus(b);
            }
        }

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(GroupStatus.XML, groupStatusList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(GroupStatus.JSON, groupStatusList)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    private List<String> configureIps(Long vsId, List<String> ips) throws Exception {
        Long slbId = slbCriteriaQuery.queryByVs(vsId);
        Slb slb = slbRepository.getById(slbId);
        if (slb == null) {
            throw new ValidationException("Cannot find slb servers by the given vsId.");
        }
        List<String> slbIps = new ArrayList<>();
        for (SlbServer slbServer : slb.getSlbServers()) {
            slbIps.add(slbServer.getIp());
        }
        if (ips != null && ips.size() > 0) {
            if (!slbIps.containsAll(ips)) {
                throw new ValidationException("Some ips do not belong to the current slb.");
            }
        } else {
            ips = slbIps;
        }
        return ips;
    }

}

