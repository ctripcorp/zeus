package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.SlbRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

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
    public Response list(@Context HttpHeaders hh) throws Exception {
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
    public Response getBySlbName(@Context HttpHeaders hh, @PathParam("slbName") String slbName) throws Exception {
        Slb slb = slbRepository.get(slbName);
        return responseHandler.handle(slb, hh.getMediaType());
    }

    @POST
    @Path("/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(@Context HttpHeaders hh, String slb) throws Exception {
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
    public Response update(@Context HttpHeaders hh, String slb) throws Exception {
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
    public Response delete(@Context HttpHeaders hh, @QueryParam("slbName") String slbName) throws Exception {
        if (slbName == null || slbName.isEmpty()) {
            throw new Exception("Missing parameter or value.");
        }
        slbRepository.delete(slbName);
        return Response.ok().build();
    }
}
