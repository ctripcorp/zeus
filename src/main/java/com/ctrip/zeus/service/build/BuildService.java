package com.ctrip.zeus.service.build;


import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface BuildService extends Repository {

    Long build(Slb nxOnlineSlb,
               Map<Long, VirtualServer> nxOnlineVses,
               Set<Long> buildingVsIds,
               Set<Long> clearingVsIds,
               Map<Long, List<TrafficPolicy>> policiesByVsId,
               Map<Long, List<Group>> groupsByVsId,
               Set<String> serversToBeMarkedDown,
               Set<String> groupMembersToBeMarkedUp) throws Exception;

    DyUpstreamOpsData buildUpstream(Long slbId,
                                    VirtualServer buildVirtualServer,
                                    Set<String> allDownServers,
                                    Set<String> allUpGroupServers,
                                    Group group) throws Exception;

    void rollBackConfig(Long slbId, int version) throws Exception;
}
