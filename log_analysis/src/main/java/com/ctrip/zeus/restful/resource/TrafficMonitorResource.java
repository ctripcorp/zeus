package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.TrafficQueryCommand;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.traffic.TrafficMonitorService;
import com.ctrip.zeus.util.ObjectJsonParser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/traffic")
public class TrafficMonitorResource {

    @Resource
    private TrafficMonitorService trafficMonitorService;
    @Resource
    private ResponseHandler responseHandler;

    @POST
    @Path("/local/query")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response query(@Context HttpServletRequest request, @Context HttpHeaders hh,
                          String body) throws Exception {
        TrafficQueryCommand cmd = new TrafficQueryCommand(null, null);
        if (body != null) {
            cmd = ObjectJsonParser.parse(body, TrafficQueryCommand.class);
            if (cmd == null){
                throw new ValidationException("Invalidate Query Command.");
            }
        }

        return responseHandler.handle(trafficMonitorService.query(cmd.getTags(), cmd.getGroupBy()), hh.getMediaType());
    }
}
