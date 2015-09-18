package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.AssertUtils;
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
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;

    @GET
    @Path("/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllSlbs")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("type") String type,
                         @TrimmedQueryParam("tag") String tag,
                         @TrimmedQueryParam("pname") String pname,
                         @TrimmedQueryParam("pvalue") String pvalue) throws Exception {
        SlbList slbList = new SlbList();
        boolean noFilter = true;
        Set<Long> filtered = new HashSet<>();
        if (tag != null) {
            noFilter = false;
            filtered.addAll(tagService.query(tag, "slb"));
        }
        if (pname != null) {
            noFilter = false;
            if (pvalue != null)
                filtered.addAll(propertyService.query(pname, pvalue, "slb"));
            else
                filtered.addAll(propertyService.query(pname, "slb"));
        }

        for (Slb slb : slbRepository.list()) {
            if (noFilter || filtered.contains(slb.getId()))
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
                        @TrimmedQueryParam("type") String type) throws Exception {
        if (slbId == null && slbName == null) {
            throw new Exception("Missing parameter.");
        }
        Slb slb = null;
        if (slbId != null) {
            slb = slbRepository.getById(slbId);
        }
        if (slb == null && slbName != null) {
            slb = slbRepository.get(slbName);
        }
        AssertUtils.assertNotNull(slb, "Slb cannot be found.");
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
        try {
            lock.lock();
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
        slbRepository.delete(slbId);
        return Response.ok().build();
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
        s.setName(s.getName().trim());
        for (VirtualServer virtualServer : s.getVirtualServers()) {
            virtualServer.setName(virtualServer.getName().trim());
            for (Domain domain : virtualServer.getDomains()) {
                domain.setName(domain.getName().trim());
            }
        }
        for (SlbServer slbServer : s.getSlbServers()) {
            slbServer.setIp(slbServer.getIp().trim());
            slbServer.setHostName(slbServer.getHostName().trim());
        }
        return s;
    }

    private Slb getSlbByType(Slb slb, String type) {
        if ("INFO".equalsIgnoreCase(type)) {
            return new Slb().setId(slb.getId())
                    .setName(slb.getName());
        }
        if ("NORMAL".equalsIgnoreCase(type)) {
            return new Slb().setId(slb.getId())
                    .setName(slb.getName())
                    .setNginxBin(slb.getNginxBin())
                    .setNginxConf(slb.getNginxConf())
                    .setNginxWorkerProcesses(slb.getNginxWorkerProcesses())
                    .setStatus(slb.getStatus())
                    .setVersion(slb.getVersion());
        }
        return slb;
    }
}
