package com.ctrip.zeus.service.traffic;

import com.ctrip.zeus.logstats.tools.*;
import com.ctrip.zeus.model.TrafficPoint;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public interface TrafficMonitorService {

    void refresh(ConcurrentMap<StatsKey, ReqStats> statsKeyStatsConcurrentMap);

    List<TrafficPoint> query(Map<String, String> queryTags, List<String> groupBy);

}
