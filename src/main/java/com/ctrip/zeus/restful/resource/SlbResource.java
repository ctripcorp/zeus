package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.filter.FilterSet;
import com.ctrip.zeus.restful.filter.QueryExecuter;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.ModelMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
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
import java.util.HashSet;
import java.util.Set;

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
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;
    private final int TIMEOUT = 1000;

    @GET
    @Path("/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllSlbs")
    public Response list(@Context final HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("type") final String type,
                         @TrimmedQueryParam("tag") final String tag,
                         @TrimmedQueryParam("pname") final String pname,
                         @TrimmedQueryParam("pvalue") final String pvalue,
                         @TrimmedQueryParam("mode") final String mode) throws Exception {
        final SlbList slbList = new SlbList();
        final ModelMode modelMode = ModelMode.getMode(mode);
        final Long[] slbIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return slbCriteriaQuery.queryAll();
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tag != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return new HashSet<>(tagService.query(tag, "slb"));
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
                            return new HashSet<>(propertyService.query(pname, pvalue, "slb"));
                        else
                            return new HashSet<>(propertyService.query(pname, "slb"));
                    }
                }).build(Long.class).run();

        QueryExecuter<IdVersion> executer = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return slbIds.length == 0 ? new HashSet<IdVersion>() : slbCriteriaQuery.queryByIdsAndMode(slbIds, modelMode);
                    }
                })
                .build(IdVersion.class);
        for (Slb slb : slbRepository.list(executer.run())) {
            slbList.addSlb(getSlbByType(slb, type));
        }
        slbList.setTotal(slbList.getSlbs().size());
        return responseHandler.handle(slbList, hh.getMediaType());
    }

    @GET
    @Path("/slb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getSlb")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("slbId") Long slbId,
                        @TrimmedQueryParam("slbName") String slbName,
                        @TrimmedQueryParam("server") String serverIp,
                        @TrimmedQueryParam("type") String type,
                        @TrimmedQueryParam("mode") final String mode) throws Exception {
        if (slbId == null && slbName == null) {
            throw new Exception("Missing parameter.");
        }
        if (slbId == null && slbName != null) {
            slbId = slbCriteriaQuery.queryByName(slbName);
        }
        if (slbId == null)
            throw new ValidationException("Slb id cannot be found.");

        Slb slb = slbRepository.getById(slbId);
        if (slb == null)
            throw new ValidationException("Slb cannot be found.");
        return responseHandler.handle(getSlbByType(slb, type), hh.getMediaType());
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
            throw new Exception("Missing parameter.");
        }
        int count = slbRepository.delete(slbId);
        String message = count == 1 ? "Delete slb successfully." : "No deletion is needed.";
        return responseHandler.handle(message, hh.getMediaType());
    }

    @GET
    @Path("/slb/upgradeAll")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response upgradeAll(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        Set<Long> list = slbCriteriaQuery.queryAll();
        Set<Long> result = slbRepository.port(list.toArray(new Long[list.size()]));
        if (result.size() == 0)
            return responseHandler.handle("Upgrade all successfully.", hh.getMediaType());
        else
            return responseHandler.handle("Upgrade fail on ids: " + Joiner.on(",").join(result), hh.getMediaType());
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

    private Slb getSlbByType(Slb slb, String type) {
        if ("INFO".equalsIgnoreCase(type)) {
            return new Slb().setId(slb.getId())
                    .setName(slb.getName());
        }
        if ("NORMAL".equalsIgnoreCase(type)) {
            Slb result = new Slb().setId(slb.getId())
                    .setName(slb.getName())
                    .setNginxBin(slb.getNginxBin())
                    .setNginxConf(slb.getNginxConf())
                    .setNginxWorkerProcesses(slb.getNginxWorkerProcesses())
                    .setStatus(slb.getStatus())
                    .setVersion(slb.getVersion());
            for (SlbServer slbServer : slb.getSlbServers()) {
                result.addSlbServer(slbServer);
            }
            return result;
        }
        return slb;
    }

    private String trimIfNotNull(String value) {
        return value != null ? value.trim() : value;
    }
}
