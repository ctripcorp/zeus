package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.SlbListView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.*;
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
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/")
public class SlbResource {
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private ViewDecorator viewDecorator;

    private final int TIMEOUT = 1000;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllSlbs")
    public Response list(@Context final HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("type") final String type,
                         @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "slb", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        SlbListView listView = new SlbListView();
        for (Slb slb : slbRepository.list(searchKeys)) {
            listView.add(new ExtendedView.ExtendedSlb(slb));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(listView.getList(), "slb");
        }

        return responseHandler.handle(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    @GET
    @Path("/slb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getSlb")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @TrimmedQueryParam("type") final String type,
                        @TrimmedQueryParam("mode") final String mode,
                        @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "slb", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        SlbListView listView = new SlbListView();
        for (Slb slb : slbRepository.list(searchKeys)) {
            listView.add(new ExtendedView.ExtendedSlb(slb));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(listView.getList(), "slb");
        }

        if (listView.getTotal() == 0) throw new ValidationException("Slb cannot be found.");
        if (listView.getTotal() == 1) {
            return responseHandler.handle(ObjectJsonWriter.write(listView.getList().get(0), type), hh.getMediaType());
        }

        return responseHandler.handle(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    @POST
    @Path("/slb/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addSlb")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String slb) throws Exception {
        Slb s = slbRepository.add(parseSlb(hh.getMediaType(), slb));
        return responseHandler.handle(s, hh.getMediaType());
    }

    @POST
    @Path("/slb/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateSlb")
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String slb) throws Exception {
        Slb s = parseSlb(hh.getMediaType(), slb);
        DistLock lock = dbLockFactory.newLock(s.getName() + "_updateSlb");
        lock.lock(TIMEOUT);
        try {
            s = slbRepository.update(s);
        } finally {
            lock.unlock();
        }
        return responseHandler.handle(s, hh.getMediaType());
    }

    @GET
    @Path("/slb/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteSlb")
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) {
            throw new Exception("Query param - slbId is required.");
        }
        Slb archive = slbRepository.getById(slbId);
        if (archive == null) throw new ValidationException("Slb cannot be found with id " + slbId + ".");

        int count = slbRepository.delete(slbId);
        try {
            archiveRepository.archiveSlb(archive);
        } catch (Exception ex) {
            logger.warn("Try archive deleted slb failed. " + GenericSerializer.writeJson(archive, false), ex);
        }
        String message = count == 1 ? "Delete slb successfully." : "No deletion is needed.";
        return responseHandler.handle(message, hh.getMediaType());
    }

    private Slb parseSlb(MediaType mediaType, String slb) throws Exception {
        Slb s;
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            s = DefaultSaxParser.parseEntity(Slb.class, slb);
        } else {
            try {
                s = DefaultJsonParser.parse(Slb.class, slb);
            } catch (Exception e) {
                throw new Exception("Slb cannot be parsed.");
            }
        }
        s.setName(trimIfNotNull(s.getName()));
        for (VirtualServer virtualServer : s.getVirtualServers()) {
            virtualServer.setName(trimIfNotNull(virtualServer.getName()));
            for (Domain domain : virtualServer.getDomains()) {
                domain.setName(trimIfNotNull(domain.getName()));
            }
        }
        for (SlbServer slbServer : s.getSlbServers()) {
            slbServer.setIp(trimIfNotNull(slbServer.getIp()));
            slbServer.setHostName(trimIfNotNull(slbServer.getHostName()));
        }
        return s;
    }

    private String trimIfNotNull(String value) {
        return value != null ? value.trim() : value;
    }
}
