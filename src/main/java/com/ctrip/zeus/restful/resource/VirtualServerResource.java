package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.entity.VirtualServerList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.filter.FilterSet;
import com.ctrip.zeus.restful.filter.QueryExecuter;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.AssertUtils;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
    private CertificateService certificateService;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
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
                         @QueryParam("slbId") final Long slbId,
                         @TrimmedQueryParam("domain") final String domain,
                         @TrimmedQueryParam("tag") final String tag,
                         @TrimmedQueryParam("pname") final String pname,
                         @TrimmedQueryParam("pvalue") final String pvalue) throws Exception {
        VirtualServerList vslist = new VirtualServerList();
        QueryExecuter executer = new QueryExecuter.Builder()
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        return virtualServerCriteriaQuery.queryAll();
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (slbId != null) {
                            input.retainAll(virtualServerCriteriaQuery.queryBySlbId(slbId));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (domain != null) {
                            input.retainAll(virtualServerCriteriaQuery.queryByDomain(domain));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (tag != null) {
                            input.retainAll(tagService.query(tag, "vs"));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (pname != null) {
                            if (pvalue != null)
                                input.retainAll(propertyService.query(pname, pvalue, "vs"));
                            else
                                input.retainAll(propertyService.query(pname, "vs"));
                        }
                        return input;
                    }
                })
                .build();
        for (VirtualServer virtualServer : virtualServerRepository.listAll(executer.run())) {
            vslist.addVirtualServer(virtualServer);
        }
        vslist.setTotal(vslist.getVirtualServers().size());
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
        if (vs == null)
            throw new ValidationException("Virtual server cannot be found.");
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
        if (virtualServer.getSsl().booleanValue()) {
            installCertificate(virtualServer);
        }
        return responseHandler.handle(virtualServer, hh.getMediaType());

    }

    @POST
    @Path("/vs/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "updateVs")
    public Response updateVirtualServer(@Context HttpHeaders hh,
                                        @Context HttpServletRequest request, String vs) throws Exception {
        VirtualServer virtualServer = parseVirtualServer(hh.getMediaType(), vs);
        if (virtualServer.getId() == null || virtualServer.getId().longValue() <= 0L)
            throw new ValidationException("Invalid virtual server id.");
        if (virtualServer.getSlbId() == null)
            throw new ValidationException("Slb id is not provided.");
        Long originSlbId = slbCriteriaQuery.queryByVs(virtualServer.getId());
        virtualServerRepository.updateVirtualServer(virtualServer);
        slbRepository.updateVersion(virtualServer.getSlbId());
        if (!originSlbId.equals(virtualServer.getSlbId()))
            slbRepository.updateVersion(originSlbId);
        Set<Long> groupIds = groupCriteriaQuery.queryByVsIds(new Long[]{virtualServer.getId()});
        groupRepository.updateVersion(groupIds.toArray(new Long[groupIds.size()]));
        if (virtualServer.getSsl().booleanValue()) {
            installCertificate(virtualServer);
        }
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
        Slb slb = slbRepository.getById(slbCriteriaQuery.queryByVs(vsId));
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

    private void installCertificate(VirtualServer virtualServer) throws Exception {
        List<String> ips = new ArrayList<>();
        for (SlbServer slbServer : slbRepository.getById(virtualServer.getSlbId()).getSlbServers()) {
            ips.add(slbServer.getIp());
        }
        Long certId = certificateService.pickCertificate(virtualServer);
        certificateService.command(virtualServer.getId(), ips, certId);
        certificateService.install(virtualServer.getId());
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
