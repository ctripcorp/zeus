package com.ctrip.zeus.service.metainfo.impl;

import com.ctrip.zeus.model.TrafficPoint;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.metainfo.AbstractStatisticsInfo;
import com.ctrip.zeus.service.traffic.TrafficMonitorService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2016/7/29.
 */
//todo: adapt new statstics impliment for open source
@Service("statisticsInfoImpl")
public class StatisticsInfoImpl extends AbstractStatisticsInfo {

    private final String SLB_ID_TAG_KEY = "slb_id";

    private final String VS_ID_TAG_KEY = "vsid";

    private final String GROUP_ID_TAG_KEY = "group_id";

    private final String SERVER_TAG_KEY = "slb_server";

    private final String SBU_TAG_KEY = "sbu";

    private final String APP_TAG_KEY = "group_appid";

    @Resource
    private TrafficMonitorService trafficMonitorService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void updateSLBQps(Map<Long, SlbMeta> slbMap) {
        Map<String, String> queryTags = Maps.newHashMap();
        for (Map.Entry<Long, SlbMeta> entry : slbMap.entrySet()) {
            queryTags.put(SLB_ID_TAG_KEY, String.valueOf(entry.getKey()));
            Double qps = getQpsByTags(queryTags);
            if (qps == null) {
                logger.warn("cannot get qps of slb. slb id: " + entry.getKey());
            }
            entry.getValue().setQps(qps);
        }
    }

    @Override
    public void updateVsQps(Map<Long, VsMeta> vsMap) {
        Map<String, String> queryTags = Maps.newHashMap();
        for (Map.Entry<Long, VsMeta> entry : vsMap.entrySet()) {
            queryTags.put(VS_ID_TAG_KEY, String.valueOf(entry.getKey()));
            Double qps = getQpsByTags(queryTags);
            if (qps == null) {
                logger.warn("cannot get qps of vs. vs id: " + entry.getKey());
            }
            entry.getValue().setQps(qps);
        }
    }

    @Override
    public void updateGroupQps(Map<Long, GroupMeta> groupMap) {
        Map<String, String> queryTags = Maps.newHashMap();
        for (Map.Entry<Long, GroupMeta> entry : groupMap.entrySet()) {
            queryTags.put(GROUP_ID_TAG_KEY, String.valueOf(entry.getKey()));
            Double qps = getQpsByTags(queryTags);
            if (qps == null) {
                logger.warn("cannot get qps of group. group id: " + entry.getKey());
            }
            entry.getValue().setQps(qps);
        }
    }

    @Override
    public void updateHostQps(Map<String, SlbServerQps> host) {
        Map<String, String> queryTags = Maps.newHashMap();
        for (Map.Entry<String, SlbServerQps> entry : host.entrySet()) {
            queryTags.put(SERVER_TAG_KEY, entry.getKey());
            Double qps = getQpsByTags(queryTags);
            if (qps == null) {
                logger.warn("cannot get qps of server. server ip: " + entry.getKey());
            }
            entry.getValue().setQps(qps);
        }
    }

    @Override
    public void updateSbuQps(Map<String, SbuMeta> sbuMap) {
        Map<String, String> queryTags = Maps.newHashMap();
        for (Map.Entry<String, SbuMeta> entry : sbuMap.entrySet()) {
            queryTags.put(SBU_TAG_KEY, entry.getKey());
            Double qps = getQpsByTags(queryTags);
            if (qps == null) {
                logger.warn("cannot get qps of sbu. sbu: " + entry.getKey());
            }
            entry.getValue().setQps(qps);
        }
    }

    @Override
    public void updateAppQps(Map<String, AppMeta> appMetaMap) {
        Map<String, String> queryTags = Maps.newHashMap();
        for (Map.Entry<String, AppMeta> entry : appMetaMap.entrySet()) {
            queryTags.put(APP_TAG_KEY, entry.getKey());
            Double qps = getQpsByTags(queryTags);
            if (qps == null) {
                logger.warn("cannot get qps of sbu. sbu: " + entry.getKey());
            }
            entry.getValue().setQps(qps);
        }
    }

    private Double getQpsByTags(Map<String, String> queryTags) {
        List<TrafficPoint> trafficPoints = trafficMonitorService.query(queryTags, null);
        if (trafficPoints != null && trafficPoints.size() >= 1) {
            return trafficPoints.get(0).getQps();
        }

        return null;
    }

    @Override
    public HashMap<String, Object> getAllDashboardMeta(String op, String domain, String ip, String date, String type, String catEnv) throws Exception {
        return null;
    }

    @Override
    public HashMap<String, Object> getApiDashboardMeta(String op, String domain, String ip, String date, String type, String catEnv) throws Exception {
        return null;
    }
}
