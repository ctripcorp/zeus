package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dao.entity.SlbGroupServerStatus;
import com.ctrip.zeus.dao.entity.SlbGroupServerStatusExample;
import com.ctrip.zeus.dao.entity.SlbServerStatus;
import com.ctrip.zeus.dao.entity.SlbServerStatusExample;
import com.ctrip.zeus.dao.mapper.SlbGroupServerStatusMapper;
import com.ctrip.zeus.dao.mapper.SlbServerStatusMapper;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupServer;
import com.ctrip.zeus.model.status.UpdateStatusItem;
import com.ctrip.zeus.service.SmartGroupServerStatusRMapper;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.ModelStatusMapping;
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
    private SlbServerStatusMapper slbServerStatusMapper;
    @Resource
    private StatusOffset statusOffset;
    @Resource
    private SlbGroupServerStatusMapper slbGroupServerStatusMapper;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private SmartGroupServerStatusRMapper smartGroupServerStatusRMapper;

    private final static int OFFSET_LENGTH = 5;

    private Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);

    @Override
    public Set<String> findAllDownServers() throws Exception {
        List<SlbServerStatus> allDownServerList = slbServerStatusMapper.selectByExample(new SlbServerStatusExample().createCriteria().andUpEqualTo(false).example());
        Set<String> allDownIps = new HashSet<>();
        for (SlbServerStatus d : allDownServerList) {
            allDownIps.add(d.getIp());
        }
        return allDownIps;
    }

    /*
     * group server is up while status is 0 .
     * */
    @Override
    public Map<String, List<Boolean>> fetchGroupServerStatus(Long[] groupIds) throws Exception {
        Map<String, List<Boolean>> result = new HashMap<>();
        if (groupIds == null || groupIds.length == 0) return result;
        List<SlbGroupServerStatus> groupServerStatusDos = smartGroupServerStatusRMapper.selectByExample(new SlbGroupServerStatusExample().createCriteria().andGroupIdIn(Arrays.asList(groupIds)).example());
        int tmp = 1;
        for (SlbGroupServerStatus s : groupServerStatusDos) {
            int tmpStatus = s.getStatus();
            /*
             * offset == 0 is true ; offset == 1 is false.
             * */
            List<Boolean> offset = new ArrayList<>(OFFSET_LENGTH);
            for (int i = 0; i < OFFSET_LENGTH; i++) {
                offset.add(offset.size(), 0 == (tmpStatus & tmp));
                tmpStatus = tmpStatus >> 1;
            }
            result.put(s.getGroupId() + "_" + s.getIp(), offset);
        }
        return result;

    }

    @Override
    public void upServer(String ip) throws Exception {
        serverStatusOperation(ip, true);
    }

    @Override
    public void downServer(String ip) throws Exception {
        serverStatusOperation(ip, false);
    }

    private void serverStatusOperation(String ip, boolean status) throws Exception {
        slbServerStatusMapper.insertUpdate(SlbServerStatus.builder().ip(ip).up(status).createdTime(new Date()).build());
        logger.info("Mybatis: server status up ; server ip :" + ip + ",Status: " + (status ? "UP" : "Down"));
    }

    @Override
    public void updateStatus(Long groupId, List<String> ips, int offset, boolean status) throws Exception {
        if (offset > OFFSET_LENGTH || offset < 0) {
            throw new Exception("offset of status should be [0-" + OFFSET_LENGTH + "]");
        }
        for (String ip : ips) {
            if (ip == null || ip.isEmpty()) {
                continue;
            }
            int reset = ~(1 << offset);
            int updatestatus = (status ? 0 : 1) << offset;
            slbGroupServerStatusMapper.updateStatus(SlbGroupServerStatus.builder().groupId(groupId).ip(ip).status(updatestatus).createdTime(new Date()).build(), reset);
        }
    }

    @Override
    public void updateStatus(List<UpdateStatusItem> items) throws Exception {
        if (items == null || items.size() == 0) return;

        List<Map<String, Object>> updateDatas = new ArrayList<>();
        for (UpdateStatusItem item : items) {
            if (item.getOffset() > OFFSET_LENGTH || item.getOffset() < 0) {
                throw new Exception("offset of status should be [0-" + OFFSET_LENGTH + "]");
            }

            for (String ip : item.getIpses()) {
                if (ip == null || ip.isEmpty()) {
                    continue;
                }
                int reset = ~(1 << item.getOffset());
                int updatestatus = (item.isUp() ? 0 : 1) << item.getOffset();
                SlbGroupServerStatus data = SlbGroupServerStatus.builder().groupId(item.getGroupId()).ip(ip).createdTime(new Date()).status(updatestatus).build();
                Map<String, Object> t = new HashMap<>();
                t.put("status", data);
                t.put("reset", reset);
                updateDatas.add(t);
            }
        }
        slbGroupServerStatusMapper.batchUpdateStatus(updateDatas);
    }

    @Override
    public boolean getServerStatus(String vsip) throws Exception {
        List<SlbServerStatus> list = slbServerStatusMapper.selectByExampleSelective(new SlbServerStatusExample().createCriteria().andIpEqualTo(vsip).example(), SlbServerStatus.Column.up);
        if (list != null && list.size() > 0) {
            return list.get(0).getUp();
        }
        return true;
    }

    @Override
    public void groupServerStatusInit(Long groupId, Long[] vsIds, String[] ips) throws Exception {
        /*
         * only called in group resource api
         * */
        for (String ip : ips) {
            slbGroupServerStatusMapper.insertUpdate(SlbGroupServerStatus.builder().
                    groupId(groupId).
                    ip(ip).
                    status(statusOffset.getDefaultStatus()).
                    createdTime(new Date()).
                    build());
        }
    }

    @Override
    public void cleanGroupServerStatus(Long groupId) throws Exception {
        slbGroupServerStatusMapper.deleteByExample(new SlbGroupServerStatusExample().createCriteria().andGroupIdEqualTo(groupId).example());
    }

    @Override
    public void cleanGroupServerStatus(Long groupId, String[] ip) throws Exception {
        if (ip == null || ip.length == 0) return;
        slbGroupServerStatusMapper.deleteByExample(new SlbGroupServerStatusExample().createCriteria().andGroupIdEqualTo(groupId).andIpIn(Arrays.asList(ip)).example());
    }

    @Override
    public void cleanDisabledGroupServerStatus(Long groupId) throws Exception {
        if (groupId == null) return;
        List<SlbGroupServerStatus> groupServerStatusDos = slbGroupServerStatusMapper.selectByExample(new SlbGroupServerStatusExample().createCriteria().andGroupIdEqualTo(groupId).example());

        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(new Long[]{groupId});
        Set<String> members = new HashSet<>();
        Group online = groupMap.getOnlineMapping().get(groupId);
        Group offline = groupMap.getOfflineMapping().get(groupId);
        if (online != null && online.getGroupServers() != null) {
            for (GroupServer gs : online.getGroupServers()) {
                members.add(gs.getIp());
            }
        }
        if (offline != null && offline.getGroupServers() != null) {
            for (GroupServer gs : offline.getGroupServers()) {
                members.add(gs.getIp());
            }
        }
        List<String> disabledIps = new ArrayList<>();
        for (SlbGroupServerStatus gssd : groupServerStatusDos) {
            if (!members.contains(gssd.getIp())) {
                disabledIps.add(gssd.getIp());
            }
        }
        if (disabledIps.size() > 0) {
            cleanGroupServerStatus(groupId, disabledIps.toArray(new String[disabledIps.size()]));
        }
    }
}
