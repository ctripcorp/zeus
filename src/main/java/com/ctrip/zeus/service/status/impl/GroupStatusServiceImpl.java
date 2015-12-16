package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.dal.core.ConfSlbActiveDao;
import com.ctrip.zeus.dal.core.ConfSlbActiveDo;
import com.ctrip.zeus.dal.core.ConfSlbActiveEntity;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.HealthCheckStatusService;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.entity.GroupStatusList;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * User: mag
 * Date: 4/1/2015
 * Time: 2:26 PM
 */
@Service("groupStatusService")
public class GroupStatusServiceImpl implements GroupStatusService {
    private static DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    @Resource
    SlbRepository slbRepository;
    @Resource
    SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    GroupRepository groupRepository;
    @Resource
    StatusService statusService;
    @Resource
    ConfSlbActiveDao confSlbActiveDao;
    @Resource
    private ActivateService activateService;
    @Resource
    private ActiveConfService activeConfService;
    @Resource
    private HealthCheckStatusService healthCheckStatusService;

    private Logger LOGGER = LoggerFactory.getLogger(GroupStatusServiceImpl.class);

    @Override
    public List<GroupStatus> getAllOnlineGroupsStatus() throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        List<ConfSlbActiveDo> slbList = confSlbActiveDao.findAll(ConfSlbActiveEntity.READSET_FULL);
        for (ConfSlbActiveDo slb : slbList) {
            result.addAll(getOnlineGroupsStatusBySlbId(slb.getSlbId()));
        }
        return result;
    }
    @Override
    public List<GroupStatus> getAllOfflineGroupsStatus() throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        Set<Long> slbList = slbCriteriaQuery.queryAll();
        for (Long slbId : slbList) {
            result.addAll(getOfflineGroupsStatusBySlbId(slbId));
        }
        return result;
    }
    @Override
    public List<GroupStatus> getOnlineGroupsStatusBySlbId(Long slbId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        Set<Long> activated = activeConfService.getGroupIdsBySlbId(slbId);
        if (activated == null || activated.size() == 0 )
        {
            return result;
        }
        result = getOnlineGroupsStatus(activated, slbId);
        return result;
    }
    @Override
    public List<GroupStatus> getOfflineGroupsStatusBySlbId(Long slbId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        Set<Long> activated = groupCriteriaQuery.queryBySlbId(slbId);
        if (activated == null || activated.size() == 0 )
        {
            return result;
        }
        result = getOfflineGroupsStatus(activated, slbId);
        return result;
    }

    @Override
    public List<GroupStatus> getOnlineGroupStatus(Long groupId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        Set<Long> slbIds = activeConfService.getSlbIdsByGroupId(groupId);
        Set<Long> gids = new HashSet<>();
        gids.add(groupId);
        for (Long slb : slbIds) {
            result.addAll(getOnlineGroupsStatus(gids, slb));
        }
        return result;
    }
    @Override
    public List<GroupStatus> getOfflineGroupStatus(Long groupId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        Set<Long> slbIds = slbCriteriaQuery.queryByGroups(new Long[]{groupId});
        Set<Long> gids = new HashSet<>();
        gids.add(groupId);
        for (Long slb : slbIds) {
            result.addAll(getOfflineGroupsStatus(gids, slb));
        }
        return result;
    }
    @Override
    public List<GroupStatus> getOnlineGroupsStatus(Set<Long> groupIds , Long slbId) throws Exception
    {
        Slb slb = activateService.getActivatedSlb(slbId);
        AssertUtils.assertNotNull(slb, "slbId not found!");
        AssertUtils.assertNotEquals(0, slb.getSlbServers().size(), "Slb doesn't have any slb server!");
        List<Group> groups = activateService.getActivatedGroups(groupIds.toArray(new Long[]{}), slbId);
        Set<Long> vsId = activeConfService.getVsIdsBySlbId(slbId);
        List<GroupStatus> res = new ArrayList<>();
        GroupStatus status = null;

        Set<String> allUpGroupServerInSlb = statusService.fetchGroupServersByVsIdsAndStatusOffset(vsId.toArray(new Long[]{}), StatusOffset.MEMBER_OPS, true);
        Set<String> allPullInGroupServerInSlb = statusService.fetchGroupServersByVsIdsAndStatusOffset(vsId.toArray(new Long[]{}), StatusOffset.PULL_OPS, true);
        Map<String,Boolean> healthCheck = healthCheckStatusService.getHealthCheckStatusBySlbId(slbId);
        Set<String> allDownServers = statusService.findAllDownServers();
        Set<Long> activated = groupCriteriaQuery.queryBySlbId(slbId);

        for (Group group : groups)
        {
            Long groupId = group.getId();

            status = new GroupStatus();
            status.setGroupId(groupId);
            status.setSlbId(slbId);
            status.setGroupName(group.getName());
            status.setSlbName(slb.getName());
            status.setActivated(activated.contains(groupId));

            List<GroupServer> groupServerList = group.getGroupServers();//groupRepository.listGroupServersByGroup(groupId);
            Long gvsId = null;
            for (GroupVirtualServer gv : group.getGroupVirtualServers()){
                if (vsId.contains(gv.getVirtualServer().getId())){
                    gvsId = gv.getVirtualServer().getId();
                    break;
                }
            }
            for (GroupServer gs : groupServerList){
                GroupServerStatus groupServerStatus = new GroupServerStatus();
                groupServerStatus.setIp(gs.getIp());
                groupServerStatus.setPort(gs.getPort());
                String key = gvsId + "_" + groupId +"_"+gs.getIp();
                boolean memberUp = allUpGroupServerInSlb.contains(key);
                boolean serverUp = !allDownServers.contains(gs.getIp());
                boolean pullIn = allPullInGroupServerInSlb.contains(key);
                groupServerStatus.setServer(serverUp);
                groupServerStatus.setMember(memberUp);
                groupServerStatus.setPull(pullIn);
                boolean up = healthCheck.containsKey(key)&&(memberUp&&serverUp&&pullIn)?healthCheck.get(key):memberUp&&serverUp&&pullIn;
                groupServerStatus.setUp(up);
                status.addGroupServerStatus(groupServerStatus);
            }
            res.add(status);
        }
        return res;
    }
    @Override
    public List<GroupStatus> getOfflineGroupsStatus(Set<Long> groupIds , Long slbId) throws Exception
    {
        Slb slb = slbRepository.getById(slbId);
        AssertUtils.assertNotNull(slb, "slbId not found!");
        AssertUtils.assertNotEquals(0, slb.getSlbServers().size(), "Slb doesn't have any slb server!");

        List<Group> groups = groupRepository.list(groupIds.toArray(new Long[]{}));
        Set<Long> vsId = virtualServerCriteriaQuery.queryBySlbId(slbId);
        List<GroupStatus> res = new ArrayList<>();
        GroupStatus status = null;

        Set<String> allUpGroupServerInSlb = statusService.fetchGroupServersByVsIdsAndStatusOffset(vsId.toArray(new Long[]{}), StatusOffset.MEMBER_OPS, true);
        Set<String> allPullInGroupServerInSlb = statusService.fetchGroupServersByVsIdsAndStatusOffset(vsId.toArray(new Long[]{}), StatusOffset.PULL_OPS, true);
        Map<String,Boolean> healthCheck = healthCheckStatusService.getHealthCheckStatusBySlbId(slbId);
        Set<String> allDownServers = statusService.findAllDownServers();
        Set<Long> activated = groupCriteriaQuery.queryBySlbId(slbId);

        for (Group group : groups)
        {
            Long groupId = group.getId();

            status = new GroupStatus();
            status.setGroupId(groupId);
            status.setSlbId(slbId);
            status.setGroupName(group.getName());
            status.setSlbName(slb.getName());
            status.setActivated(activated.contains(groupId));

            List<GroupServer> groupServerList = group.getGroupServers();//groupRepository.listGroupServersByGroup(groupId);
            Long gvsId = null;
            for (GroupVirtualServer gv : group.getGroupVirtualServers()){
                if (vsId.contains(gv.getVirtualServer().getId())){
                    gvsId = gv.getVirtualServer().getId();
                    break;
                }
            }
            for (GroupServer gs : groupServerList){
                GroupServerStatus groupServerStatus = new GroupServerStatus();
                groupServerStatus.setIp(gs.getIp());
                groupServerStatus.setPort(gs.getPort());
                String key = gvsId + "_" + groupId +"_"+gs.getIp();
                boolean memberUp = allUpGroupServerInSlb.contains(key);
                boolean serverUp = !allDownServers.contains(gs.getIp());
                boolean pullIn = allPullInGroupServerInSlb.contains(key);
                groupServerStatus.setServer(serverUp);
                groupServerStatus.setMember(memberUp);
                groupServerStatus.setPull(pullIn);
                boolean up = healthCheck.containsKey(key)&&(memberUp&&serverUp&&pullIn)?healthCheck.get(key):memberUp&&serverUp&&pullIn;
                groupServerStatus.setUp(up);
                status.addGroupServerStatus(groupServerStatus);
            }
            res.add(status);
        }
        return res;
    }
}
