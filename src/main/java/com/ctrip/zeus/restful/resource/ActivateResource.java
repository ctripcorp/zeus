package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.ConfAppName;
import com.ctrip.zeus.model.entity.ConfReq;
import com.ctrip.zeus.model.entity.ConfSlbName;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.Activate.ActivateService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.util.AssertUtils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
    private NginxService nginxAgentService;
    @Resource
    private BuildInfoService buildInfoService;
    @Resource
    private BuildService buildService;
    @Resource
    private DbLockFactory dbLockFactory;

    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);



    @GET
    @Path("/activate")
    @Authorize(name="activate")
    public Response list(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("slbName") List<String> slbNames,  @QueryParam("appName") List<String> appNames)throws Exception{
        return activateAll(slbNames,appNames,hh);
    }

    @POST
    @Path("/activate")
    @Authorize(name="activate")
    public Response activate(@Context HttpServletRequest request,@Context HttpHeaders hh, String req) throws Exception {
        ConfReq confreq = null;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            confreq = DefaultSaxParser.parseEntity(ConfReq.class, req);
        }else //if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE))
        {
            confreq = DefaultJsonParser.parse(ConfReq.class, req);
        }

        AssertUtils.isNull(confreq,"the parameter is illegal!\n request parameter: "+req);

        List<String> appNameList = Lists.transform(confreq.getConfAppNames(),new Function<ConfAppName, String>(){
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

        List<String> slbNameList = Lists.transform(confreq.getConfSlbNames(),new Function<ConfSlbName, String>(){
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

        return activateAll(slbNameList,appNameList,hh);
    }

    private Response activateAll(List<String> slbNames,List<String> appNames, HttpHeaders hh)throws Exception{

        AssertUtils.arrertNotEquels(0,slbNames.size()+appNames.size(),"slbName list and appName list are empty!");

        //update active action to conf-slb-active and conf-app-active
        activateConfService.activate(slbNames,appNames);

        //find all slbs which need build config
        Set<String> slbList = buildInfoService.getAllNeededSlb(slbNames, appNames);

        if (slbList.size() > 0)
        {
            //build all slb config
            for (String buildSlbName : slbList) {
                int ticket = buildInfoService.getTicket(buildSlbName);
                boolean buildFlag = false;
                DistLock buildLock = dbLockFactory.newLock(buildSlbName + "_build");
                try{
                    buildLock.lock(lockTimeout.get());
                    buildFlag =buildService.build(buildSlbName,ticket);
                }finally {
                    buildLock.unlock();
                }
                if (buildFlag) {
                    DistLock writeLock = dbLockFactory.newLock(buildSlbName + "_writeAndReload");
                    try {
                        writeLock.lock(lockTimeout.get());
                        //Push Service
                        nginxAgentService.writeAllAndLoadAll(buildSlbName);

                    } finally {
                        writeLock.unlock();
                    }
                }
            }
            return Response.ok().status(200).build();
        }else
        {
            return Response.status(200).type(hh.getMediaType()).entity("No slb need activate!please check your config").build();
        }


    }
}
