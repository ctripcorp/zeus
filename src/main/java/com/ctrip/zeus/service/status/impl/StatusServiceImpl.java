package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.handler.StatusGroupServerService;
import com.ctrip.zeus.service.status.handler.StatusServerService;
import com.ctrip.zeus.service.status.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/16/2015.
 */
@Service("statusService")
public class StatusServiceImpl implements StatusService {
    @Resource
    private StatusServerService statusServerService;
    @Resource
    private StatusGroupServerService statusGroupServerService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ActivateService activateService;
    @Resource
    private GroupRepository groupRepository;

    private Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);

    @Override
    public Set<String> findAllDownServers() throws Exception {
            List<StatusServerDo> allDownServerList = statusServerService.listAllDown();
            Set<String> allDownIps = new HashSet<>();
            for (StatusServerDo d : allDownServerList) {
                allDownIps.add(d.getIp());
            }

            return allDownIps;

    }

    @Override
    public Set<String> findAllDownGroupServersBySlbId(Long slbId) throws Exception {
            Set<String> allDownAppServers = new HashSet<>();
            List<StatusGroupServerDo> allDownAppServerList = statusGroupServerService.listAllDownBySlbId(slbId);
            for (StatusGroupServerDo d : allDownAppServerList) {
                allDownAppServers.add(d.getSlbId() + "_" + d.getSlbVirtualServerId() + "_" + d.getGroupId() + "_" + d.getIp());
            }
            return allDownAppServers;
    }
    @Override
    public Set<String> findAllUpGroupServersBySlbId(Long slbId) throws Exception {
        Set<String> allUpAppServers = new HashSet<>();
        List<StatusGroupServerDo> allUpAppServerList = statusGroupServerService.listAllUpBySlbId(slbId);
        for (StatusGroupServerDo d : allUpAppServerList) {
            allUpAppServers.add(d.getSlbId() + "_" + d.getSlbVirtualServerId() + "_" + d.getGroupId() + "_" + d.getIp());
        }
        return allUpAppServers;
    }

    @Override
    public void upServer(String ip) throws Exception {

        serverStatusOperation(ip,true);
    }

    @Override
    public void downServer(String ip) throws Exception {

        serverStatusOperation(ip,false);
    }

    private void serverStatusOperation(String ip , boolean status) throws Exception {

        statusServerService.updateStatusServer(new StatusServerDo().setIp(ip).setUp(status));

        logger.info("server status up ; server ip :"+ip+",Status: "+(status?"UP":"Down"));
    }


    @Override
    public void upMember(Long slbId ,Long groupId, List<String> ips) throws Exception {
        Group group;
        if(activateService.isGroupActivated(groupId,slbId)){
            group = activateService.getActivatedGroup(groupId,slbId);
        }else {
            group = groupRepository.getById(groupId);
        }
        if (group == null){
            return;
        }
        for (String ip : ips){
            statusGroupServerService.deleteByGroupIdAndSlbIdAndIp(slbId,groupId,ip);
        }
        List<GroupVirtualServer> groupVirtualServers = group.getGroupVirtualServers();
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers){
            if (!groupVirtualServer.getVirtualServer().getSlbId().equals(slbId)){
                continue;
            }
            for (String ip : ips) {
                if (ip==null||ip.isEmpty())
                {
                    continue;
                }
                statusGroupServerService.updateStatusGroupServer(new StatusGroupServerDo().setSlbId(groupVirtualServer.getVirtualServer().getSlbId())
                        .setSlbVirtualServerId(groupVirtualServer.getVirtualServer().getId()).setGroupId(groupId).setIp(ip).setUp(true));
            }
            logger.info("[up Member]: VirtualServer:"+groupVirtualServer.toString()+"ips:"+ips.toString());
        }
    }

    @Override
    public void downMember(Long slbId ,Long groupId, List<String> ips) throws Exception {
        Group group;
        if(activateService.isGroupActivated(groupId,slbId)){
            group = activateService.getActivatedGroup(groupId,slbId);
        }else {
            group = groupRepository.getById(groupId);
        }
        if (group == null){
            return;
        }
        for (String ip : ips){
            statusGroupServerService.deleteByGroupIdAndSlbIdAndIp(slbId,groupId,ip);
        }
        List<GroupVirtualServer> groupVirtualServers = group.getGroupVirtualServers();
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers){
            if (!groupVirtualServer.getVirtualServer().getSlbId().equals(slbId)){
                continue;
            }
            for (String ip : ips) {
                if (ip==null||ip.isEmpty())
                {
                    continue;
                }
                statusGroupServerService.updateStatusGroupServer(new StatusGroupServerDo().setSlbId(groupVirtualServer.getVirtualServer().getSlbId())
                        .setSlbVirtualServerId(groupVirtualServer.getVirtualServer().getId()).setGroupId(groupId).setIp(ip).setUp(false));
            }
            logger.info("[down Member]: VirtualServer:"+groupVirtualServer.toString()+"ips:"+ips.toString());
        }
    }

    @Override
    public boolean getGroupServerStatus(Long slbId, Long groupId, String vsip) throws Exception {

        List<StatusGroupServerDo> list = statusGroupServerService.listBySlbIdAndGroupIdAndIp(slbId, groupId, vsip);
        if (list!=null&&list.size()>0)
        {
            return list.get(0).isUp();
        }
        return false;
    }

    @Override
    public boolean getServerStatus(String vsip) throws Exception {
        List<StatusServerDo> list = statusServerService.listByIp(vsip);
        if (list!=null&&list.size()>0)
        {
            return list.get(0).isUp();
        }
        return true;
    }
}
