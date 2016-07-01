package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.entity.VirtualServerList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.filter.FilterSet;
import com.ctrip.zeus.restful.filter.QueryExecuter;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.google.common.base.Joiner;
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
import java.util.ArrayList;
import java.util.HashSet;
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
    private ArchiveRepository archiveRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;
    @Resource
    private ResponseHandler responseHandler;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/vses")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllVses")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("slbId") final Long slbId,
                         @TrimmedQueryParam("domain") final String domain,
                         @TrimmedQueryParam("tag") final List<String> tags,
                         @TrimmedQueryParam("pname") final String pname,
                         @TrimmedQueryParam("pvalue") final String pvalue,
                         @TrimmedQueryParam("mode") final String mode) throws Exception {
        final SelectionMode selectionMode = SelectionMode.getMode(mode);

        final IdVersion[] vsFilter = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbId != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return virtualServerCriteriaQuery.queryBySlbId(slbId);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return domain != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return virtualServerCriteriaQuery.queryByDomain(domain);
                    }
                }).build(IdVersion.class).run();

        final Long[] vsIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsFilter != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (IdVersion e : vsFilter) {
                            result.add(e.getId());
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tags != null && tags.size() > 0;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return new HashSet<>(tagService.query(tags, "vs"));
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return pname != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        if (pvalue != null)
                            return new HashSet<>(propertyService.query(pname, pvalue, "vs"));
                        else
                            return new HashSet<>(propertyService.query(pname, "vs"));
                    }
                }).build(Long.class).run(new ResultHandler<Long, Long>() {
                    @Override
                    public Long[] handle(Set<Long> result) throws Exception {
                        if (result == null) {
                            result = virtualServerCriteriaQuery.queryAll();
                        }
                        return result.toArray(new Long[result.size()]);
                    }
                });

        QueryExecuter<IdVersion> executer = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsFilter != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<IdVersion> result = new HashSet<>();
                        for (IdVersion e : vsFilter) {
                            result.add(e);
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return virtualServerCriteriaQuery.queryByIdsAndMode(vsIds, selectionMode);
                    }
                }).build(IdVersion.class);

        VirtualServerList vslist = new VirtualServerList();
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
        virtualServer = virtualServerRepository.add(virtualServer.getSlbId(), virtualServer);
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
        virtualServerRepository.update(virtualServer);
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
            throw new ValidationException("Query parameter - vsId is required.");
        VirtualServer archive = virtualServerRepository.getById(vsId);
        if (archive == null) throw new ValidationException("Virtual server cannot be found with id " + vsId + ".");

        virtualServerRepository.delete(vsId);
        try {
            archiveRepository.archiveVs(archive);
        } catch (Exception ex) {
            logger.warn("Try archive deleted vs failed. " + GenericSerializer.writeJson(archive, false), ex);
        }
        return responseHandler.handle("Successfully deleted virtual server with id " + vsId + ".", hh.getMediaType());
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
        vs.setName(trimIfNotNull(vs.getName()));
        vs.setPort(trimIfNotNull(vs.getPort()));
        for (Domain domain : vs.getDomains()) {
            domain.setName(trimIfNotNull(domain.getName()));
        }
        return vs;
    }

    private String trimIfNotNull(String value) {
        return value != null ? value.trim() : value;
    }
}
