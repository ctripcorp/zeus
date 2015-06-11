package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.netflix.config.DynamicBooleanProperty;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/6/11.
 */

@Component
@Path("/deactivate")
public class DeActivateResource {
    @Resource
    private ActivateService activateService;
    @Resource
    private NginxService nginxAgentService;
    @Resource
    private BuildInfoService buildInfoService;
    @Resource
    private BuildService buildService;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private GroupRepository groupRepository;

    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);
    private static DynamicBooleanProperty writable = DynamicPropertyFactory.getInstance().getBooleanProperty("activate.writable", true);

    @GET
    @Path("/group")
    @Authorize(name="deactivate")
    public Response deactivateGroup(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("groupId") List<Long> groupIds,  @QueryParam("groupName") List<String> groupNames)throws Exception{
        List<Long> _groupIds = new ArrayList<>();
        List<Long> _slbIds = new ArrayList<>();

        if ( groupIds!=null && !groupIds.isEmpty() )
        {
            _groupIds.addAll(groupIds);
        }
        if ( groupNames!=null && !groupNames.isEmpty() )
        {
            for (String groupName : groupNames)
            {
                Group group = groupRepository.get(groupName);
                if (group == null)
                {
                    continue;
                }
                _groupIds.add(group.getId());
            }
        }

        for (Long gid : _groupIds)
        {
            activateService.deactiveGroup(gid);
        }

        //find all slbs which need build config
        Set<Long> slbList = buildInfoService.getAllNeededSlb(_slbIds, groupIds);

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
                if (buildFlag && writable.get()) {
                    DistLock writeLock = dbLockFactory.newLock( "writeAndReload_" +  buildSlbId);
                    try {
                        writeLock.lock(lockTimeout.get());
                        //Push Service
                        nginxAgentService.writeAllAndLoadAll(buildSlbId);

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
