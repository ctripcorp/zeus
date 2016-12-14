package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.operationlog.entity.OperationLogData;
import com.ctrip.zeus.operationlog.entity.OperationLogDataList;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.operationLog.OperationLogService;
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
 * Created by fanqq on 2015/7/20.
 */
@Component
@Path("/logs")
public class OperationLogResource {
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ResponseHandler responseHandler;
    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response logs(@Context HttpServletRequest request,
                         @Context HttpHeaders hh ,
                         @QueryParam("type")String type,
                         @QueryParam("targetId")String targetId,
                         @QueryParam("op")String op,
                         @QueryParam("user")String user,
                         @QueryParam("clientIp")String clientIp,
                         @QueryParam("success")Boolean success,
                         @QueryParam("fromDate")String from,
                         @QueryParam("toDate")String to,
                         @QueryParam("count")Long count
                         ) throws Exception{

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date fromDate = null;
        Date toDate = null;
        if (from !=null){
            fromDate= sdf.parse(from);
        }
        if (to != null) {
            toDate = sdf.parse(to);
        }
        OperationLogDataList result = operationLogService.find(type,targetId,op,user,clientIp,success,fromDate,toDate,count);
        return responseHandler.handle(result,hh.getMediaType());
    }
}
