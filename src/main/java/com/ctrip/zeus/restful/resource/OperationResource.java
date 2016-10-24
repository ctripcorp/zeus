package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.message.queue.MessageType;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateInstaller;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.entity.ServerStatus;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.ctrip.zeus.util.MessageUtil;
import com.google.common.base.Joiner;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicLongProperty;
import com.google.common.collect.Sets;
import com.netflix.config.DynamicPropertyFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private MessageQueue messageQueue;


    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 15000L);
    private static DynamicBooleanProperty healthyOpsActivate = DynamicPropertyFactory.getInstance().getBooleanProperty("healthy.operation.active", false);

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/upServer")
    @Authorize(name = "upDownServer")
    public Response upServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        return serverOps(request, hh, ip, true);
    }

    @GET
    @Path("/downServer")
    @Authorize(name = "upDownServer")
    public Response downServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        return serverOps(request, hh, ip, false);
    }

    private Response serverOps(HttpServletRequest request, HttpHeaders hh, String serverip, boolean up) throws Exception {
        Long[] groupIds = entityFactory.getGroupIdsByGroupServerIp(serverip, SelectionMode.REDUNDANT);

        if (groupIds == null || groupIds.length == 0) {
            throw new ValidationException("Not found Server Ip.");
        }
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(groupIds);
        Set<Long> vsIds = new HashSet<>();
        for (Long id : groupIds) {
            if (groupMap.getOnlineMapping().get(id) != null) {
                Group group = groupMap.getOnlineMapping().get(id);
                for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                    vsIds.add(gvs.getVirtualServer().getId());
                }
            } else if (groupMap.getOfflineMapping().get(id) != null) {
                Group group = groupMap.getOfflineMapping().get(id);
                for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                    vsIds.add(gvs.getVirtualServer().getId());
                }
            }
        }
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
        Set<Long> slbIds = new HashSet<>();
        for (VirtualServer vs : vsMap.getOnlineMapping().values()) {
            slbIds.addAll(vs.getSlbIds());
        }
        for (VirtualServer vs : vsMap.getOfflineMapping().values()) {
            slbIds.addAll(vs.getSlbIds());
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
        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());
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

        Long[] gids = entityFactory.getGroupIdsByGroupServerIp(serverip, SelectionMode.ONLINE_FIRST);

        Set<Long> groupIdSet = new HashSet<>();
        groupIdSet.addAll(Arrays.asList(gids));
        List<GroupStatus> statuses = groupStatusService.getOfflineGroupsStatus(groupIdSet);

        for (GroupStatus gs : statuses) {
            addHealthyProperty(gs);
        }


        List<Group> groups = groupRepository.list(gids);

        if (groups != null) {
            for (Group group : groups) {
                ss.addGroupName(group.getName());
            }
        }
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), null, serverip);
        } else {
            messageQueue.produceMessage(MessageType.OpsServer, null, serverip);
        }

        return responseHandler.handle(ss, hh.getMediaType());
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
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }

        Group gp = groupRepository.getById(groupId);
        if (null != batch && batch.equals(true)) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                if (ips.contains(gs.getIp())) {
                    _ips.add(gs.getIp());
                }
            }
            if (!_ips.containsAll(ips)) {
                IdVersion[] key = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.ONLINE_EXCLUSIVE);
                if (key.length != 0) {
                    Group online = groupRepository.getByKey(key[0]);
                    if (online != null && online.getGroupServers() != null) {
                        for (GroupServer gs : online.getGroupServers()) {
                            if (ips.contains(gs.getIp())) {
                                _ips.add(gs.getIp());
                            }
                        }
                    }
                }
            }
        }
        return memberOps(request, hh, groupId, _ips, true, TaskOpsType.MEMBER_OPS);
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
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }

        Group gp = groupRepository.getById(groupId);
        if (null != batch && batch.equals(true)) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                if (ips.contains(gs.getIp())) {
                    _ips.add(gs.getIp());
                }
            }
            if (!_ips.containsAll(ips)) {
                IdVersion[] key = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.ONLINE_EXCLUSIVE);
                if (key.length != 0) {
                    Group online = groupRepository.getByKey(key[0]);
                    if (online != null && online.getGroupServers() != null) {
                        for (GroupServer gs : online.getGroupServers()) {
                            if (ips.contains(gs.getIp())) {
                                _ips.add(gs.getIp());
                            }
                        }
                    }
                }
            }
        }

        return memberOps(request, hh, groupId, _ips, false, TaskOpsType.MEMBER_OPS);
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
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        Group gp = groupRepository.getById(groupId);
        if (null != batch && batch.equals(true)) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                if (ips.contains(gs.getIp())) {
                    _ips.add(gs.getIp());
                }
            }
            if (!_ips.containsAll(ips)) {
                IdVersion[] key = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.ONLINE_EXCLUSIVE);
                if (key.length != 0) {
                    Group online = groupRepository.getByKey(key[0]);
                    if (online != null && online.getGroupServers() != null) {
                        for (GroupServer gs : online.getGroupServers()) {
                            if (ips.contains(gs.getIp())) {
                                _ips.add(gs.getIp());
                            }
                        }
                    }
                }
            }
        }
        return memberOps(request, hh, groupId, _ips, true, TaskOpsType.PULL_MEMBER_OPS);
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
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        Group gp = groupRepository.getById(groupId);
        if (null != batch && batch.equals(true)) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                if (ips.contains(gs.getIp())) {
                    _ips.add(gs.getIp());
                }
            }
            if (!_ips.containsAll(ips)) {
                IdVersion[] key = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.ONLINE_EXCLUSIVE);
                if (key.length != 0) {
                    Group online = groupRepository.getByKey(key[0]);
                    if (online != null && online.getGroupServers() != null) {
                        for (GroupServer gs : online.getGroupServers()) {
                            if (ips.contains(gs.getIp())) {
                                _ips.add(gs.getIp());
                            }
                        }
                    }
                }
            }
        }
        return memberOps(request, hh, groupId, _ips, false, TaskOpsType.PULL_MEMBER_OPS);
    }

    @GET
    @Path("/raise")
    @Authorize(name = "upDownMember")
    public Response raise(@Context HttpServletRequest request,
                          @Context HttpHeaders hh,
                          @QueryParam("groupId") Long groupId,
                          @QueryParam("groupName") String groupName,
                          @QueryParam("ip") List<String> ips,
                          @QueryParam("batch") Boolean batch) throws Exception {
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        Group gp = groupRepository.getById(groupId);
        if (gp == null) {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (null != batch && batch.equals(true)) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                if (ips.contains(gs.getIp())) {
                    _ips.add(gs.getIp());
                }
            }
            if (!_ips.containsAll(ips)) {
                IdVersion[] key = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.ONLINE_EXCLUSIVE);
                if (key.length != 0) {
                    Group online = groupRepository.getByKey(key[0]);
                    if (online != null && online.getGroupServers() != null) {
                        for (GroupServer gs : online.getGroupServers()) {
                            if (ips.contains(gs.getIp())) {
                                _ips.add(gs.getIp());
                            }
                        }
                    }
                }
            }
        }
        if (_ips.size() == 0) {
            throw new ValidationException("Not found ip in group.GroupId:" + groupId + " ip:" + ips.toString());
        }

        if (healthyOpsActivate.get()) {
            return memberOps(request, hh, groupId, _ips, true, TaskOpsType.HEALTHY_OPS);
        } else {
            return healthyOps(hh, groupId, _ips, true);
        }
    }

    @GET
    @Path("/fall")
    @Authorize(name = "upDownMember")
    public Response fall(@Context HttpServletRequest request,
                         @Context HttpHeaders hh,
                         @QueryParam("groupId") Long groupId,
                         @QueryParam("groupName") String groupName,
                         @QueryParam("ip") List<String> ips,
                         @QueryParam("batch") Boolean batch) throws Exception {
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        Group gp = groupRepository.getById(groupId);
        if (gp == null) {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (null != batch && batch.equals(true)) {

            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                _ips.add(gs.getIp());
            }
        } else if (ips != null) {
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                if (ips.contains(gs.getIp())) {
                    _ips.add(gs.getIp());
                }
            }
            if (!_ips.containsAll(ips)) {
                IdVersion[] key = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.ONLINE_EXCLUSIVE);
                if (key.length != 0) {
                    Group online = groupRepository.getByKey(key[0]);
                    if (online != null && online.getGroupServers() != null) {
                        for (GroupServer gs : online.getGroupServers()) {
                            if (ips.contains(gs.getIp())) {
                                _ips.add(gs.getIp());
                            }
                        }
                    }
                }
            }
        }
        if (_ips.size() == 0) {
            throw new ValidationException("Not found ip in group.GroupId:" + groupId + " ip:" + ips.toString());
        }
        if (healthyOpsActivate.get()) {
            return memberOps(request, hh, groupId, _ips, false, TaskOpsType.HEALTHY_OPS);
        } else {
            return healthyOps(hh, groupId, _ips, false);
        }
    }

    private Response healthyOps(HttpHeaders hh, Long groupId, List<String> ips, boolean b) throws Exception {
        statusService.updateStatus(groupId, ips, StatusOffset.HEALTHY, b);
        return responseHandler.handle(groupStatusService.getOfflineGroupStatus(groupId), hh.getMediaType());
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
        IdVersion[] check = virtualServerCriteriaQuery.queryByIdAndMode(vsId, SelectionMode.REDUNDANT);
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
        IdVersion[] keys = virtualServerCriteriaQuery.queryByIdAndMode(vsId, SelectionMode.REDUNDANT);
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
    @Path("/cert/batchInstall")
    public Response batchInstall(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) {
            throw new ValidationException("slbId is required.");
        }
        certificateInstaller.localBatchInstall(slbId);
        return responseHandler.handle("Certificates are installed successfully.", hh.getMediaType());
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

    private Response memberOps(HttpServletRequest request, HttpHeaders hh, Long groupId, List<String> ips, boolean up, String type) throws Exception {
        Map<String, List<Boolean>> status = statusService.fetchGroupServerStatus(new Long[]{groupId});
        boolean skipOps = true;
        for (String ip : ips) {
            int index = 0;
            if (type.equals(TaskOpsType.HEALTHY_OPS)) index = StatusOffset.HEALTHY;
            if (type.equals(TaskOpsType.PULL_MEMBER_OPS)) index = StatusOffset.PULL_OPS;
            if (type.equals(TaskOpsType.MEMBER_OPS)) index = StatusOffset.MEMBER_OPS;
            boolean preStatus = status.get(groupId.toString() + "_" + ip).get(index);
            if (preStatus != up) {
                skipOps = false;
            }
        }
        if (skipOps) {
            GroupStatus groupStatus = groupStatusService.getOfflineGroupStatus(groupId);
            logger.info("Group status equals the desired value.Do not need execute task.GroupId:" + groupId + " ips:"
                    + ips.toString() + " up:" + up + " type:" + type);
            return responseHandler.handle(groupStatus, hh.getMediaType());
        }
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
            slbIds.addAll(tmp.getSlbIds());
        }
        //TODO flag for Healthy ops
        if (type.equals(TaskOpsType.HEALTHY_OPS)) {
            for (Long slbId : slbIds) {
                if (!configHandler.getEnable("healthy.operation.active", slbId, null, null, false)) {
                    logger.info("healthy.operation.active is false. slbId:" + slbId);
                    return healthyOps(hh, groupId, ips, up);
                }
            }
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
        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());
        for (TaskResult taskResult : results) {
            if (!taskResult.isSuccess()) {
                throw new Exception("Task Failed! Fail cause : " + taskResult.getFailCause());
            }
        }
        GroupStatus groupStatus = groupStatusService.getOfflineGroupStatus(groupId);
        addHealthyProperty(groupStatus);

        String slbMessageData = MessageUtil.getMessageData(request, new Group[]{offlineGroup}, null, null, ips.toArray(new String[ips.size()]), true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), groupId, slbMessageData);
        } else {
            if (type.equals(TaskOpsType.HEALTHY_OPS)) {
                messageQueue.produceMessage(MessageType.OpsHealthy, groupId, slbMessageData);
            } else if (type.equals(TaskOpsType.PULL_MEMBER_OPS)) {
                messageQueue.produceMessage(MessageType.OpsPull, groupId, slbMessageData);
            } else if (type.equals(TaskOpsType.MEMBER_OPS)) {
                messageQueue.produceMessage(MessageType.OpsMember, groupId, slbMessageData);
            }
        }


        return responseHandler.handle(groupStatus, hh.getMediaType());
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

    private void addHealthyProperty(GroupStatus gs) throws Exception {
        boolean health = true;
        boolean unhealth = true;
        for (GroupServerStatus gss : gs.getGroupServerStatuses()) {
            if (gss.getServer() && gss.getHealthy() && gss.getPull() && gss.getMember()) {
                unhealth = false;
            } else {
                health = false;
            }
        }
        if (health) {
            propertyBox.set("healthy", "health", "group", gs.getGroupId());
        } else if (unhealth) {
            propertyBox.set("healthy", "unhealth", "group", gs.getGroupId());
        } else {
            propertyBox.set("healthy", "sub-health", "group", gs.getGroupId());
        }
    }

    @GET
    @Path("/health/fillData")
    public Response healthFillData(@Context HttpServletRequest request,
                                   @Context HttpHeaders hh) throws Exception {
        List<GroupStatus> list = groupStatusService.getAllOfflineGroupsStatus();
        for (GroupStatus gs : list) {
            addHealthyProperty(gs);
        }
        return responseHandler.handle("Fill Data Success.", hh.getMediaType());
    }

}

