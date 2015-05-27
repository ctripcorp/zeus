package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.service.Activate.ActivateService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
    public Response activateByName(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("slbName") List<String> slbNames,  @QueryParam("GroupName") List<String> groupNames)throws Exception{
//        return activateAll(slbNames,groupNames,hh);
        return null;
    }

    @GET
    @Path("/activate")
    @Authorize(name="activate")
    public Response activateById(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("slbId") List<Long> slbIds,  @QueryParam("GroupId") List<Long> groupIds)throws Exception{
        return activateAll(slbIds,groupIds,hh);
    }


    private Response activateAll(List<Long> slbIds,List<Long> groupIds, HttpHeaders hh)throws Exception{

        AssertUtils.arrertNotEquels(0,slbIds.size()+groupIds.size(),"slbIds list and groupIds list are empty!");

        //update active action to conf-slb-active and conf-app-active
        activateConfService.activate(slbIds,groupIds);

        //find all slbs which need build config
        Set<Long> slbList = buildInfoService.getAllNeededSlb(slbIds, groupIds);

        if (slbList.size() > 0)
        {
            //build all slb config
            for (Long buildSlbId : slbList) {
                int ticket = buildInfoService.getTicket(buildSlbId);
                boolean buildFlag = false;
                DistLock buildLock = dbLockFactory.newLock( "build_" + buildSlbId);
                try{
                    buildLock.lock(lockTimeout.get());
                    buildFlag =buildService.build(buildSlbId,ticket);
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
