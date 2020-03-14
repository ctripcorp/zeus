package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.TrafficPoint;
import com.ctrip.zeus.model.TrafficQueryCommand;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.service.traffic.TrafficMonitorService;
import com.ctrip.zeus.util.ObjectJsonParser;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Component
@Path("/traffic")
public class TrafficApiResource {

    @Resource
    private TrafficMonitorService trafficMonitorService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private ResponseHandler responseHandler;

    private DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @POST
    @Path("/query")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response query(@Context HttpHeaders hh,
                          @Context final HttpServletRequest request,
                          String body) throws Exception {
        TrafficQueryCommand cmd = new TrafficQueryCommand(null, null);
        if (body != null) {
            cmd = ObjectJsonParser.parse(body, TrafficQueryCommand.class);
            if (cmd == null) {
                throw new ValidationException("Invalidate Query Command.");
            }
        }
        Queue<String[]> params = new LinkedList<>();
        if (cmd.getExtraQuery() != null) {
            for (Map.Entry<String, String> entry : cmd.getExtraQuery().entrySet()) {
                params.add(new String[]{entry.getKey(), entry.getValue()});
            }
        }
        if (cmd.getTags() != null) {
            for (Map.Entry<String, String> entry : cmd.getTags().entrySet()) {
                params.add(new String[]{entry.getKey(), entry.getValue()});
            }
        }
        
        QueryEngine queryRender = new QueryEngine(params, "slb", SelectionMode.OFFLINE_FIRST);
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);
        List<Slb> slbList = slbRepository.list(searchKeys);
        Map<String, Double> aggResult = new LinkedHashMap<>();
        for (Slb slb : slbList) {
            for (SlbServer slbServer : slb.getSlbServers()) {
                try {
                    String data = NginxClient.getClient(buildRemoteUrl(slbServer.getIp())).getTraffic(body);
                    List<TrafficPoint> trafficPoints = ObjectJsonParser.parseArray(data, TrafficPoint.class);
                    assert trafficPoints != null;
                    trafficPoints.forEach(e -> {
                        aggResult.putIfAbsent(e.getGroupBy(), 0.0);
                        aggResult.put(e.getGroupBy(), aggResult.get(e.getGroupBy()) + e.getQps());
                    });
                } catch (Exception e) {
                    logger.warn("Fetch Slb Traffic Failed.SlbId:" + slb.getId() + "SlbIp:" + slbServer.getIp());
                }
            }
        }
        return responseHandler.handle(aggResult, hh.getMediaType());
    }

    private String buildRemoteUrl(String ip) {
        return "http://" + ip + ":" + adminServerPort.get();
    }
}
