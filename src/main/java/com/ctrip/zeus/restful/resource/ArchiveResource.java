package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.ArchiveRepository;
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

/**
 * Created by zhoumy on 2016/5/17.
 */
@Component
@Path("/archive")
public class ArchiveResource {
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupByStatus")
    public Response getArchiveGroup(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                    @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId == null) {
            throw new ValidationException("Query parameter - groupId is required.");
        }
        Group archive = archiveRepository.getGroupArchive(groupId);
        if (archive == null) {
            return responseHandler.handle("Group archive of id " + groupId + " cannot be found.", hh.getMediaType());
        } else {
            return responseHandler.handle(archive, hh.getMediaType());
        }
    }

    @GET
    @Path("/slb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getSlb")
    public Response getArchiveSlb(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                    @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) {
            throw new ValidationException("Query parameter - slbId is required.");
        }
        Slb archive = archiveRepository.getSlbArchive(slbId);
        if (archive == null) {
            return responseHandler.handle("Slb archive of id " + slbId + " cannot be found.", hh.getMediaType());
        } else {
            return responseHandler.handle(archive, hh.getMediaType());
        }
    }

    @GET
    @Path("/vs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getVs")
    public Response getArchiveVs(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                    @QueryParam("vsId") Long vsId) throws Exception {
        if (vsId == null) {
            throw new ValidationException("Query parameter - vsId is required.");
        }
        VirtualServer archive = archiveRepository.getVsArchive(vsId);
        if (archive == null) {
            return responseHandler.handle("Virtual server archive of id " + vsId + " cannot be found.", hh.getMediaType());
        } else {
            return responseHandler.handle(archive, hh.getMediaType());
        }
    }
}
