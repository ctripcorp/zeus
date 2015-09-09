package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.client.StatusClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.S;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.GroupStatusService;
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
    GroupRepository groupRepository;

    @Resource
    StatusService statusService;
    @Resource
    private ActivateService activateService;
    @Resource
    private ActiveConfService activeConfService;


    private long currentSlbId = -1L;
    private Logger LOGGER = LoggerFactory.getLogger(GroupStatusServiceImpl.class);

    @Override
    public List<GroupStatus> getAllGroupStatus() throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        List<Slb> slbList = slbRepository.list();
        for (Slb slb : slbList) {
            result.addAll(getAllGroupStatus(slb.getId()));
        }
        return result;
    }

    @Override
    public List<GroupStatus> getAllGroupStatus(Long slbId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        List<Group> groups = groupRepository.list(slbId);
        List<Long> list = new ArrayList<>();
        Set<Long> groupIds = new HashSet<>();
        for (Group group : groups) {
            groupIds.add(group.getId());
        }
        Set<Long> activated = activeConfService.getGroupIdsBySlbId(slbId);
        if (activated != null)
        {
            groupIds.addAll(activated);
        }
        list.addAll(groupIds);
        if ( list.size() == 0 ){
            return result;
        }
        GroupStatusList appStatus = getGroupStatus(list, slbId);
        result.addAll(appStatus.getGroupStatuses());
        return result;
    }

    @Override
    public List<GroupStatus> getGroupStatus(Long groupId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        List<Slb> slbList = slbRepository.listByGroups(new Long[]{groupId});
        List<Long> list = new ArrayList<>();
        list.add(groupId);
        for (Slb slb : slbList) {
            result.addAll(getGroupStatus(list, slb.getId()).getGroupStatuses());
        }
        return result;
    }
    @Override
    public GroupStatusList getLocalGroupStatus(List<Long> groupIds , Long slbId) throws Exception
    {
        GroupStatusList res = new GroupStatusList();
        GroupStatus status = null;
        Slb slb = slbRepository.getById(slbId);
        AssertUtils.assertNotNull(slb, "slb Id not found!");
        List<Group> groups = groupRepository.list(groupIds.toArray(new Long[]{}));
        HashMap<Long,Boolean> isActivated = activateService.isGroupsActivated(groupIds.toArray(new Long[]{}),slbId);
        Set<String> allUpGroupServerInSlb = statusService.findAllUpGroupServersBySlbId(slbId);
        Set<String> allDownServers = statusService.findAllDownServers();

        for (Group group : groups)
        {
            Long groupId = group.getId();

            status = new GroupStatus();
            status.setGroupId(groupId);
            status.setSlbId(slbId);
            status.setGroupName(group.getName());
            status.setSlbName(slb.getName());
            status.setActivated(isActivated.get(groupId));

            Group activatedGroup = null;
            Map<String,Integer> ipPort = new HashMap<>();
            List<String> activatedIps = new ArrayList<>();
            if (isActivated.get(groupId))
            {
                activatedGroup = activateService.getActivatedGroup(groupId,slbId);
                if (activatedGroup!=null) {
                    for (GroupServer gs : activatedGroup.getGroupServers()){
                        ipPort.put(gs.getIp(),gs.getPort());
                        activatedIps.add(gs.getIp());
                    }
                }
            }
            List<GroupServer> groupServerList = group.getGroupServers();//groupRepository.listGroupServersByGroup(groupId);
            List<String> ips = new ArrayList<>();

            for (GroupServer gs : groupServerList){
                ipPort.put(gs.getIp(),gs.getPort());
                ips.add(gs.getIp());
            }
            for (String ip : ipPort.keySet()) {
                GroupServerStatus serverStatus = getGroupServerStatus(groupId, slbId, ip, ipPort.get(ip),allDownServers,allUpGroupServerInSlb,group);
                if (activatedIps.contains(ip)&&ips.contains(ip)){
                    serverStatus.setDiscription("Activated");
                }else if (!activatedIps.contains(ip)&&ips.contains(ip)){
                    serverStatus.setDiscription("To Activate");
                }else if (activatedIps.contains(ip)&&!ips.contains(ip)){
                    serverStatus.setDiscription("To Deactivate");
                }
                status.addGroupServerStatus(serverStatus);
            }
            res.addGroupStatus(status);
        }
        res.setTotal(res.getGroupStatuses().size());
        return res;
    }
    @Override
    public GroupStatusList getGroupStatus(List<Long> groupIds, Long slbId) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        AssertUtils.assertNotNull(slb, "slbId not found!");
        AssertUtils.assertNotEquals(0, slb.getSlbServers().size(), "Slb doesn't have any slb server!");
        int index = ThreadLocalRandom.current().nextInt(slb.getSlbServers().size());
        StatusClient client = StatusClient.getClient("http://"+slb.getSlbServers().get(index).getIp()+":"+adminServerPort.get());
        return client.getGroupStatus(groupIds,slbId);
    }

    @Override
    public GroupStatus getGroupStatus(Long groupId, Long slbId) throws Exception {
        List<Long> list = new ArrayList<>();
        list.add(groupId);
        GroupStatusList res = getGroupStatus(list,slbId);
        if (res!=null&&!res.getGroupStatuses().isEmpty())
        {
            return res.getGroupStatuses().get(0);
        }else{
            return new GroupStatus();
        }
    }


    @Override
    public GroupServerStatus getGroupServerStatus(Long groupId, Long slbId, String ip, Integer port , Set<String> allDownServers,Set<String> allUpGroupServerInSlb,Group group) throws Exception {

        GroupServerStatus groupServerStatus = new GroupServerStatus();
        groupServerStatus.setIp(ip);
        groupServerStatus.setPort(port);
        StringBuilder sb = new StringBuilder(64);
        sb.append(slbId).append("_").append(group.getGroupVirtualServers().get(0).getVirtualServer().getId()).append("_").append(groupId).append("_").append(ip);

        boolean memberUp = allUpGroupServerInSlb.contains(sb.toString());
        boolean serverUp = !allDownServers.contains(ip);
        boolean backendUp = getUpstreamStatus(groupId,ip,memberUp,serverUp);

        groupServerStatus.setServer(serverUp);
        groupServerStatus.setMember(memberUp);
        groupServerStatus.setUp(backendUp);

        return groupServerStatus;
    }


    //TODO: should include port to get accurate upstream
    private boolean getUpstreamStatus(Long groupId, String ip , boolean memberUp , boolean serverUp) throws Exception {
        UpstreamStatus upstreamStatus = LocalClient.getInstance().getUpstreamStatus();
        List<S> servers = upstreamStatus.getServers().getServer();
        String upstreamNameEndWith = "_"+groupId;
        for (S server : servers) {
            if (!server.getUpstream().endsWith(upstreamNameEndWith))
            {
                continue;
            }
            String ipPort = server.getName();
            String[] ipPorts = ipPort.split(":");
            if (ipPorts.length == 2){
                if (ipPorts[0].equals(ip)){
                    boolean flag = "up".equalsIgnoreCase(server.getStatus());
                    if (!(memberUp&&serverUp)&&flag)
                    {
                        LOGGER.error("nginx status api return status while memberUp or serverUp is down! ip:"+ip+" groupId:"+groupId);
                    }
                    return flag;
                }
            }
        }
        //Not found status from nginx , ip is mark down or health check is disable
        // return memberUp&&serverUp
        return memberUp&&serverUp;
    }
}
