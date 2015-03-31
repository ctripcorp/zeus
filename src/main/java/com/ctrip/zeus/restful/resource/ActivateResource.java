package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.ConfAppName;
import com.ctrip.zeus.model.entity.ConfReq;
import com.ctrip.zeus.model.entity.ConfSlbName;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.Activate.ActivateService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.nginx.NginxAgentService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/3/20.
 */

@Component
@Path("/conf")
public class ActivateResource {

    @Resource
    private ActivateService activateConfService;
    @Resource
    private NginxAgentService nginxAgentService;
    @Resource
    private BuildInfoService buildInfoService;
    @Resource
    private BuildService buildService;

    @POST
    @Path("/activate")
    public Response activate(@Context HttpHeaders hh, String req) throws Exception {
        ConfReq confreq = null;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            confreq = DefaultSaxParser.parseEntity(ConfReq.class, req);
        }else //if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE))
        {
            confreq = DefaultJsonParser.parse(ConfReq.class, req);
        }

        List<String> appnamelist = Lists.transform(confreq.getConfAppNames(),new Function<ConfAppName, String>(){
            @Nullable
            @Override
            public String apply(@Nullable ConfAppName confAppName) {
                try {
                    return confAppName.getAppname();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });

        List<String> slbnamelist = Lists.transform(confreq.getConfSlbNames(),new Function<ConfSlbName, String>(){
            @Nullable
            @Override
            public String apply(@Nullable ConfSlbName slbName) {
                try {
                    return slbName.getSlbname();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });


        if (confreq!=null && (slbnamelist.size()+appnamelist.size() > 0))
        {
            //update active action to conf-slb-active and conf-app-active
            activateConfService.activate(slbnamelist, appnamelist);

            //find all slbs which need build config
            Set<String> slblist = buildInfoService.getAllNeededSlb(slbnamelist, appnamelist);

            if (slblist.size() > 0){
                //build all slb config
                for (String buildslbName : slblist) {
                    int ticket = buildInfoService.getTicket(buildslbName);
                    if(buildService.build(buildslbName,ticket))
                    {
                        //Push Service
                        nginxAgentService.reloadConf(buildslbName);
                    }
                }
            }else {
                return Response.status(200).type(hh.getMediaType()).entity("no slb need activate!").build();
            }

        }else
        {
            //bad request
            return Response.status(500).type(hh.getMediaType()).build();
        }

        return Response.ok().build();
    }

}
