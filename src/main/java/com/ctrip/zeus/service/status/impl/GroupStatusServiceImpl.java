package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.ConfSlbActiveDao;
import com.ctrip.zeus.dal.core.ConfSlbActiveDo;
import com.ctrip.zeus.dal.core.ConfSlbActiveEntity;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.HealthCheckStatusService;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
    MappingFactory mappingFactory;
    @Resource
    private HealthCheckStatusService healthCheckStatusService;

    private Logger LOGGER = LoggerFactory.getLogger(GroupStatusServiceImpl.class);

    @Override
    public List<GroupStatus> getAllOnlineGroupsStatus() throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        Set<IdVersion> slbIds = slbCriteriaQuery.queryAll(ModelMode.MODEL_MODE_ONLINE);
        for (IdVersion slb : slbIds) {
            result.addAll(getOnlineGroupsStatusBySlbId(slb));
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
    public List<GroupStatus> getOnlineGroupsStatusBySlbId(IdVersion slbId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        ModelStatusMapping<VirtualServer> vses = mappingFactory.getBySlbIds(slbId.getId());
        ModelStatusMapping<Group> groups = mappingFactory.getByVsIds(vses.getOnlineMapping().keySet().toArray(new Long[]{}));
        if (groups.getOnlineMapping() == null || groups.getOnlineMapping().size() == 0 )
        {
            return result;
        }
        result = getOnlineGroupsStatus(groups.getOnlineMapping(),vses.getOnlineMapping().keySet(), slbId);
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
    public List<GroupStatus> getOnlineGroupsStatus(Map<Long,Group> groups ,Set<Long> vsIds ,  IdVersion slbId) throws Exception
    {
        Slb slb = slbRepository.getByKey(slbId);
        AssertUtils.assertNotNull(slb, "slbId not found!");
        AssertUtils.assertNotEquals(0, slb.getSlbServers().size(), "Slb doesn't have any slb server!");
        List<GroupStatus> res = new ArrayList<>();
        GroupStatus status = null;

        Map<String,List<Boolean>> memberStatus = statusService.fetchGroupServersByVsIds(vsIds.toArray(new Long[]{}));
        Map<String,Boolean> healthCheck = healthCheckStatusService.getHealthCheckStatusBySlbId(slbId.getId());
        Set<String> allDownServers = statusService.findAllDownServers();
        for (Group group : groups.values())
        {
            Long groupId = group.getId();

            status = new GroupStatus();
            status.setGroupId(groupId);
            status.setSlbId(slbId.getId());
            status.setGroupName(group.getName());
            status.setSlbName(slb.getName());
            status.setActivated(true);

            List<GroupServer> groupServerList = group.getGroupServers();//groupRepository.listGroupServersByGroup(groupId);
            Long gvsId = null;
            for (GroupVirtualServer gv : group.getGroupVirtualServers()){
                if (vsIds.contains(gv.getVirtualServer().getId())){
                    gvsId = gv.getVirtualServer().getId();
                    break;
                }
            }
            for (GroupServer gs : groupServerList){
                GroupServerStatus groupServerStatus = new GroupServerStatus();
                groupServerStatus.setIp(gs.getIp());
                groupServerStatus.setPort(gs.getPort());
                String key =gvsId + "_" + groupId +"_"+ gs.getIp();
                boolean memberUp = memberStatus.get(key).get(StatusOffset.MEMBER_OPS);
                boolean serverUp = !allDownServers.contains(gs.getIp());
                boolean pullIn =  memberStatus.get(key).get(StatusOffset.PULL_OPS);
                groupServerStatus.setServer(serverUp);
                groupServerStatus.setMember(memberUp);
                groupServerStatus.setPull(pullIn);
                key = groupId +"_"+ gs.getIp();
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
        Map <Long,Boolean> activated = activateService.isGroupsActivated(groupIds.toArray(new Long[]{}),null);

        for (Group group : groups)
        {
            Long groupId = group.getId();

            status = new GroupStatus();
            status.setGroupId(groupId);
            status.setSlbId(slbId);
            status.setGroupName(group.getName());
            status.setSlbName(slb.getName());
            status.setActivated(activated.get(groupId));

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
                String key =gvsId + "_" + groupId +"_"+ gs.getIp();
                boolean memberUp = allUpGroupServerInSlb.contains(key);
                boolean serverUp = !allDownServers.contains(gs.getIp());
                boolean pullIn = allPullInGroupServerInSlb.contains(key);
                groupServerStatus.setServer(serverUp);
                groupServerStatus.setMember(memberUp);
                groupServerStatus.setPull(pullIn);
                key = groupId +"_"+ gs.getIp();
                boolean up = healthCheck.containsKey(key)&&(memberUp&&serverUp&&pullIn)?healthCheck.get(key):memberUp&&serverUp&&pullIn;
                groupServerStatus.setUp(up);
                status.addGroupServerStatus(groupServerStatus);
            }
            res.add(status);
        }
        return res;
    }
}
