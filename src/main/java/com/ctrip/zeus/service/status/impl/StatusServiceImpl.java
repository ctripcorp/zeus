package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.handler.StatusGroupServerService;
import com.ctrip.zeus.service.status.handler.StatusServerService;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.util.AssertUtils;
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
    private SlbRepository slbClusterRepository;

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
    public void upMember(Long groupId, String ip) throws Exception {

        List<GroupSlb> appslblist = slbClusterRepository.listGroupSlbsByGroups(new Long[]{groupId});
        if (appslblist==null||appslblist.size()==0)
        {
            logger.warn("[up member]: Can not find appslb by GroupId! GroupId: "+groupId);
            AssertUtils.isNull(appslblist,"[up member]: Can not find GroupSlb by GroupID! Please Check the Configuration or param again!");
            return;
        }

        dataAdjust(appslblist,groupId);

        for (GroupSlb d : appslblist)
        {
            statusGroupServerService.updateStatusGroupServer(new StatusGroupServerDo().setSlbId(d.getSlbId())
                    .setSlbVirtualServerId(d.getVirtualServer().getId()).setGroupId(groupId).setIp(ip).setUp(true));
            logger.info("[up Member]: AppSlb:"+d.toString());
        }
    }

    @Override
    public void downMember(Long groupId, String ip) throws Exception {

        List<GroupSlb> appslblist = slbClusterRepository.listGroupSlbsByGroups(new Long[]{groupId});
        if (appslblist==null||appslblist.size()==0)
        {
            logger.warn("[down member]: Can not find appslb by GroupId! GroupId: "+groupId);
            AssertUtils.isNull(appslblist,"[up member]: Can not find GroupSlb by GroupID! Please Check the Configuration or param again!");
            return;
        }

        dataAdjust(appslblist,groupId);

        for (GroupSlb d : appslblist)
        {
            statusGroupServerService.updateStatusGroupServer(new StatusGroupServerDo().setSlbId(d.getSlbId())
                    .setSlbVirtualServerId(d.getVirtualServer().getId()).setGroupId(groupId).setIp(ip).setUp(false));
            logger.info("[down Member]: AppSlb:"+d.toString());
        }

    }

    @Override
    public boolean getGroupServerStatus(Long slbId, Long groupId, String vsip) throws Exception {

        List<StatusGroupServerDo> list = statusGroupServerService.listBySlbIdAndGroupIdAndIp(slbId, groupId, vsip);
        if (list!=null&&list.size()>0)
        {
            return list.get(0).isUp();
        }
        return true;
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

    private boolean dataAdjust(List<GroupSlb> list ,Long groupId)throws Exception{
        Set<String> groupvs =new HashSet<>();
        Set<Long> slbIds = new HashSet<>();
        for (GroupSlb p : list)
        {
            groupvs.add(p.getGroupId().toString()+p.getSlbId()+p.getVirtualServer().getId());
            slbIds.add(p.getSlbId());
        }

        List<StatusGroupServerDo> statuslist = new ArrayList<>();
        for (Long slbId:slbIds){
            List<StatusGroupServerDo> tmplist = statusGroupServerService.listBySlbIdAndGroupId(slbId, groupId);
            if (tmplist!=null)
            {
                statuslist.addAll(tmplist);
            }
        }

        for (StatusGroupServerDo d:statuslist)
        {
            if(!groupvs.contains(String.valueOf(d.getGroupId())+d.getSlbId()+d.getSlbVirtualServerId()))
            {
                statusGroupServerService.deleteBySlbIdAndGroupIdAndVsId(d.getSlbId(), d.getGroupId(), d.getSlbVirtualServerId());
                logger.info("[status adjust]remove StatusAppServer :"+d.toString());
            }
        }

        return true;
    }
}
