package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.util.AssertUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @GET
    @Path("/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllSlbs")
    public Response list(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        SlbList slbList = new SlbList();
        for (Slb slb : slbRepository.list()) {
            slbList.addSlb(slb);
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
                        @QueryParam("slbName") String slbName) throws Exception {
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
        return responseHandler.handle(slb, hh.getMediaType());
    }
    
    @POST
    @Path("/slb/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addSlb")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String slb) throws Exception {
        slbRepository.add(parseSlb(hh.getMediaType(), slb));
        return Response.ok().build();
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
            slbRepository.update(s);
        } finally {
            lock.unlock();
        }
        return Response.ok().build();
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
        return s;
    }
}
