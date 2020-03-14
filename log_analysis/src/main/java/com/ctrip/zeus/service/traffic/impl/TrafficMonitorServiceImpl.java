package com.ctrip.zeus.service.traffic.impl;

import com.ctrip.zeus.logstats.tools.ReqStats;
import com.ctrip.zeus.logstats.tools.StatsKey;
import com.ctrip.zeus.model.TrafficEntity;
import com.ctrip.zeus.model.TrafficPoint;
import com.ctrip.zeus.service.traffic.TrafficMonitorService;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

@Service("trafficMonitorService")
public class TrafficMonitorServiceImpl implements TrafficMonitorService {

    private TrafficEntity trafficEntity = null;
    private final static String GROUP_BY_TOTAL = "Total";
    private DynamicIntProperty inertval = DynamicPropertyFactory.getInstance().getIntProperty("traffic.monitor.interval", 60);


    @Override
    public void refresh(ConcurrentMap<StatsKey, ReqStats> statsKeyStatsConcurrentMap) {
        ArrayList<Map<String, String>> tagsArray = new ArrayList<>(statsKeyStatsConcurrentMap.size());
        ArrayList<ReqStats> reqStats = new ArrayList<>(statsKeyStatsConcurrentMap.size());
        Map<String, List<Integer>> tagMap = new HashMap<>();
        int i = 0;
        for (Map.Entry<StatsKey, ReqStats> entry : statsKeyStatsConcurrentMap.entrySet()) {
            StatsKey k = entry.getKey();
            ReqStats s = entry.getValue();
            Map<String, String> tags = k.getTags();
            reqStats.add(s);
            tagsArray.add(tags);
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                String key = tag.getKey() + "-" + tag.getValue();
                tagMap.putIfAbsent(key, new ArrayList<>());
                tagMap.get(key).add(i);
            }
            ++i;
        }
        trafficEntity = new TrafficEntity(tagMap, tagsArray, reqStats);
    }

    @Override
    public List<TrafficPoint> query(Map<String, String> queryTags, List<String> groupBy) {

        if (trafficEntity == null) {
            return null;
        }
        TrafficEntity entity = this.trafficEntity;
        List<TrafficPoint> result = new ArrayList<>();
        List<List<Integer>> indexs = new ArrayList<>();
        if (queryTags == null || queryTags.size() == 0) {
            if (groupBy == null || groupBy.size() == 0) {
                TrafficPoint point = new TrafficPoint();
                long sum = 0;
                for (ReqStats req : entity.getReqStats()) {
                    sum += req.getCount();
                }
                point.setCount(sum).setGroupBy(GROUP_BY_TOTAL);
                result.add(point);
            } else {
                Map<String, Long> groupByCount = new HashMap<>();
                for (int idx = 0; idx < entity.getReqStats().size(); idx++) {
                    Map<String, String> tags = entity.getTagsArray().get(idx);
                    StringBuilder groupByValue = new StringBuilder();
                    for (String g : groupBy) {
                        String tagValue = tags.get(g);
                        if (tagValue == null) {
                            tagValue = "-";
                        }
                        groupByValue.append(g);
                        groupByValue.append(":");
                        groupByValue.append(tagValue);
                        groupByValue.append(";");
                    }
                    String tmpKey = groupByValue.toString();
                    groupByCount.putIfAbsent(tmpKey, 0L);
                    groupByCount.put(tmpKey, groupByCount.get(tmpKey) + entity.getReqStats().get(idx).getCount());
                }
                for (Map.Entry<String, Long> groupByKey : groupByCount.entrySet()) {
                    result.add(new TrafficPoint(groupByKey.getKey(), groupByKey.getValue(), groupByKey.getValue() / (double) inertval.get()));
                }
            }
        } else {
            for (Map.Entry<String, String> tag : queryTags.entrySet()) {
                String key = tag.getKey() + "-" + tag.getValue();
                indexs.add(entity.getTagMap().get(key));
            }
            List<Integer> maxRetained = retainAll(indexs);
            if (groupBy == null || groupBy.size() == 0) {
                TrafficPoint point = new TrafficPoint();
                long sum = 0;
                for (Integer idx : maxRetained) {
                    sum += entity.getReqStats().get(idx).getCount();
                }
                point.setCount(sum).setGroupBy(GROUP_BY_TOTAL).setQps(sum / (double) inertval.get());
                result.add(point);
            } else {
                Map<String, Long> groupByCount = new HashMap<>();
                for (Integer idx : maxRetained) {
                    Map<String, String> tags = entity.getTagsArray().get(idx);
                    StringBuilder groupByValue = new StringBuilder();
                    for (String g : groupBy) {
                        String tagValue = tags.get(g);
                        if (tagValue == null) {
                            tagValue = "-";
                        }
                        groupByValue.append(g);
                        groupByValue.append(":");
                        groupByValue.append(tagValue);
                        groupByValue.append(";");
                    }
                    String tmpKey = groupByValue.toString();
                    groupByCount.putIfAbsent(tmpKey, 0L);
                    groupByCount.put(tmpKey, groupByCount.get(tmpKey) + entity.getReqStats().get(idx).getCount());
                }
                for (Map.Entry<String, Long> groupByKey : groupByCount.entrySet()) {
                    result.add(new TrafficPoint(groupByKey.getKey(), groupByKey.getValue(), groupByKey.getValue() / (double) inertval.get()));
                }
            }
        }
        return result;

    }

    // intersection
    private List<Integer> retainAll(List<List<Integer>> indexs) {
        if (indexs.size() == 0 || CollectionUtils.isEmpty(indexs.get(0))) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>(indexs.get(0));
        for (int i = 1; i < indexs.size(); i++) {
            result.retainAll(indexs.get(i));
            if (result.size() == 0) {
                return result;
            }
        }
        return result;
    }
}
