package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.entity.VirtualServerList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/8/5.
 */
@Component
@Path("/")
public class VirtualServerResource {
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Path("/vses")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllVses")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("slbId") Long slbId,
                         @TrimmedQueryParam("domain") String domain,
                         @TrimmedQueryParam("tag") String tag,
                         @TrimmedQueryParam("pname") String pname,
                         @TrimmedQueryParam("pvalue") String pvalue) throws Exception {
        VirtualServerList vslist = new VirtualServerList();
        Set<Long> filtered = virtualServerCriteriaQuery.queryAll();
        if (slbId != null) {
            filtered.retainAll(virtualServerCriteriaQuery.queryBySlbId(slbId));
        }
        if (domain != null) {
            filtered.retainAll(virtualServerCriteriaQuery.queryByDomain(domain));
        }
        if (tag != null) {
            filtered.retainAll(tagService.query(tag, "vs"));
        }
        if (pname != null) {
            if (pvalue != null)
                filtered.retainAll(propertyService.query(pname, pvalue, "vs"));
            else
                filtered.retainAll(propertyService.query(pname, "vs"));
        }
        for (VirtualServer virtualServer : virtualServerRepository.listAll(filtered.toArray(new Long[filtered.size()]))) {
            vslist.addVirtualServer(virtualServer);
        }
        return responseHandler.handle(vslist, hh.getMediaType());
    }

    @GET
    @Path("/vs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getVs")
    public Response getVirtualServer(@Context HttpHeaders hh,
                                     @Context HttpServletRequest request,
                                     @QueryParam("vsId") Long vsId) throws Exception {
        VirtualServer vs = virtualServerRepository.getById(vsId);
        return responseHandler.handle(vs, hh.getMediaType());
    }

    @POST
    @Path("/vs/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "addVs")
    public Response addVirtualServer(@Context HttpHeaders hh,
                                     @Context HttpServletRequest request, String vs) throws Exception {
        VirtualServer virtualServer = parseVirtualServer(hh.getMediaType(), vs);
        if (virtualServer.getSlbId() == null)
            throw new ValidationException("Slb id is not provided.");
        virtualServer = virtualServerRepository.addVirtualServer(virtualServer.getSlbId(), virtualServer);
        slbRepository.updateVersion(virtualServer.getSlbId());
        return responseHandler.handle(virtualServer, hh.getMediaType());

    }

    @POST
    @Path("/vs/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "updateVs")
    public Response updateVirtualServer(@Context HttpHeaders hh,
                                        @Context HttpServletRequest request, String vs) throws Exception {
        VirtualServer virtualServer = parseVirtualServer(hh.getMediaType(), vs);
        if (virtualServer.getSlbId() == null)
            throw new ValidationException("Slb id is not provided.");
        virtualServerRepository.updateVirtualServer(virtualServer);
        slbRepository.updateVersion(virtualServer.getSlbId());
        Set<Long> groupIds = groupCriteriaQuery.queryByVsIds(new Long[]{virtualServer.getId()});
        groupRepository.updateVersion(groupIds.toArray(new Long[groupIds.size()]));
        return responseHandler.handle(virtualServer, hh.getMediaType());
    }

    @GET
    @Path("/vs/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteVs")
    public Response deleteVirtualServer(@Context HttpHeaders hh,
                                        @Context HttpServletRequest request,
                                        @QueryParam("vsId") Long vsId) throws Exception {
        if (vsId == null)
            throw new ValidationException("vsId is required.");
        Slb slb = slbRepository.getByVirtualServer(vsId);
        if (slb == null) {
            throw new ValidationException("Cannot find slb with vsId " + vsId + ".");
        }
        virtualServerRepository.deleteVirtualServer(vsId);
        slbRepository.updateVersion(slb.getId());
        return responseHandler.handle("Successfully deleted virtual server with id " + vsId + ".", hh.getMediaType());
    }

    @GET
    @Path("/vs/upgradeAll")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response upgradeAll(@Context HttpHeaders hh,
                               @Context HttpServletRequest request) throws Exception {
        List<Long> vsIds = virtualServerRepository.portVirtualServerRel();
        if (vsIds.size() == 0)
            return responseHandler.handle("Successfully ported all virtual server relations.", hh.getMediaType());
        else
            return responseHandler.handle("Error occurs when porting virtual server relations on id " + Joiner.on(',').join(vsIds) + ".", hh.getMediaType());
    }

    @GET
    @Path("/vs/upgrade")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response upgradeSingle(@Context HttpHeaders hh,
                                  @Context HttpServletRequest request,
                                  @QueryParam("vsId") Long vsId) throws Exception {
        virtualServerRepository.portVirtualServerRel(vsId);
        return responseHandler.handle("Successfully ported virtual server relations.", hh.getMediaType());
    }

    private VirtualServer parseVirtualServer(MediaType mediaType, String virtualServer) throws Exception {
        VirtualServer vs;
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            vs = DefaultSaxParser.parseEntity(VirtualServer.class, virtualServer);
        } else {
            try {
                vs = DefaultJsonParser.parse(VirtualServer.class, virtualServer);
            } catch (Exception e) {
                throw new Exception("Virtual server cannot be parsed.");
            }
        }
        return vs;
    }
}
