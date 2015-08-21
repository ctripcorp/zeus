package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.nginx.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/nginx")
public class NginxResource {

    @Resource
    private NginxService nginxService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private NginxConfService nginxConfService;
    @Resource
    private NginxConfServerDao nginxConfServerDao;
    @Resource
    private NginxConfUpstreamDao nginxConfUpstreamDao;
    @Resource
    private SlbRepository slbRepository;

    @GET
    @Path("/load")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response load(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("slbId") Long slbId, @QueryParam("version") Integer version) throws Exception{
        NginxResponse result = nginxService.load(slbId,version);
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(NginxResponse.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(NginxResponse.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/conf")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVsConf(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("vs") Long vsid ,@QueryParam("version") Integer versionNum) throws Exception{
        VirtualServerConfResponse response = new VirtualServerConfResponse();

        Slb slb = slbRepository.getByVirtualServer(vsid);
        int version;
        if (null == versionNum || versionNum <= 0)
        {
            version= nginxConfService.getCurrentVersion(slb.getId());
        }else
        {
            version = versionNum;
        }
        NginxConfServerDo nginxConfServerDo = nginxConfServerDao.findBySlbVirtualServerIdAndVersion(vsid, version, NginxConfServerEntity.READSET_FULL);
        NginxConfUpstreamDo nginxConfUpstreamDo = nginxConfUpstreamDao.findBySlbVirtualServerIdAndVersion(vsid,version,NginxConfUpstreamEntity.READSET_FULL);
        response.setVersion(version)
                .setServerConf(nginxConfServerDo.getContent())
                .setUpstreamConf(nginxConfUpstreamDo.getContent())
                .setVirtualServerId(vsid);
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/write")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response write(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("VirtualServer")List<Long> vsIds , @QueryParam("slbId") Long slbId ,@QueryParam("version") Integer version)throws Exception
    {
        NginxResponse result = nginxService.writeToDisk(vsIds,slbId,version);
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(NginxResponse.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(NginxResponse.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }

    }
    @POST
    @Path("/dyups/{upStreamName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response localDyups(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @PathParam("upStreamName") String upsName, String upsCommands )throws Exception{
        NginxResponse result = nginxService.dyopsLocal(upsName,upsCommands);
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(NginxResponse.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(NginxResponse.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }
    }


    @GET
    @Path("/status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response status(@Context HttpServletRequest request,@Context HttpHeaders hh) throws Exception {
        NginxServerStatus status = nginxService.getStatus();
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(NginxServerStatus.XML, status)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(NginxServerStatus.JSON, status)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/trafficStatus")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getLocalTrafficStatus(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                          @QueryParam("since") Long since,
                                          @QueryParam("count") int count) throws Exception {
        count = count == 0 ? 1 : count;
        since = since == null ? System.currentTimeMillis() - 60 * 1000L : since;
        List<ReqStatus> statuses = nginxService.getLocalTrafficStatus(new Date(since), count);
        TrafficStatusList l = new TrafficStatusList().setTotal(statuses.size());
        for (ReqStatus status : statuses) {
            l.addReqStatus(status);
        }
        return responseHandler.handle(l, hh.getMediaType());
    }

    @GET
    @Path("/trafficStatus/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getLocalTrafficStatus(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                          @QueryParam("since") Long since,
                                          @QueryParam("groupName") String groupName,
                                          @QueryParam("count") int count) throws Exception {
        count = count == 0 ? 1 : count;
        since = since == null ? System.currentTimeMillis() - 60 * 1000L : since;
        List<ReqStatus> statuses = nginxService.getLocalTrafficStatus(new Date(since), groupName, count);
        TrafficStatusList l = new TrafficStatusList().setTotal(statuses.size());
        for (ReqStatus status : statuses) {
            l.addReqStatus(status);
        }
        return responseHandler.handle(l, hh.getMediaType());
    }

    @GET
    @Path("/loadAll/slb/{slbId:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response loadAll(@Context HttpServletRequest request,@Context HttpHeaders hh, @PathParam("slbId") Long slbId) throws Exception {
        List<NginxResponse> nginxResponseList = nginxService.loadAll(slbId,null);
        NginxResponseList result = new NginxResponseList();
        for (NginxResponse nginxResponse : nginxResponseList) {
            result.addNginxResponse(nginxResponse);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(NginxResponseList.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(NginxResponseList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/allStatus/slb/{slbId:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response allStatus(@Context HttpServletRequest request,@Context HttpHeaders hh, @PathParam("slbId") Long slbId) throws Exception {
        List<NginxServerStatus> nginxServerStatusList = nginxService.getStatusAll(slbId);
        NginxServerStatusList result = new NginxServerStatusList();
        for (NginxServerStatus nginxServerStatus : nginxServerStatusList) {
            result.addNginxServerStatus(nginxServerStatus);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(NginxServerStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(NginxServerStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
