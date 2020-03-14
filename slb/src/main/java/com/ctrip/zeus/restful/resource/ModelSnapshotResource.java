package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.ModelSnapshotService;
import com.ctrip.zeus.service.commit.CommitMergeService;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;

@Component
@Path("/model/snapshot")
public class ModelSnapshotResource {

    @Resource
    private AuthService authService;
    @Resource
    private ModelSnapshotService modelSnapshotService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private ConfVersionService confVersionService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private CommitMergeService commitMergeService;
    @Resource
    private ConfigHandler configHandler;

    @GET
    @Path("/get")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("slbId") Long slbId,
                        @QueryParam("ip") String ip,
                        @QueryParam("version") Long version) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, AuthDefaultValues.ALL);
        if (slbId == null && ip == null) {
            throw new ValidationException("Need slbId or ip Param.");
        }
        if (slbId == null) {
            Set<IdVersion> ids = slbCriteriaQuery.queryBySlbServerIp(ip);
            if (ids.size() > 0) {
                slbId = ids.iterator().next().getId();
            } else {
                throw new NotFoundException("Not Found SlbId By Ip:" + ip);
            }
        }
        String clientIP = MessageUtil.getClientIP(request);
        Slb slb = slbRepository.getById(slbId);
        AssertUtils.assertNotNull(slb, "Not Found Slb By slbId.SlbId :" + slbId);
        if (configHandler.getEnable("snapshot.incremental", slbId
                , null, null, false)) {
            Long latestFullVersion = modelSnapshotService.findLatestFullVersionBefore(slbId, version);
            if (latestFullVersion == null) {
                throw new NotFoundException("Latest full version not found. ");
            }
            List<ModelSnapshotEntity> snapshots = modelSnapshotService.get(slbId, latestFullVersion - 1, version);
            ModelSnapshotEntity merged = modelSnapshotService.merge(snapshots);
            if (slb.getSlbServers().get(0).getIp().equalsIgnoreCase(clientIP)) {
                modelSnapshotService.replaceWithCanaryGroups(merged);
                return responseHandler.handle(merged, hh.getMediaType());
            }
            return responseHandler.handle(merged, hh.getMediaType());
        }
        if (slb.getSlbServers().get(0).getIp().equalsIgnoreCase(clientIP)) {
            return responseHandler.handle(modelSnapshotService.getCanary(slbId, version), hh.getMediaType());
        }
        return responseHandler.handle(modelSnapshotService.get(slbId, version), hh.getMediaType());
    }

    @GET
    @Path("/incremental/get")
    public Response incrementalGet(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                   @QueryParam("slbId") Long slbId,
                                   @QueryParam("ip") String ip,
                                   @QueryParam("serverVersion") Long from,
                                   @QueryParam("slbVersion") Long to) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, AuthDefaultValues.ALL);

        AssertUtils.assertNotNull(from, "fromVersion is null");
        AssertUtils.assertNotNull(to, "toVersion is null");
        if (slbId == null && ip == null) {
            throw new ValidationException("Need slbId or ip Param.");
        }
        if (slbId == null) {
            Set<IdVersion> ids = slbCriteriaQuery.queryBySlbServerIp(ip);
            if (ids.size() > 0) {
                slbId = ids.iterator().next().getId();
            } else {
                throw new NotFoundException("Not Found SlbId By Ip:" + ip);
            }
        }

        String clientIP = MessageUtil.getClientIP(request);
        Slb slb = slbRepository.getById(slbId);
        AssertUtils.assertNotNull(slb, "Not Found Slb By slbId.SlbId :" + slbId);

        List<ModelSnapshotEntity> snapshots = modelSnapshotService.get(slbId, from, to);
        ModelSnapshotEntity merged = modelSnapshotService.merge(snapshots);

        if (slb.getSlbServers().get(0).getIp().equalsIgnoreCase(clientIP)) {
            modelSnapshotService.replaceWithCanaryGroups(merged);
        }
        return responseHandler.handle(merged, hh.getMediaType());
    }

    @GET
    @Path("/commit/query")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("slbId") Long slbId,
                        @QueryParam("ip") String ip,
                        @QueryParam("fromVersion") Long from,
                        @QueryParam("toVersion") Long to) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, AuthDefaultValues.ALL);

        AssertUtils.assertNotNull(from, "fromVersion is null");
        AssertUtils.assertNotNull(to, "toVersion is null");
        if (slbId == null && ip == null) {
            throw new ValidationException("Need slbId or ip Param.");
        }
        if (slbId == null) {
            Set<IdVersion> ids = slbCriteriaQuery.queryBySlbServerIp(ip);
            if (ids.size() > 0) {
                slbId = ids.iterator().next().getId();
            } else {
                throw new NotFoundException("Not Found SlbId By Ip:" + ip);
            }
        }
        List<Commit> res = new ArrayList<>();
        List<ModelSnapshotEntity> entities = modelSnapshotService.get(slbId, from, to);
        entities.forEach(e -> res.add(e.getCommits()));
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/version")
    public Response getVersion(@Context HttpHeaders hh, @Context HttpServletRequest request,
                               @QueryParam("slbId") Long slbId,
                               @QueryParam("ip") String serverIp) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, AuthDefaultValues.ALL);
        AssertUtils.assertNotNull(serverIp, "Need ip param.");

        if (slbId == null) {
            Set<IdVersion> ids = slbCriteriaQuery.queryBySlbServerIp(serverIp);
            if (ids.size() > 0) {
                slbId = ids.iterator().next().getId();
            } else {
                throw new NotFoundException("Not Found SlbId By Ip:" + serverIp);
            }
        }
        Long slbVersion = confVersionService.getSlbCurrentVersion(slbId);
        Long slbServerVersion = confVersionService.getSlbServerCurrentVersion(slbId, serverIp);

        if (slbServerVersion == null) {
            slbServerVersion = 0L;
        }
        if (slbVersion == null) {
            slbVersion = 0L;
        }
        Map<String, Long> result = new HashMap<>();
        result.put("slb-version", slbVersion);
        result.put("slb-server-version", slbServerVersion);
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/version/set")
    public Response setVersion(@Context HttpHeaders hh, @Context HttpServletRequest request,
                               @QueryParam("slbId") Long slbId,
                               @QueryParam("version") Long version,
                               @QueryParam("ip") String serverIp) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, AuthDefaultValues.ALL);
        AssertUtils.assertNotNull(serverIp, "ip is needed.");
        AssertUtils.assertNotNull(version, "version is needed.");

        if (slbId == null) {
            Set<IdVersion> ids = slbCriteriaQuery.queryBySlbServerIp(serverIp);
            if (ids.size() > 0) {
                slbId = ids.iterator().next().getId();
            } else {
                throw new NotFoundException("Not Found SlbId By Ip:" + serverIp);
            }
        }

        confVersionService.updateSlbServerCurrentVersion(slbId, serverIp, version);
        return responseHandler.handle("success", hh.getMediaType());
    }

}
