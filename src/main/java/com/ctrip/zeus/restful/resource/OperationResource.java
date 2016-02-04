package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.dal.core.StatusGroupServerDao;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.*;
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
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
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
import java.util.*;

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
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private CertificateService certificateService;
    @Resource
    private CertificateInstaller certificateInstaller;
    @Resource
    private EntityFactory entityFactory;


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
        Long [] slbIds = entityFactory.getSlbIdsByIp(serverip, ModelMode.MODEL_MODE_ONLINE);

        if (slbIds == null || slbIds.length == 0 ){
            throw new ValidationException("Not found Server Ip.");
        }
        List<OpsTask> tasks = new ArrayList<>();
        for (Long slbId : slbIds) {
            OpsTask task = new OpsTask();
            task.setIpList(serverip);
            task.setOpsType(TaskOpsType.SERVER_OPS);
            task.setTargetSlbId(slbId);
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

        Set<Long> groupIds = groupCriteriaQuery.queryByGroupServerIp(serverip);

        List<Group> groups = groupRepository.list(groupIds.toArray(new Long[]{}));

        if (groups != null) {
            for (Group group : groups) {
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
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null){
                throw new ValidationException("Group Id or Name not found!");
            }else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }

        if (null != batch && batch.equals(true)) {
            Group gp = groupRepository.getById(groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            _ips = ips;
        }
        return memberOps(hh, groupId, _ips, true, TaskOpsType.MEMBER_OPS);
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
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null){
                throw new ValidationException("Group Id or Name not found!");
            }else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }

        if (null != batch && batch.equals(true)) {
            Group gp = groupRepository.getById(groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            _ips = ips;
        }

        return memberOps(hh, groupId, _ips, false, TaskOpsType.MEMBER_OPS);
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
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null){
                throw new ValidationException("Group Id or Name not found!");
            }else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }

        if (null != batch && batch.equals(true)) {
            Group gp = groupRepository.getById(groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            _ips = ips;
        }
        return memberOps(hh, groupId, _ips, true, TaskOpsType.PULL_MEMBER_OPS);
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
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null){
                throw new ValidationException("Group Id or Name not found!");
            }else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }

        if (null != batch && batch.equals(true)) {
            Group gp = groupRepository.getById(groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            _ips = ips;
        }
        return memberOps(hh, groupId, _ips, false, TaskOpsType.PULL_MEMBER_OPS);
    }

    @POST
    @Path("/uploadcerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Authorize(name = "uploadCerts")
    public Response uploadCerts(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @FormDataParam("cert") InputStream cert,
                                @FormDataParam("key") InputStream key,
                                @QueryParam("domain") String domain) throws Exception {
        if (domain == null || domain.isEmpty()) {
            throw new ValidationException("Domain info is required.");
        }
        String[] domainMembers = domain.split("\\|");
        Arrays.sort(domainMembers);
        domain = Joiner.on("|").join(domainMembers);

        certificateService.upload(cert, key, domain, CertificateConfig.ONBOARD);
        return responseHandler.handle("Certificates uploaded. Virtual server creation is permitted.", hh.getMediaType());
    }

    @POST
    @Path("/upgradecerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Authorize(name = "upgradeCerts")
    public Response upgradeCerts(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @FormDataParam("cert") InputStream cert,
                                 @FormDataParam("key") InputStream key,
                                 @QueryParam("domain") String domain,
                                 @QueryParam("vsId") Long vsId,
                                 @QueryParam("ip") List<String> ips) throws Exception {
        if (domain == null || domain.isEmpty()) {
            throw new ValidationException("Domain info is required.");
        }
        if (vsId == null) {
            throw new ValidationException("vsId is required when updating certificate.");
        }
        // update certificate or run grayscale test
        IdVersion[] check = virtualServerCriteriaQuery.queryByIdAndMode(vsId, ModelMode.MODEL_MODE_REDUNDANT);
        Set<IdVersion> keys = virtualServerCriteriaQuery.queryByDomain(domain);
        keys.retainAll(Sets.newHashSet(check));
        if (keys.size() == 0) {
            throw new ValidationException("VsId and domain mismatched.");
        }
        configureIps(keys.toArray(new IdVersion[keys.size()]), ips);

        String[] domainMembers = domain.split("\\|");
        Arrays.sort(domainMembers);
        domain = Joiner.on("|").join(domainMembers);
        Long certId = certificateService.upgrade(cert, key, domain, CertificateConfig.ONBOARD);
        return responseHandler.handle("Certificate uploaded. New cert-id is " + certId + ". Contact slb team with the given cert-id to install the new certificate.", hh.getMediaType());
//        certificateService.command(vsId, ips, certId);
//        certificateService.install(vsId);
//        return responseHandler.handle("Certificates uploaded. Re-activate the virtual server to take effect.", hh.getMediaType());
    }

    @GET
    @Path("/dropcerts")
    @Authorize(name = "dropCerts")
    public Response dropCerts(@Context HttpServletRequest request,
                              @Context HttpHeaders hh,
                              @QueryParam("vsId") Long vsId,
                              @QueryParam("ip") List<String> ips) throws Exception {
        return responseHandler.handle("dropcerts is not available at the moment.", hh.getMediaType());
//        if (vsId == null && (ips == null || ips.size() == 0))
//            throw new ValidationException("vsId and ip addresses are required.");
//        certificateService.recall(vsId, ips);
//        certificateService.uninstallIfRecalled(vsId);
//        return responseHandler.handle("Certificates dropped successfully. Re-activate the virtual server to take effect.", hh.getMediaType());
    }

    @GET
    @Path("/cert/remoteInstall")
    @Authorize(name = "remoteInstallCerts")
    public Response remoteInstall(@Context HttpServletRequest request,
                                  @Context HttpHeaders hh,
                                  @QueryParam("certId") Long certId,
                                  @QueryParam("vsId") Long vsId,
                                  @QueryParam("ips") List<String> ips) throws Exception {
        if (certId == null || ips == null || vsId == null) {
            throw new ValidationException("certId, vsId and ips are required.");
        }
        IdVersion[] keys = virtualServerCriteriaQuery.queryByIdAndMode(vsId, ModelMode.MODEL_MODE_REDUNDANT);
        ips = configureIps(keys, ips);
        certificateService.install(vsId, ips, certId);
        return responseHandler.handle("Certificates uploaded. Re-activate the virtual server to take effect.", hh.getMediaType());
    }

    @GET
    @Path("/installcerts")
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

    @GET
    @Path("/uninstallcerts")
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
        ModelStatusMapping<Group> mapping = entityFactory.getGroupsByIds(new Long[]{groupId});
        if (mapping.getOfflineMapping() == null || mapping.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Group By Id.");
        }
        Group onlineGroup = mapping.getOnlineMapping().get(groupId);
        Group offlineGroup = mapping.getOfflineMapping().get(groupId);
        Set<Long> vsIds = new HashSet<>();
        Set<Long> slbIds = new HashSet<>();
        if (onlineGroup != null) {
            for (GroupVirtualServer gvs : onlineGroup.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }
        }
        for (GroupVirtualServer gvs : offlineGroup.getGroupVirtualServers()) {
            vsIds.add(gvs.getVirtualServer().getId());
        }

        ModelStatusMapping<VirtualServer> vsMaping = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));

        VirtualServer tmp;
        for (Long vsId : vsIds) {
            tmp = vsMaping.getOnlineMapping().get(vsId);
            if (tmp == null) {
                tmp = vsMaping.getOfflineMapping().get(vsId);
            }
            slbIds.add(tmp.getSlbId());
        }

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

        List<GroupStatus> statuses = groupStatusService.getOfflineGroupStatus(groupId);
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

    private List<String> configureIps(IdVersion[] keys, List<String> ips) throws Exception {
        Set<Long> slbId = slbCriteriaQuery.queryByVses(keys);
        ModelStatusMapping<Slb> check = entityFactory.getSlbsByIds(slbId.toArray(new Long[slbId.size()]));
        if (check.getOfflineMapping().size() == 0 && check.getOnlineMapping().size() == 0) {
            throw new ValidationException("Cannot find slb servers by the given vsId.");
        }
        Set<String> slbIps = new HashSet<>();
        for (Slb slb : check.getOfflineMapping().values()) {
            for (SlbServer server : slb.getSlbServers()) {
                slbIps.add(server.getIp());
            }
        }
        for (Slb slb : check.getOnlineMapping().values()) {
            for (SlbServer slbServer : slb.getSlbServers()) {
                slbIps.add(slbServer.getIp());
            }
        }
        if (ips != null && ips.size() > 0) {
            if (!slbIps.containsAll(ips)) {
                throw new ValidationException("Some ips do not belong to the current slb.");
            }
        } else {
            ips = new ArrayList<>(slbIps);
        }
        return ips;
    }

}

