package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.task.entity.OpsTaskList;
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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fanqq on 2015/8/24.
 */
@Component
@Path("/")
public class TaskResource {
    @Resource
    private TaskService taskService;
    @Resource
    private ResponseHandler responseHandler;
    @GET
    @Path("/tasks")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response logs(@Context HttpServletRequest request,
                         @Context HttpHeaders hh ,
                         @QueryParam("opsType")String opsType,
                         @QueryParam("targetSlbId")Long targetSlbId,
                         @QueryParam("fromDate")String from
    ) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date fromDate = null;
        if (from !=null){
            fromDate= sdf.parse(from);
        }
        OpsTaskList result = taskService.find(fromDate, opsType, targetSlbId);
        return responseHandler.handle(result,hh.getMediaType());
    }
}
