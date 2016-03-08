package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.dal.core.StatusHealthCheckDao;
import com.ctrip.zeus.dal.core.StatusHealthCheckDo;
import com.ctrip.zeus.dal.core.StatusHealthCheckEntity;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.Item;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.status.HealthCheckStatusService;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.status.entity.UpdateStatusItem;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fanqq on 2015/11/11.
 */
@Service("healthCheckStatusService")
public class HealthCheckStatusServiceImpl implements HealthCheckStatusService {
    @Resource
    private StatusHealthCheckDao statusHealthCheckDao;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private StatusService statusService;

    private static int count = 0;
    private static DynamicIntProperty invalidInterval = DynamicPropertyFactory.getInstance().getIntProperty("health.check.status.invalid.interval", 300000);
    private Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);
    private final int RISE_FAIL_MIN = 10;

    @Override
    public void freshHealthCheckStatus() throws Exception {
        String hostIp = S.getIp();
        Long[] slbIds = entityFactory.getSlbIdsByIp(hostIp, SelectionMode.ONLINE_EXCLUSIVE);
        Long slbId = null;
        if (slbIds != null && slbIds.length == 1) {
            slbId = slbIds[0];
        } else {
            logger.warn("Not found relative online slb for host ip [" + hostIp + "].");
        }

        UpstreamStatus upstreamStatus = LocalClient.getInstance().getUpstreamStatus();

        if (upstreamStatus == null || upstreamStatus.getServers() == null || upstreamStatus.getServers().getServer() == null) {
            logger.info("[HeathCheckFetch] Null status data for host ip [" + hostIp + "].");
            return;
        }

        List<Item> servers = upstreamStatus.getServers().getServer();

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesBySlbIds(slbId);
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByVsIds(vsMap.getOnlineMapping().keySet().toArray(new Long[]{}));

        List<UpdateStatusItem> updateStatusItems = new ArrayList<>();
        for (Item item : servers) {
            if (item.getStatus() == null) continue;
            if (item.getStatus().trim().equals("up")) {
                if (item.getRise() < RISE_FAIL_MIN) {
                    //update
                    String[] tmp = item.getUpstream().split("_");
                    if (tmp.length != 2) {
                        logger.warn("[FreshHealthCheckStatus] Skipped invalidate upstream name.");
                        continue;
                    }
                    Long groupId = Long.parseLong(tmp[1]);
                    Group group = groupMap.getOnlineMapping().get(groupId);
                    if (group == null) {
                        logger.warn("[FreshHealthCheckStatus] Skipped invalidate upstream name.");
                        continue;
                    }
                    String [] ipPort = item.getName().split(":");
                    if (ipPort.length != 2){
                        logger.warn("[FreshHealthCheckStatus] Skipped invalidate ip port.");
                        continue;
                    }
                    String ip = ipPort[1];
                    for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                        if (vsMap.getOnlineMapping().containsKey(gvs.getVirtualServer().getId())) {
                            updateStatusItems.add(new UpdateStatusItem()
                                    .setSlbId(slbId)
                                    .setOffset(StatusOffset.HEALTH_CHECK)
                                    .setGroupId(groupId)
                                    .setUp(true)
                                    .setVsId(gvs.getVirtualServer().getId())
                                    .addIps(ip));
                        }
                    }
                }
            } else if (item.getStatus().trim().equals("down")) {
                if (item.getFall() < RISE_FAIL_MIN) {
                    //update
                    String[] tmp = item.getUpstream().split("_");
                    if (tmp.length != 2) {
                        logger.warn("[FreshHealthCheckStatus] Skipped invalidate upstream name.");
                        continue;
                    }
                    Long groupId = Long.parseLong(tmp[1]);
                    Group group = groupMap.getOnlineMapping().get(groupId);
                    if (group == null) {
                        logger.warn("[FreshHealthCheckStatus] Skipped invalidate upstream name.GroupId:" + groupId + ";SlbId:" + slbId);
                        continue;
                    }
                    String [] ipPort = item.getName().split(":");
                    if (ipPort.length != 2){
                        logger.warn("[FreshHealthCheckStatus] Skipped invalidate ip port.");
                        continue;
                    }
                    String ip = ipPort[0];
                    for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                        if (vsMap.getOnlineMapping().containsKey(gvs.getVirtualServer().getId())) {
                            updateStatusItems.add(new UpdateStatusItem()
                                    .setSlbId(slbId)
                                    .setOffset(StatusOffset.HEALTH_CHECK)
                                    .setGroupId(groupId)
                                    .setUp(false)
                                    .setVsId(gvs.getVirtualServer().getId())
                                    .addIps(ip));
                        }
                    }
                }
            }
            if (updateStatusItems.size() > 0) {
                statusService.updateStatus(updateStatusItems);
            }
        }
    }
}
