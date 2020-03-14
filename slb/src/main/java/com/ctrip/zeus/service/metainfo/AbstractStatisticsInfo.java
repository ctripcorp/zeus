package com.ctrip.zeus.service.metainfo;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.tag.PropertyService;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractStatisticsInfo implements StatisticsInfo {
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private PropertyService propertyService;

    private Map<Long, SlbMeta> slbMetaMap = new ConcurrentHashMap<>();
    private Map<Long, VsMeta> vsMetaMap = new ConcurrentHashMap<>();
    private Map<Long, GroupMeta> groupMetaMap = new ConcurrentHashMap<>();
    private Map<String, SlbServerQps> slbServerQpsMap = new ConcurrentHashMap<>();
    private Map<String, SbuMeta> sbuMap = new ConcurrentHashMap<>();
    private Map<String, IdcMeta> idcMetaMap = new ConcurrentHashMap<>();
    private Map<String, AppMeta> appMetaMap = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final DynamicBooleanProperty qpsStatisticsEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("statistics.qps.enable", true);

    @Override
    public void updateMeta() {
        try {
            Map<Long, SlbMeta> slbMetaMap = new ConcurrentHashMap<>();
            Map<Long, VsMeta> vsMetaMap = new ConcurrentHashMap<>();
            Map<Long, GroupMeta> groupMetaMap = new ConcurrentHashMap<>();
            Map<String, SlbServerQps> slbServerQpsMap = new ConcurrentHashMap<>();
            Map<String, SbuMeta> sbuMap = new ConcurrentHashMap<>();
            Map<String, IdcMeta> idcMetaMap = new ConcurrentHashMap<>();
            Map<String, AppMeta> appMetaMap = new ConcurrentHashMap<>();
            Set<IdVersion> idVersionSet = groupCriteriaQuery.queryAll(SelectionMode.OFFLINE_FIRST);
            List<Group> groups = groupRepository.list(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
            Set<IdVersion> vsIdSet = virtualServerCriteriaQuery.queryAll(SelectionMode.OFFLINE_FIRST);
            List<VirtualServer> virtualServers = virtualServerRepository.listAll(vsIdSet.toArray(new IdVersion[vsIdSet.size()]));
            Set<IdVersion> slbIdSet = slbCriteriaQuery.queryAll(SelectionMode.OFFLINE_FIRST);
            List<Slb> slbs = slbRepository.list(slbIdSet.toArray(new IdVersion[slbIdSet.size()]));
            Map<Long, Group> groupMap = new HashMap<>();
            Map<Long, Slb> slbMap = new HashMap<>();
            Map<Long, Set<Long>> vsGroupIds = new HashMap<>();
            Map<Long, Set<String>> vsMembers = new HashMap<>();
            Map<Long, Integer> vsMemberCount = new HashMap<>();
            Map<String, List<Group>> appIdGroups = new HashMap<>();
            Map<Long, Set<Long>> slbVsMap = new HashMap<>();
            updateGroupMeta(groups, appIdGroups, groupMap, vsGroupIds, vsMembers, groupMetaMap);
            updateVSMeta(virtualServers, vsGroupIds, groupMap, vsMemberCount, vsMembers, slbVsMap, vsMetaMap);
            Map<Long, Set<String>> slbGroupServers = new HashMap<>();
            updateSlbMeta(slbGroupServers, slbs, slbMap, slbVsMap, vsGroupIds, vsMemberCount, vsMembers, groupMap, slbMetaMap, slbServerQpsMap);
            updateSbuMeta(groupMap, sbuMap);
            updateAppMeta(appIdGroups, appMetaMap);
            updateQpses(slbMetaMap, vsMetaMap, groupMetaMap, slbServerQpsMap, sbuMap, appMetaMap);
            updateIdcMeta(slbGroupServers, slbMap, slbVsMap, vsGroupIds, groupMap, idcMetaMap, slbMetaMap);
            Map<Long, SlbMeta> slbTmp = this.slbMetaMap;
            this.slbMetaMap = slbMetaMap;
            slbTmp.clear();
            Map<Long, VsMeta> vsTmp = this.vsMetaMap;
            this.vsMetaMap = vsMetaMap;
            vsTmp.clear();
            Map<Long, GroupMeta> groupTmp = this.groupMetaMap;
            this.groupMetaMap = groupMetaMap;
            groupTmp.clear();
            Map<String, SlbServerQps> hostTmp = this.slbServerQpsMap;
            this.slbServerQpsMap = slbServerQpsMap;
            hostTmp.clear();
            Map<String, SbuMeta> sbuTep = this.sbuMap;
            this.sbuMap = sbuMap;
            sbuTep.clear();
            Map<String, IdcMeta> idcTmp = this.idcMetaMap;
            this.idcMetaMap = idcMetaMap;
            idcTmp.clear();
            Map<String, AppMeta> appTmp = this.appMetaMap;
            this.appMetaMap = appMetaMap;
            appTmp.clear();
        } catch (Exception e) {
            logger.error("update Meta info failed.Get offline groups failed.", e);
        }
    }

    @Override
    public List<SlbMeta> getAllSlbMeta() {
        List<SlbMeta> result = new ArrayList<>();
        if (slbMetaMap.size() > 0) {
            result.addAll(slbMetaMap.values());
        }
        return result;
    }

    @Override
    public List<SlbMeta> getAllSlbMeta(List<Long> slbIds) {
        List<SlbMeta> result = new ArrayList<>();
        if (slbIds == null) return result;
        for (Long slbId : slbIds) {
            if (slbMetaMap.containsKey(slbId)) {
                result.add(slbMetaMap.get(slbId));
            }
        }
        return result;
    }

    @Override
    public List<VsMeta> getAllVsMeta() {
        List<VsMeta> result = new ArrayList<>();
        if (vsMetaMap.size() > 0) {
            result.addAll(vsMetaMap.values());
        }
        return result;
    }

    @Override
    public List<VsMeta> getAllVsMeta(List<Long> vsIds) {
        List<VsMeta> result = new ArrayList<>();
        if (vsIds == null) return result;
        for (Long vsId : vsIds) {
            if (vsMetaMap.containsKey(vsId)) {
                result.add(vsMetaMap.get(vsId));
            }
        }
        return result;
    }

    @Override
    public List<GroupMeta> getAllGroupMeta() {
        List<GroupMeta> result = new ArrayList<>();
        if (groupMetaMap.size() > 0) {
            result.addAll(groupMetaMap.values());
        }
        return result;
    }

    @Override
    public List<GroupMeta> getAllGroupMeta(List<Long> groupIds) {
        List<GroupMeta> result = new ArrayList<>();
        if (groupIds == null) return result;
        for (Long groupId : groupIds) {
            if (groupMetaMap.containsKey(groupId)) {
                result.add(groupMetaMap.get(groupId));
            }
        }
        return result;
    }

    @Override
    public List<SlbServerQps> getAllSlbServerQps() {
        List<SlbServerQps> result = new ArrayList<>();
        if (slbServerQpsMap.size() > 0) {
            result.addAll(slbServerQpsMap.values());
        }
        return result;
    }

    @Override
    public List<SlbServerQps> getAllSlbServerQps(List<String> ips) {
        List<SlbServerQps> result = new ArrayList<>();
        if (ips == null) return result;

        for (String ip : ips) {
            if (slbServerQpsMap.containsKey(ip)) {
                result.add(slbServerQpsMap.get(ip));
            }
        }
        return result;
    }

    @Override
    public List<SbuMeta> getAllSbuMeta() {
        List<SbuMeta> result = new ArrayList<>();
        if (sbuMap.size() > 0) {
            result.addAll(sbuMap.values());
        }
        return result;
    }

    @Override
    public List<SbuMeta> getAllSbuMeta(List<String> sbus) {
        List<SbuMeta> result = new ArrayList<>();
        if (sbus == null) return result;

        for (String sbu : sbus) {
            if (sbuMap.containsKey(sbu)) {
                result.add(sbuMap.get(sbu));
            }
        }
        return result;
    }

    @Override
    public List<IdcMeta> getAllIdcMeta(List<String> idcs) {
        List<IdcMeta> result = new ArrayList<>();
        if (idcs == null) return result;

        for (String idc : idcs) {
            if (idcMetaMap.containsKey(idc)) {
                result.add(idcMetaMap.get(idc));
            }
        }
        return result;
    }

    @Override
    public List<IdcMeta> getAllIdcMeta() {
        List<IdcMeta> result = new ArrayList<>();
        if (idcMetaMap.size() > 0) {
            result.addAll(idcMetaMap.values());
        }
        return result;
    }

    @Override
    public List<AppMeta> getAllAppMeta() {
        List<AppMeta> result = new ArrayList<>();
        if (appMetaMap.size() > 0) {
            result.addAll(appMetaMap.values());
        }
        return result;
    }

    @Override
    public List<AppMeta> getAllAppMeta(List<String> appids) {
        List<AppMeta> result = new ArrayList<>();
        if (appids == null) return result;

        for (String appId : appids) {
            if (appMetaMap.containsKey(appId)) {
                result.add(appMetaMap.get(appId));
            }
        }
        return result;
    }


    private void updateQpses(Map<Long, SlbMeta> slbMetaMap, Map<Long, VsMeta> vsMetaMap, Map<Long, GroupMeta> groupMetaMap,
                             Map<String, SlbServerQps> slbServerQpsMap, Map<String, SbuMeta> sbuMap, Map<String, AppMeta> appMetaMap) {
        if (!qpsStatisticsEnabled.get()) {
            return;
        }
        updateSLBQps(slbMetaMap);
        updateVsQps(vsMetaMap);
        updateGroupQps(groupMetaMap);
        updateHostQps(slbServerQpsMap);
        updateSbuQps(sbuMap);
        updateAppQps(appMetaMap);
    }

    private void updateIdcMeta(Map<Long, Set<String>> slbGroupServers, Map<Long, Slb> slbMap, Map<Long, Set<Long>> slbVsMap,
                               Map<Long, Set<Long>> vsGroupIds, Map<Long, Group> groupMap, Map<String, IdcMeta> idcMetaMap,
                               Map<Long, SlbMeta> slbMetaMap) throws Exception {
        Map<Property, List<Long>> idcMap = propertyService.queryTargetGroup("idc", "slb");
        for (Property p : idcMap.keySet()) {
            if (!idcMetaMap.containsKey(p.getValue())) {
                idcMetaMap.put(p.getValue(), new IdcMeta().setIdc(p.getValue()).setQps(0d));
            }
            IdcMeta idcMeta = idcMetaMap.get(p.getValue());
            idcMeta.setSlbCount(idcMap.get(p).size());
            Set<String> serverMembers = new HashSet<>();
            int vsCount = 0;
            int groupCount = 0;
            int memberCount = 0;
            int slbServerCount = 0;
            double qps = 0;
            Set<String> tmpAppIds = new HashSet<>();
            for (Long slbId : idcMap.get(p)) {
                if (slbGroupServers.containsKey(slbId)) {
                    serverMembers.addAll(slbGroupServers.get(slbId));
                }
                if (slbMetaMap.containsKey(slbId)) {
                    SlbMeta meta = slbMetaMap.get(slbId);
                    vsCount += meta.getVsCount();
                    groupCount += meta.getGroupCount();
                    memberCount += meta.getMemberCount();
                    qps += meta.getQps();
                }
                if (slbMap.containsKey(slbId)) {
                    slbServerCount += slbMap.get(slbId).getSlbServers().size();
                    Set<Long> s = slbVsMap.get(slbId);
                    if (s == null) continue;

                    for (Long vsId : s) {
                        if (!vsGroupIds.containsKey(vsId)) continue;

                        for (Long g : vsGroupIds.get(vsId)) {
                            tmpAppIds.add(groupMap.get(g).getAppId());
                        }
                    }
                }
            }
            idcMeta.setGroupServerCount(serverMembers.size());
            idcMeta.setMemberCount(memberCount);
            idcMeta.setVsCount(vsCount);
            idcMeta.setGroupCount(groupCount);
            idcMeta.setQps(qps);
            idcMeta.setSlbServerCount(slbServerCount);
            idcMeta.setAppCount(tmpAppIds.size());
        }
    }

    private void updateAppMeta(Map<String, List<Group>> appIdGroups, Map<String, AppMeta> appMetaMap) {
        for (String appId : appIdGroups.keySet()) {
            AppMeta tmp = new AppMeta();
            tmp.setAppId(appId).setGroupCount(appIdGroups.get(appId).size()).setQps(0d);
            List<Group> list = appIdGroups.get(appId);
            Set<Long> vsIds = new HashSet<>();
            Set<Long> slbIds = new HashSet<>();
            Set<String> servers = new HashSet<>();
            int memberCount = 0;

            for (Group g : list) {
                memberCount += g.getGroupServers().size();
                for (GroupServer gs : g.getGroupServers()) {
                    servers.add(gs.getIp());
                }
                for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                    slbIds.addAll(gvs.getVirtualServer().getSlbIds());
                    vsIds.add(gvs.getVirtualServer().getId());
                }
            }
            tmp.setVsCount(vsIds.size());
            tmp.setSlbCount(slbIds.size());
            tmp.setMemberCount(memberCount);
            tmp.setGroupServerCount(servers.size());
            appMetaMap.put(appId, tmp);
        }
    }

    private void updateSbuMeta(Map<Long, Group> groupMap, Map<String, SbuMeta> sbuMap) throws Exception {
        Map<Property, List<Long>> map = propertyService.queryTargetGroup("SBU", "group");
        for (Property p : map.keySet()) {
            if (!sbuMap.containsKey(p.getValue())) {
                sbuMap.put(p.getValue(), new SbuMeta().setSbu(p.getValue()).setQps(0d));
            }
            Set<String> tmpAppIds = new HashSet<>();
            SbuMeta sbuMeta = sbuMap.get(p.getValue());
            sbuMeta.setGroupCount(map.get(p).size());
            Set<String> serverMembers = new HashSet<>();
            Set<Long> vsIds = new HashSet<>();
            int memberCount = 0;
            for (Long gid : map.get(p)) {
                if (!groupMap.containsKey(gid)) {
                    continue;
                }
                memberCount += groupMap.get(gid).getGroupServers().size();
                tmpAppIds.add(groupMap.get(gid).getAppId());
                for (GroupServer gs : groupMap.get(gid).getGroupServers()) {
                    serverMembers.add(gs.getIp());
                }
                for (GroupVirtualServer gvs : groupMap.get(gid).getGroupVirtualServers()) {
                    vsIds.add(gvs.getVirtualServer().getId());
                }
            }
            sbuMeta.setGroupServerCount(serverMembers.size());
            sbuMeta.setMemberCount(memberCount);
            sbuMeta.setVsCount(vsIds.size());
            sbuMeta.setAppCount(tmpAppIds.size());
        }

    }

    private void updateSlbMeta(Map<Long, Set<String>> slbGroupServers, List<Slb> slbs, Map<Long, Slb> slbMap, Map<Long, Set<Long>> slbVsMap, Map<Long, Set<Long>> vsGroupIds, Map<Long, Integer> vsMemberCount, Map<Long, Set<String>> vsMembers, Map<Long, Group> groupMap, Map<Long, SlbMeta> slbMetaMap, Map<String, SlbServerQps> slbServerQpsMap) {
        for (Slb slb : slbs) {
            if (!slbMetaMap.containsKey(slb.getId())) {
                slbMetaMap.put(slb.getId(), new SlbMeta().setSlbId(slb.getId()).setQps(0d));
            }
            for (SlbServer slbServer : slb.getSlbServers()) {
                slbServerQpsMap.put(slbServer.getIp(), new SlbServerQps().setIp(slbServer.getIp()));
            }
            Set<String> tmpAppIds = new HashSet<>();
            slbMap.put(slb.getId(), slb);
            SlbMeta slbMeta = slbMetaMap.get(slb.getId());

            Set<Long> s = slbVsMap.get(slb.getId());
            slbMeta.setVsCount(s == null ? 0 : s.size());

            int groupCount = 0;
            int groupMemberCount = 0;
            Set<String> groupServer = new HashSet<>();
            if (s != null) {
                for (Long vsId : s) {
                    if (vsGroupIds.containsKey(vsId)) {
                        groupCount += vsGroupIds.get(vsId).size();
                        for (Long gid : vsGroupIds.get(vsId)) {
                            tmpAppIds.add(groupMap.get(gid).getAppId());
                        }
                    }
                    if (vsMemberCount.containsKey(vsId)) {
                        groupMemberCount += vsMemberCount.get(vsId);
                    }
                    if (vsMembers.containsKey(vsId)) {
                        groupServer.addAll(vsMembers.get(vsId));
                    }
                }
            }
            slbMeta.setGroupCount(groupCount);
            slbMeta.setMemberCount(groupMemberCount);
            slbMeta.setGroupServerCount(groupServer.size());
            slbMeta.setAppCount(tmpAppIds.size());
            slbGroupServers.put(slb.getId(), groupServer);
        }


    }

    private void updateVSMeta(List<VirtualServer> virtualServers, Map<Long, Set<Long>> vsGroupIds, Map<Long, Group> groupMap, Map<Long, Integer> vsMemberCount, Map<Long, Set<String>> vsMembers, Map<Long, Set<Long>> slbVsMap, Map<Long, VsMeta> vsMetaMap) {

        for (VirtualServer vs : virtualServers) {
            Set<String> tmpAppIds = new HashSet<>();
            if (!vsMetaMap.containsKey(vs.getId())) {
                vsMetaMap.put(vs.getId(), new VsMeta().setVsId(vs.getId()).setQps(0d));
            }
            VsMeta vsMeta = vsMetaMap.get(vs.getId());
            if (vsGroupIds.containsKey(vs.getId())) {
                vsMeta.setGroupCount(vsGroupIds.get(vs.getId()).size());
                int memberCount = 0;
                for (Long gid : vsGroupIds.get(vs.getId())) {
                    memberCount += groupMap.get(gid).getGroupServers().size();
                    tmpAppIds.add(groupMap.get(gid).getAppId());
                }
                vsMeta.setMemberCount(memberCount);
                vsMeta.setAppCount(tmpAppIds.size());
                vsMemberCount.put(vs.getId(), memberCount);
                if (vsMembers.containsKey(vs.getId())) {
                    vsMeta.setGroupServerCount(vsMembers.get(vs.getId()).size());
                } else {
                    vsMeta.setGroupServerCount(0);
                }
            } else {
                vsMeta.setGroupCount(0);
                vsMeta.setMemberCount(0);
                vsMeta.setGroupServerCount(0);
                vsMemberCount.put(vs.getId(), 0);
            }

            for (Long slbId : vs.getSlbIds()) {
                Set<Long> s = slbVsMap.get(slbId);
                if (s == null) {
                    s = new HashSet<>();
                    slbVsMap.put(slbId, s);
                }
                s.add(vs.getId());
            }
        }
    }

    private void updateGroupMeta(List<Group> groups, Map<String, List<Group>> appIdGroups,
                                 Map<Long, Group> groupMap, Map<Long, Set<Long>> vsGroupIds,
                                 Map<Long, Set<String>> vsMembers, Map<Long, GroupMeta> groupMetaMap) {
        for (Group g : groups) {
            if (!groupMetaMap.containsKey(g.getId())) {
                groupMetaMap.put(g.getId(), new GroupMeta().setGroupId(g.getId()).setQps(0d));
            }
            if (!appIdGroups.containsKey(g.getAppId())) {
                appIdGroups.put(g.getAppId(), new ArrayList<Group>());
            }
            appIdGroups.get(g.getAppId()).add(g);

            groupMetaMap.get(g.getId()).setMemberCount(g.getGroupServers().size());
            groupMetaMap.get(g.getId()).setGroupServerCount(g.getGroupServers().size());
            groupMap.put(g.getId(), g);
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (!vsGroupIds.containsKey(gvs.getVirtualServer().getId())) {
                    vsGroupIds.put(gvs.getVirtualServer().getId(), new HashSet<Long>());
                }
                vsGroupIds.get(gvs.getVirtualServer().getId()).add(g.getId());
                if (!vsMembers.containsKey(gvs.getVirtualServer().getId())) {
                    vsMembers.put(gvs.getVirtualServer().getId(), new HashSet<String>());
                }
                for (GroupServer gs : g.getGroupServers()) {
                    vsMembers.get(gvs.getVirtualServer().getId()).add(gs.getIp());
                }
            }
        }
    }
}
