package com.ctrip.zeus.service.metainfo;

import com.ctrip.zeus.model.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2016/7/29.
 */
public interface StatisticsInfo {
    void updateSLBQps(Map<Long, SlbMeta> slbMap);

    void updateVsQps(Map<Long, VsMeta> vsMap);

    void updateGroupQps(Map<Long, GroupMeta> groupMap);

    void updateHostQps(Map<String, SlbServerQps> host);

    void updateSbuQps(Map<String, SbuMeta> sbuMap);

    void updateAppQps(Map<String, AppMeta> appMetaMap);

    void updateMeta();

    List<SlbMeta> getAllSlbMeta();

    List<SlbMeta> getAllSlbMeta(List<Long> slbIds);

    List<VsMeta> getAllVsMeta();

    List<VsMeta> getAllVsMeta(List<Long> vsIds);

    List<GroupMeta> getAllGroupMeta();

    List<GroupMeta> getAllGroupMeta(List<Long> groupIds);

    List<SlbServerQps> getAllSlbServerQps();

    List<SlbServerQps> getAllSlbServerQps(List<String> ips);

    List<SbuMeta> getAllSbuMeta();

    List<SbuMeta> getAllSbuMeta(List<String> sbus);

    List<IdcMeta> getAllIdcMeta();

    List<IdcMeta> getAllIdcMeta(List<String> idcs);

    List<AppMeta> getAllAppMeta();

    List<AppMeta> getAllAppMeta(List<String> appids);

    HashMap<String, Object> getAllDashboardMeta(String op, String domain, String ip, String date, String type, String catEnv) throws Exception;

    HashMap<String, Object> getApiDashboardMeta(String op, String domain, String ip, String date, String type, String catEnv) throws Exception;
}
