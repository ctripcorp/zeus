package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.StatusOffset;
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
    private SlbRepository slbRepository;
    @Resource
    private ActivateService activateService;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private StatusGroupServerDao statusGroupServerDao;
    @Resource
    private StatusServerDao statusServerDao;
    @Resource
    private StatusOffset statusOffset;


    private Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);

    @Override
    public Set<String> findAllDownServers() throws Exception {
            List<StatusServerDo> allDownServerList = statusServerDao.findAllByIsUp(false, StatusServerEntity.READSET_FULL);
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
    public Set<String> fetchGroupServersByVsIdsAndStatusOffset(Long[] vsIds, int offset , boolean status) throws Exception {
        Set<String> result = new HashSet<>();
        List<StatusGroupServerDo> statusGroupServerDos= statusGroupServerDao.findAllBySlbVirtualServerIds(vsIds, StatusGroupServerEntity.READSET_FULL);
        int tmp = ~(1 << offset);
        for (StatusGroupServerDo statusGroupServerDo : statusGroupServerDos){
            int tmpStatus = statusGroupServerDo.getStatus();
            /*
            * tmp == (tmpStatus|tmp) : 1. true  offsetStatus = true(0)  2. false: offsetStatus = false(1)
            * offsetStatus equals (tmp == (tmpStatus|tmp))
            * */
            boolean offsetStatus = (tmp == (tmpStatus|tmp));
            if (status == offsetStatus){
                result.add(statusGroupServerDo.getSlbVirtualServerId() + "_" + statusGroupServerDo.getGroupId() + "_" + statusGroupServerDo.getIp());
            }
        }
        return result;
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
        statusServerDao.insert(new StatusServerDo().setIp(ip).setUp(status).setCreatedTime(new Date()));
        logger.info("server status up ; server ip :"+ip+",Status: "+(status?"UP":"Down"));
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
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers){
            if (!groupVirtualServer.getVirtualServer().getSlbId().equals(slbId)){
                continue;
            }
            for (String ip : ips) {
                if (ip==null||ip.isEmpty())
                {
                    continue;
                }
                StatusGroupServerDo data = new StatusGroupServerDo();
                data.setSlbVirtualServerId(groupVirtualServer.getVirtualServer().getId())
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
    public boolean getServerStatus(String vsip) throws Exception {
        List<StatusServerDo> list = statusServerDao.findAllByIp(vsip, StatusServerEntity.READSET_FULL);
        if (list!=null&&list.size()>0)
        {
            return list.get(0).isUp();
        }
        return true;
    }

    @Override
    public Set<Long> findGroupIdByIp(String ip) throws Exception {
        Set<Long> result = new HashSet<>();
        List<StatusGroupServerDo> list = statusGroupServerDao.findAllByIp(ip,StatusGroupServerEntity.READSET_FULL);
        if (list == null){
            return result;
        }
        for (StatusGroupServerDo statusGroupServerDo : list){
            result.add(statusGroupServerDo.getGroupId());
        }
        return result;
    }

    @Override
    public void groupServerStatusInit(Long groupId, Long[] vsIds, String[] ips) throws Exception {
        /*
        * only called in group resource api
        * */
        for (Long vsId : vsIds){
            for (String ip : ips){
                statusGroupServerDao.insert(new StatusGroupServerDo()
                        .setGroupId(groupId)
                        .setSlbVirtualServerId(vsId)
                        .setIp(ip)
                        .setIsActivated(false)
                        .setStatus(statusOffset.getDefaultStatus())
                        .setCreatedTime(new Date()));
            }
        }
        statusGroupServerDao.deleteByGroupIdAndVirtualServerIdsAndExpIpsAndIsActivated(new StatusGroupServerDo()
                .setGroupId(groupId)
                .setVirtualServerIds(vsIds)
                .setIsActivated(false)
                .setIps(ips));
     }

    @Override
    public void activateGroupStatusInit(Group group, Long vsId) throws Exception {
        Long groupId = group.getId();
        List<String> ips = new ArrayList<>();
        for (GroupServer groupServer : group.getGroupServers()){
            ips.add(groupServer.getIp());
        }
        for (String ip : ips){
            statusGroupServerDao.insertOrUpdateIsActivated(new StatusGroupServerDo()
                    .setGroupId(groupId)
                    .setSlbVirtualServerId(vsId)
                    .setIp(ip)
                    .setIsActivated(true)
                    .setStatus(statusOffset.getDefaultStatus())
                    .setCreatedTime(new Date()));
        }
        statusGroupServerDao.deleteByGroupIdAndVirtualServerIdsAndExpIpsAndIsActivated(new StatusGroupServerDo()
                .setGroupId(groupId)
                .setVirtualServerIds(new Long[]{vsId})
                .setIsActivated(true)
                .setIps(ips.toArray(new String[]{})));
    }
}
