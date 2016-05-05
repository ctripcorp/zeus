package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
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
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/11/11.
 */
@Service("healthCheckStatusService")
public class HealthCheckStatusServiceImpl implements HealthCheckStatusService {
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private StatusService statusService;

    private static int count = 0;
    private static DynamicIntProperty invalidInterval = DynamicPropertyFactory.getInstance().getIntProperty("health.check.status.invalid.interval", 300000);
    private Logger logger = LoggerFactory.getLogger(HealthCheckStatusServiceImpl.class);
    private static DynamicIntProperty riseOrFailMin = DynamicPropertyFactory.getInstance().getIntProperty("health.check.status.rise-fail.min", 30);
    private static DynamicBooleanProperty updateAlways = DynamicPropertyFactory.getInstance().getBooleanProperty("health.check.status.always.update", false);


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
        logger.info("[HealthCheckStatusService] start fetch status from nginx server.");
        UpstreamStatus upstreamStatus = LocalClient.getInstance().getUpstreamStatus();
        logger.info("[HealthCheckStatusService] finish fetch status from nginx server.");
        if (upstreamStatus == null || upstreamStatus.getServers() == null || upstreamStatus.getServers().getServer() == null) {
            logger.info("[HeathCheckFetch] Null status data for host ip [" + hostIp + "].");
            return;
        }

        List<Item> servers = upstreamStatus.getServers().getServer();

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesBySlbIds(slbId);
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByVsIds(vsMap.getOnlineMapping().keySet().toArray(new Long[]{}));

        logger.info("[HealthCheckStatusService] start check server items.");

        List<UpdateStatusItem> updateStatusItems = new ArrayList<>();
        for (Item item : servers) {
            if (item.getStatus() == null) continue;
            if (item.getStatus().trim().equals("up")) {
                if (updateAlways.get() || item.getRise() < riseOrFailMin.get()) {
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
                    String[] ipPort = item.getName().split(":");
                    if (ipPort.length != 2) {
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
                                    .setUp(true)
                                    .setVsId(gvs.getVirtualServer().getId())
                                    .addIps(ip));
                        }
                    }
                }
            } else if (item.getStatus().trim().equals("down")) {
                if (updateAlways.get() || item.getFall() < riseOrFailMin.get()) {
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
                    String[] ipPort = item.getName().split(":");
                    if (ipPort.length != 2) {
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
        }
        if (updateStatusItems.size() > 0) {
                logger.info("[HealthCheckStatusService] start update status. size : " + updateStatusItems.size());
                statusService.updateStatus(updateStatusItems);
                logger.info("[HealthCheckStatusService] end update status. size : " + updateStatusItems.size());
        }
    }
}
