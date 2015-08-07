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
    /**
     * build config by slb name and ticket number
     * @param slbId the slb id
     * @param ticket the ticket number
     * @return status
     * @throws Exception
     */
    boolean build(Long slbId, int ticket) throws Exception;

    List<VirtualServer> getNeedBuildVirtualServers(Long slbId,HashMap<Long , Group> activatingGroups , Set<Long>groupList)throws Exception;

    Map<Long,List<Group>> getInfluencedVsGroups(Long slbId,HashMap<Long,Group>activatingGroups,List<VirtualServer>buildVirtualServer,Set<Long> deactivateGroup)throws Exception;

    void build(Long slbId,
                 Slb activatedSlb,
                 List<VirtualServer>buildVirtualServer,
                 Map<Long,List<Group>>groupsMap,
                 Set<String> allDownServers,
                 Set<String>allUpGroupServers
    )throws Exception;
    List<DyUpstreamOpsData> buildUpstream(Long slbId, Set<String>allDownServers ,Set<String> allUpGroupServers,Group group ) throws Exception;
    void rollBackConfig(Long slbId,int version) throws Exception;
}
