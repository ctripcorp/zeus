package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.restful.message.view.VsListView;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.support.ObjectJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

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
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private ViewDecorator viewDecorator;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @api {get} /api/vses: Request vs information
     * @apiName ListVirtualServers
     * @apiGroup Vs
     *
     *
     * @apiParam {long[]} vsId          1,2,3
     * @apiParam {string[]} vsName      a,b,c
     * @apiParam {string[]} ip          reserved
     * @apiParam {string[]} domain      a.ctrip.com,b.ctrip.com
     * @apiParam {boolean} ssl          true/false
     * @apiParam {string} mode          get {online/offline/redundant} (redundant=online&offline) version
     * @apiParam {string} type          get vses with {info/normal/detail/extended} information
     * @apiParam {string[]} anyTag      union search vses by tags e.g. anyTag=group1,group2
     * @apiParam {string[]} tags        join search vses by tags e.g. tags=group1,group2
     * @apiParam {string[]} anyProp     union search vses by properties(key:value) e.g. anyProp=dc:oy,dc:jq
     * @apiParam {string[]} props       join search vses by properties(key:value) e.g. props=department:hotel,dc:jq
     * @apiParam {any} group            supported group property queries, ref /api/groups
     * @apiParam {any} slb              supported slb property queries, ref /api/slbs
     *
     * @apiSuccess {VirtualServer[]} vses   vs list json object
     */
    @GET
    @Path("/vses")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllVses")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("type") final String type,
                         @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "vs", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        VsListView listView = new VsListView();
        for (VirtualServer vs : virtualServerRepository.listAll(searchKeys)) {
            listView.add(new ExtendedView.ExtendedVs(vs));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(listView.getList(), "vs");
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    @GET
    @Path("/vs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getVs")
    public Response getVirtualServer(@Context HttpHeaders hh,
                                     @Context HttpServletRequest request,
                                     @TrimmedQueryParam("mode") final String mode,
                                     @TrimmedQueryParam("type") final String type,
                                     @Context UriInfo uriInfo) throws Exception {
        SelectionMode selectionMode = SelectionMode.getMode(mode);
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "vs", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        if (SelectionMode.REDUNDANT == selectionMode) {
            if (searchKeys.length > 2)
                throw new ValidationException("Too many matches have been found after querying.");
        } else {
            if (searchKeys.length > 1)
                throw new ValidationException("Too many matches have been found after querying.");
        }

        VsListView listView = new VsListView();
        for (VirtualServer vs : virtualServerRepository.listAll(searchKeys)) {
            listView.add(new ExtendedView.ExtendedVs(vs));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(listView.getList(), "vs");
        }

        if (listView.getTotal() == 0) throw new ValidationException("Virtual server cannot be found.");
        if (listView.getTotal() == 1) {
            return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView.getList().get(0), type), hh.getMediaType());
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
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
