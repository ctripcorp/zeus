package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.restful.message.ResponseHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by fanqq on 2015/8/24.
 */
@Component
@Path("/test")
public class TestResource {
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Path("/sleep")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response logs(@Context HttpServletRequest request,
                         @Context HttpHeaders hh,
                         @QueryParam("interval") Long interval
    ) throws Exception {
        DistLock lock = dbLockFactory.newLock("testGracefulShutdown");
        long start = System.nanoTime();
        try {
            lock.lock();
            Thread.sleep(interval);
        } finally {
            lock.unlock();
        }
        return responseHandler.handle("Awake after " + (System.nanoTime() - start) / 1000000 + ".", hh.getMediaType());
    }
}
