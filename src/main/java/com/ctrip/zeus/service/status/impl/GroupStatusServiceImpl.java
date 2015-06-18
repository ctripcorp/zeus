package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.client.StatusClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.S;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

    private long currentSlbId = -1L;

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

        List<GroupSlb> groupSlbs = slbRepository.listGroupSlbsBySlb(slbId);
        for (GroupSlb groupSlb : groupSlbs) {
            GroupStatus appStatus = getGroupStatus(groupSlb.getGroupId(), groupSlb.getSlbId());
            result.add(appStatus);
        }
        return result;
    }

    @Override
    public List<GroupStatus> getGroupStatus(Long groupId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        List<Slb> slbList = slbRepository.listByGroups(new Long[]{groupId});
        for (Slb slb : slbList) {
            result.add(getGroupStatus(groupId, slb.getId()));
        }
        return result;
    }
    @Override
    public GroupStatus getLocalGroupStatus(Long groupId , Long slbId) throws Exception
    {
        Slb slb = slbRepository.getById(slbId);
        Group group = groupRepository.getById(groupId);
        AssertUtils.isNull(group,"group Id not found!");
        AssertUtils.isNull(slb,"slb Id not found!");

        GroupStatus status = new GroupStatus();
        status.setGroupId(groupId);
        status.setSlbId(slbId);
        status.setGroupName(group.getName());
        status.setSlbName(slb.getName());

        List<GroupServer> groupServerList = groupRepository.listGroupServersByGroup(groupId);
        for (GroupServer groupServer : groupServerList) {
            GroupServerStatus serverStatus = getGroupServerStatus(groupId, slbId, groupServer.getIp(), groupServer.getPort());
            status.addGroupServerStatus(serverStatus);
        }
        return status;
    }
    @Override
    public GroupStatus getGroupStatus(Long groupId, Long slbId) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        AssertUtils.isNull(slb,"slbId not found!");
        AssertUtils.assertNotEquels(0,slb.getSlbServers().size(),"Slb doesn't have any slb server!");
        StatusClient client = StatusClient.getClient("http://"+slb.getSlbServers().get(0).getIp()+":"+adminServerPort.get());
        return client.getGroupStatus(groupId,slbId);
    }

    @Override
    public GroupServerStatus getGroupServerStatus(Long groupId, Long slbId, String ip, Integer port) throws Exception {

        GroupServerStatus groupServerStatus = new GroupServerStatus();
        groupServerStatus.setIp(ip);
        groupServerStatus.setPort(port);


        boolean memberUp = statusService.getGroupServerStatus(slbId, groupId, ip);
        boolean serverUp = statusService.getServerStatus(ip);
        boolean backendUp = getUpstreamStatus(groupId,ip);

        groupServerStatus.setServer(serverUp);
        groupServerStatus.setMember(memberUp);
        groupServerStatus.setUp(backendUp);

        return groupServerStatus;
    }

    //TODO: should include port to get accurate upstream
    private boolean getUpstreamStatus(Long groupId, String ip) throws Exception {
        UpstreamStatus upstreamStatus = LocalClient.getInstance().getUpstreamStatus();
        List<S> servers = upstreamStatus.getServers().getServer();
        String upstreamNameEndWith = "_"+groupRepository.getById(groupId).getName();
        for (S server : servers) {
            if (!server.getUpstream().endsWith(upstreamNameEndWith))
            {
                continue;
            }
            String ipPort = server.getName();
            String[] ipPorts = ipPort.split(":");
            if (ipPorts.length == 2){
                if (ipPorts[0].equals(ip)){
                    return "up".equalsIgnoreCase(server.getStatus());
                }
            }
        }
        return false;
    }
    private boolean isCurrentSlb(Long slbId) throws Exception {
        if (currentSlbId < 0)
        {
            String ip = com.ctrip.zeus.util.S.getIp();
            Slb slb = slbRepository.getBySlbServer(ip);
            if (slb != null )
            {
                currentSlbId = slb.getId();
            }
        }
        return slbId.equals(currentSlbId);
    }
}
