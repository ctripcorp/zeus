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
import java.util.*;

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
    @Resource
    private StatusGroupServerDao statusGroupServerDao;

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
    /*
    * group server is up while status is 0 .
    * */
    @Override
    public Set<String> findAllUpGroupServersBySlbId(Long slbId) throws Exception {
        Set<String> allUpAppServers = new HashSet<>();

        List<StatusGroupServerDo> allUpAppServerList = statusGroupServerDao.findAllBySlbIdAndStatus( slbId , 0 , StatusGroupServerEntity.READSET_FULL);
        for (StatusGroupServerDo d : allUpAppServerList) {
            allUpAppServers.add(d.getSlbId() + "_" + d.getSlbVirtualServerId() + "_" + d.getGroupId() + "_" + d.getIp());
        }
        return allUpAppServers;
    }

    @Override
    public Set<String> findAllGroupServersBySlbIdAndStatusOffset(Long slbId, int offset , boolean status) throws Exception {
        Set<String> allUpAppServers = new HashSet<>();
        List<StatusGroupServerDo> allUpAppServerList = statusGroupServerDao.findAllBySlbId(slbId, StatusGroupServerEntity.READSET_FULL);
        int tmp = ~(1 << offset);
        for (StatusGroupServerDo statusGroupServerDo : allUpAppServerList){
            int tmpstatus = statusGroupServerDo.getStatus();
            boolean offsetStatus = tmp == (tmpstatus|tmp);
            if (offsetStatus == status){
                allUpAppServers.add(statusGroupServerDo.getSlbId() + "_" + statusGroupServerDo.getSlbVirtualServerId() + "_" + statusGroupServerDo.getGroupId() + "_" + statusGroupServerDo.getIp());
            }
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
    public void updateStatus(Long slbId, Long groupId, List<String> ips, int offset, boolean status) throws Exception {
        if (offset > 30 || offset < 0){
            throw new Exception("offset of status should be [0-30]");
        }
        Group group;
        if(activateService.isGroupActivated(groupId,slbId)){
            group = activateService.getActivatedGroup(groupId,slbId);
        }else {
            group = groupRepository.getById(groupId);
        }
        if (group == null){
            return;
        }

        List<GroupVirtualServer> groupVirtualServers = group.getGroupVirtualServers();
        List<Long> vsIds =  new ArrayList<>();
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers) {
            if (!groupVirtualServer.getVirtualServer().getSlbId().equals(slbId)) {
                continue;
            }
            vsIds.add(groupVirtualServer.getVirtualServer().getId());
        }

        for (GroupVirtualServer groupVirtualServer : groupVirtualServers){
            if (!groupVirtualServer.getVirtualServer().getSlbId().equals(slbId)){
                continue;
            }
            for (String ip : ips) {
                if (ip==null||ip.isEmpty())
                {
                    continue;
                }
                statusGroupServerDao.deleteByGroupIdAndSlbIdAndIpAndExSlbVirtualServerIds(new StatusGroupServerDo()
                        .setSlbId(slbId)
                        .setGroupId(groupId)
                        .setIp(ip).setExVirtualServerIds(vsIds.toArray(new Long[]{})));
                StatusGroupServerDo defaultData = new StatusGroupServerDo();
                defaultData.setSlbId(slbId)
                        .setSlbVirtualServerId(groupVirtualServer.getVirtualServer().getId())
                        .setGroupId(groupId)
                        .setIp(ip)
                        .setCreatedTime(new Date())
                        .setStatus(2);
                statusGroupServerDao.insert(defaultData);

                StatusGroupServerDo data = new StatusGroupServerDo();
                data.setSlbId(slbId)
                        .setSlbVirtualServerId(groupVirtualServer.getVirtualServer().getId())
                        .setGroupId(groupId)
                        .setIp(ip)
                        .setCreatedTime(new Date());
                int reset = ~(1 << offset);
                int updatestatus = (status?0:1)<<offset;
                data.setReset(reset).setStatus(updatestatus);
                statusGroupServerDao.updateStatus(data);
            }
            logger.info("[update status]: VirtualServer:"+groupVirtualServer.toString()+"ips:"+ips.toString());
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
