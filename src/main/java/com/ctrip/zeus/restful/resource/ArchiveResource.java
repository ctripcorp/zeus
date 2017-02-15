package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.restful.message.view.ArchiveList;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.support.ObjectJsonWriter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by zhoumy on 2016/5/17.
 */
@Component
@Path("/archive")
public class ArchiveResource {
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupByStatus")
    public Response getArchiveGroup(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                    @TrimmedQueryParam("groupName") String groupName,
                                    @QueryParam("groupId") Long groupId,
                                    @QueryParam("version") Integer version) throws Exception {
        if (groupId == null && groupName == null) {
            throw new ValidationException("Query parameter - groupId/groupName is required.");
        }
        Group archive = null;
        if (groupId != null) {
            archive = archiveRepository.getGroupArchive(groupId, version == null ? 0 : version);
        }
        if (groupName != null) {
            archive = archiveRepository.getGroupArchive(groupName, version == null ? 0 : version);
        }
        if (archive == null) {
            return responseHandler.handle("Group archive of id " + groupId + " cannot be found.", hh.getMediaType());
        } else {
            for (GroupVirtualServer e : archive.getGroupVirtualServers()) {
                VirtualServer v = virtualServerRepository.getById(e.getVirtualServer().getId());
                if (v != null) {
                    e.setVirtualServer(v);
                }
            }
            return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedGroup(archive), ViewConstraints.DETAIL), hh.getMediaType());
        }
    }

    @GET
    @Path("/group/list")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listArchiveGroup(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                     @QueryParam("groupId") Long groupId) throws Exception {

        if (groupId == null) {
            throw new ValidationException("Query parameter - groupId is required.");
        }
        List<Archive<Group>> list = archiveRepository.getAllGroupArchives(groupId);
        ArchiveList<Group> archiveList = new ArchiveList<>();
        archiveList.setTotal(list.size());
        archiveList.setArchives(list);
        return responseHandler.handle(archiveList, hh.getMediaType());
    }

    @GET
    @Path("/vs/list")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listArchiveVs(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                  @QueryParam("vsId") Long vsId) throws Exception {

        if (vsId == null) {
            throw new ValidationException("Query parameter - vsId is required.");
        }
        List<Archive<VirtualServer>> list = archiveRepository.getAllVsArchives(vsId);
        ArchiveList<VirtualServer> archiveList = new ArchiveList<>();
        archiveList.setTotal(list.size());
        archiveList.setArchives(list);
        return responseHandler.handle(archiveList, hh.getMediaType());
    }

    @GET
    @Path("/slb/list")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listArchiveSlb(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                   @QueryParam("slbId") Long slbId) throws Exception {

        if (slbId == null) {
            throw new ValidationException("Query parameter - slbId is required.");
        }
        List<Archive<Slb>> list = archiveRepository.getAllSlbArchives(slbId);
        ArchiveList<Slb> archiveList = new ArchiveList<>();
        archiveList.setTotal(list.size());
        archiveList.setArchives(list);
        return responseHandler.handle(archiveList, hh.getMediaType());
    }

    @GET
    @Path("/slb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getSlb")
    public Response getArchiveSlb(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                  @QueryParam("slbId") Long slbId,
                                  @QueryParam("version") Integer version) throws Exception {
        if (slbId == null) {
            throw new ValidationException("Query parameter - slbId is required.");
        }
        Slb archive = archiveRepository.getSlbArchive(slbId, version == null ? 0 : version);
        if (archive == null) {
            return responseHandler.handle("Slb archive of id " + slbId + " cannot be found.", hh.getMediaType());
        } else {
            return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedSlb(archive), ViewConstraints.DETAIL), hh.getMediaType());
        }
    }

    @GET
    @Path("/vs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getVs")
    public Response getArchiveVs(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                 @QueryParam("vsId") Long vsId,
                                 @QueryParam("version") Integer version) throws Exception {
        if (vsId == null) {
            throw new ValidationException("Query parameter - vsId is required.");
        }
        VirtualServer archive = archiveRepository.getVsArchive(vsId, version == null ? 0 : version);
        if (archive == null) {
            return responseHandler.handle("Virtual server archive of id " + vsId + " cannot be found.", hh.getMediaType());
        } else {
            return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedVs(archive), ViewConstraints.DETAIL), hh.getMediaType());
        }
    }

    @GET
    @Path("/policy")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getArchiveVs(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                 @QueryParam("policyId") Long policyId,
                                 @QueryParam("policyName") String policyName,
                                 @QueryParam("version") Integer version) throws Exception {
        if (policyId == null && policyName == null) {
            throw new ValidationException("At least one query param policyId or policyName must be provided.");
        }
        if (version == null || version <= 0) {
            TrafficPolicy archive = archiveRepository.getPolicyArchive(policyId, policyName);
            if (archive == null) {
                return responseHandler.handle("Cannot find deleted traffic policy from archives.", hh.getMediaType());
            } else {
                return responseHandler.handle(new ExtendedView.ExtendedTrafficPolicy(archive), hh.getMediaType());
            }
        } else {
            TrafficPolicy archive = trafficPolicyRepository.getByKey(new IdVersion(policyId, version));
            if (archive == null) {
                return responseHandler.handle("Cannot find traffic policy of id " + policyId + " and version " + version + ".", hh.getMediaType());
            } else {
                return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedTrafficPolicy(archive), ViewConstraints.DETAIL), hh.getMediaType());
            }
        }
    }
}
