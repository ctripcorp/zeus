package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
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
    private EntityFactory entityFactory;

    @GET
    @Path("/conf")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVsConf(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("vsId") Long vsid ,@QueryParam("version") Integer versionNum) throws Exception{
        VirtualServerConfResponse response = new VirtualServerConfResponse();
        ModelStatusMapping<VirtualServer> map =  entityFactory.getVsesByIds(new Long[]{vsid});
        if (map.getOnlineMapping() == null || map.getOnlineMapping().get(vsid) == null){
            throw new ValidationException("Not Found Vs by vsId.");
        }
        Long slbId = map.getOnlineMapping().get(vsid).getSlbId();
        int version;
        if (null == versionNum || versionNum <= 0)
        {
            version= nginxConfService.getCurrentVersion(slbId);
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
}
