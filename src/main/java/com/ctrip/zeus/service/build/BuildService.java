package com.ctrip.zeus.service.build;


import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.Repository;
import com.ctrip.zeus.task.entity.OpsTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface BuildService extends Repository {

    Long build(Slb onlineSlb,
                 Map<Long,VirtualServer> onlineVses,
                 Set<Long> needBuildVses,
                 Set<Long> deactivateVses,
                 Map<Long,List<Group>> vsGroups,
                 Set<String> allDownServers,
                 Set<String>allUpGroupServers
    )throws Exception;

    DyUpstreamOpsData buildUpstream(Long slbId,
                                          VirtualServer buildVirtualServer,
                                          Set<String>allDownServers ,
                                          Set<String> allUpGroupServers,
                                          Group group ) throws Exception;
    void rollBackConfig(Long slbId,int version) throws Exception;
}
