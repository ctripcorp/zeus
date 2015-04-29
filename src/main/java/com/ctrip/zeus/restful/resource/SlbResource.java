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
@Path("/slb")
public class SlbResource {
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;

    @GET
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
    @Path("/get/{slbName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getSlb")
    public Response getBySlbName(@Context HttpHeaders hh, @Context HttpServletRequest request, @PathParam("slbName") String slbName) throws Exception {
        Slb slb = slbRepository.get(slbName);
        return responseHandler.handle(slb, hh.getMediaType());
    }

    @POST
    @Path("/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addSlb")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String slb) throws Exception {
        Slb s;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            s = DefaultSaxParser.parseEntity(Slb.class, slb);
        } else if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
            s = DefaultJsonParser.parse(Slb.class, slb);
        } else {
            throw new Exception("Unacceptable type.");
        }
        slbRepository.add(s);
        return Response.ok().build();
    }

    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateSlb")
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String slb) throws Exception {
        Slb s;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            s = DefaultSaxParser.parseEntity(Slb.class, slb);
        } else if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
            s = DefaultJsonParser.parse(Slb.class, slb);
        } else {
            throw new Exception("Unacceptable type.");
        }
        DistLock lock = dbLockFactory.newLock(s.getName() + "_update");
        try {
            lock.lock();
            slbRepository.update(s);
        } finally {
            lock.unlock();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteSlb")
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("slbName") String slbName) throws Exception {
        if (slbName == null || slbName.isEmpty()) {
            throw new Exception("Missing parameter or value.");
        }
        slbRepository.delete(slbName);
        return Response.ok().build();
    }
}
