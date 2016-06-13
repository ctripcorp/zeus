package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.status.entity.UpdateStatusItem;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
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
    private StatusGroupServerDao statusGroupServerDao;
    @Resource
    private StatusServerDao statusServerDao;
    @Resource
    private StatusOffset statusOffset;
    @Resource
    private GroupServerStatusDao groupServerStatusDao;

    private final int OFFSET_LENGTH = 5;

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
    public Map<String, List<Boolean>> fetchGroupServerStatus(Long[] groupIds) throws Exception {
        Map<String, List<Boolean>> result = new HashMap<>();
        List<GroupServerStatusDo> groupServerStatusDos = groupServerStatusDao.findAllByGroupIds(groupIds, GroupServerStatusEntity.READSET_FULL);
        int tmp = 1;
        for (GroupServerStatusDo s : groupServerStatusDos) {
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
        statusServerDao.insert(new StatusServerDo().setIp(ip).setUp(status).setCreatedTime(new Date()));
        logger.info("server status up ; server ip :" + ip + ",Status: " + (status ? "UP" : "Down"));
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
            GroupServerStatusDo data = new GroupServerStatusDo();
            data.setGroupId(groupId);
            data.setIp(ip);
            data.setCreatedTime(new Date());
            int reset = ~(1 << offset);
            int updatestatus = (status ? 0 : 1) << offset;
            data.setReset(reset).setStatus(updatestatus);
            groupServerStatusDao.updateStatus(data);
        }
    }

    @Override
    public void updateStatus(List<UpdateStatusItem> items) throws Exception {
        List<GroupServerStatusDo> updateDatas = new ArrayList<>();
        for (UpdateStatusItem item : items) {
            if (item.getOffset() > OFFSET_LENGTH || item.getOffset() < 0) {
                throw new Exception("offset of status should be [0-" + OFFSET_LENGTH + "]");
            }
            for (String ip : item.getIpses()) {
                if (ip == null || ip.isEmpty()) {
                    continue;
                }
                GroupServerStatusDo data = new GroupServerStatusDo();
                data.setGroupId(item.getGroupId());
                data.setIp(ip);
                data.setCreatedTime(new Date());
                int reset = ~(1 << item.getOffset());
                int updatestatus = (item.isUp() ? 0 : 1) << item.getOffset();
                data.setReset(reset).setStatus(updatestatus);
                updateDatas.add(data);
            }
        }
        groupServerStatusDao.batchUpdateStatus(updateDatas.toArray(new GroupServerStatusDo[]{}));
    }


    @Override
    public boolean getServerStatus(String vsip) throws Exception {
        List<StatusServerDo> list = statusServerDao.findAllByIp(vsip, StatusServerEntity.READSET_FULL);
        if (list != null && list.size() > 0) {
            return list.get(0).isUp();
        }
        return true;
    }

    @Override
    public void groupServerStatusInit(Long groupId, Long[] vsIds, String[] ips) throws Exception {
        /*
        * only called in group resource api
        * */
        for (String ip : ips) {
            groupServerStatusDao.insert(new GroupServerStatusDo()
                    .setGroupId(groupId)
                    .setIp(ip)
                    .setStatus(statusOffset.getDefaultStatus())
                    .setCreatedTime(new Date()));
        }

    }

    @Override
    public void cleanGroupServerStatus(Long groupId) throws Exception {
        groupServerStatusDao.deleteByGroupId(new GroupServerStatusDo().setGroupId(groupId));

    }
}
