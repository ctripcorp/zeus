package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.*;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.SmartArchiveGroupMapper;
import com.ctrip.zeus.service.SmartGroupStatusRMapper;
import com.ctrip.zeus.service.SmartVsStatusRMapper;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2016/1/19.
 */
@Component("entityFactory")
public class EntityFactoryImpl implements EntityFactory {
    @Resource
    private SlbArchiveGroupMapper slbArchiveGroupMapper;
    @Resource
    private SlbArchiveVsMapper slbArchiveVsMapper;

    @Resource
    private SlbArchiveSlbMapper slbArchiveSlbMapper;

    @Resource
    private SlbGroupStatusRMapper slbGroupStatusRMapper;

    @Resource
    private SlbGroupGsRMapper slbGroupGsRMapper;

    @Resource
    private SlbVsStatusRMapper slbVsStatusRMapper;

    @Resource
    private SlbSlbStatusRMapper slbSlbStatusRMapper;

    @Resource
    private SlbTrafficPolicyGroupRMapper slbTrafficPolicyGroupRMapper;

    @Resource
    private SlbTrafficPolicyVsRMapper slbTrafficPolicyVsRMapper;

    @Resource
    private SlbTrafficPolicyMapper slbTrafficPolicyMapper;

    @Resource
    private SlbArchiveDrMapper slbArchiveDrMapper;

    @Resource
    private SlbDrStatusRMapper slbDrStatusRMapper;

    @Resource
    private AutoFiller autoFiller;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SmartGroupStatusRMapper smartGroupStatusRMapper;
    @Resource
    private SmartVsStatusRMapper smartVsStatusRMapper;
    @Resource
    private SmartArchiveGroupMapper smartArchiveGroupMapper;

    @Override
    public ModelStatusMapping<Group> getGroupsByVsIds(Long[] vsIds) throws Exception {
        if (vsIds == null || vsIds.length == 0) return new ModelStatusMapping<>();

        Set<Long> groupIds = new HashSet<>();
        Map<String, Group> ref = new HashMap<>();
        ModelStatusMapping<Group> result = new ModelStatusMapping<>();

        for (SlbArchiveGroup d : smartArchiveGroupMapper.findAllByVsIds(Arrays.asList(vsIds))) {
            groupIds.add(d.getGroupId());
            Group g = ContentReaders.readGroupContent(d.getContent());
            g.setCreatedTime(d.getDatachangeLasttime());
            for (GroupVirtualServer e : g.getGroupVirtualServers()) {
                e.setVirtualServer(new VirtualServer().setId(e.getVirtualServer().getId()));
            }
            autoFiller.autofill(g);
            ref.put(d.getGroupId() + "," + d.getVersion(), g);
        }

        if (groupIds.size() > 0) {
            for (SlbGroupStatusR d : smartGroupStatusRMapper.selectByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdIn(new ArrayList<>(groupIds)).example())) {
                Group tmp = ref.get(d.getGroupId() + "," + d.getOfflineVersion());
                if (tmp != null) result.addOffline(d.getGroupId(), tmp);
                if (d.getOnlineVersion() != 0) {
                    tmp = ref.get(d.getGroupId() + "," + d.getOnlineVersion());
                    if (tmp != null) result.addOnline(d.getGroupId(), tmp);
                }
            }
        }
        return result;
    }

    @Override
    public ModelStatusMapping<VirtualServer> getVsesBySlbIds(Long slbId) throws Exception {
        if (slbId == null) return new ModelStatusMapping<>();
        Set<Long> vsIds = new HashSet<>();
        Map<String, VirtualServer> ref = new HashMap<>();
        for (SlbArchiveVs d : slbArchiveVsMapper.findAllBySlbId(slbId)) {
            vsIds.add(d.getVsId());
            VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
            autoFiller.autofill(vs);
            ref.put(vs.getId() + "," + d.getVersion(), vs);
        }

        ModelStatusMapping<VirtualServer> result = new ModelStatusMapping<>();
        if (vsIds.size() == 0) return result;

        for (SlbVsStatusR d : smartVsStatusRMapper.selectByExample(new SlbVsStatusRExample().createCriteria().andVsIdIn(new ArrayList<>(vsIds)).example())) {
            VirtualServer tmp = ref.get(d.getVsId() + "," + d.getOfflineVersion());
            if (tmp != null) result.addOffline(d.getVsId(), tmp);
            if (d.getOnlineVersion() != 0) {
                tmp = ref.get(d.getVsId() + "," + d.getOnlineVersion());
                if (tmp != null) result.addOnline(d.getVsId(), tmp);
            }
        }
        return result;

    }

    @Override
    public ModelStatusMapping<Slb> getSlbsByIds(Long[] slbIds) throws Exception {
        if (slbIds == null || slbIds.length == 0) return new ModelStatusMapping<>();
        Map<String, Slb> ref = new HashMap<>();

        for (SlbArchiveSlb d : slbArchiveSlbMapper.findVersionizedByIds(Arrays.asList(slbIds))) {
            Slb slb = ContentReaders.readSlbContent(d.getContent());
            autoFiller.autofill(slb);
            ref.put(slb.getId() + "," + slb.getVersion(), slb);
        }

        ModelStatusMapping<Slb> result = new ModelStatusMapping<>();
        if (slbIds == null || slbIds.length == 0) return result;
        for (SlbSlbStatusR d : slbSlbStatusRMapper.selectByExample(new SlbSlbStatusRExample().createCriteria().andSlbIdIn(Arrays.asList(slbIds)).example())) {
            result.addOffline(d.getSlbId(), ref.get(d.getSlbId() + "," + d.getOfflineVersion()));
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getSlbId(), ref.get(d.getSlbId() + "," + d.getOnlineVersion()));
            }
        }
        return result;
    }

    @Override
    public ModelStatusMapping<VirtualServer> getVsesByIds(Long[] vsIds) throws Exception {
        if (vsIds == null || vsIds.length == 0) return new ModelStatusMapping<>();

        Map<String, VirtualServer> ref = new HashMap<>();
        ModelStatusMapping<VirtualServer> result = new ModelStatusMapping<>();

        for (SlbArchiveVs d : slbArchiveVsMapper.findVersionizedByIds(Arrays.asList(vsIds))) {
            VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
            autoFiller.autofill(vs);
            ref.put(vs.getId() + "," + d.getVersion(), vs);
        }
        for (SlbVsStatusR d : smartVsStatusRMapper.selectByExample(new SlbVsStatusRExample().createCriteria().andVsIdIn(Arrays.asList(vsIds)).example())) {
            result.addOffline(d.getVsId(), ref.get(d.getVsId() + "," + d.getOfflineVersion()));
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getVsId(), ref.get(d.getVsId() + "," + d.getOnlineVersion()));
            }
        }

        return result;
    }

    @Override
    public ModelStatusMapping<Group> getGroupsByIds(Long[] groupIds) throws Exception {
        if (groupIds == null || groupIds.length == 0) return new ModelStatusMapping<>();
        ModelStatusMapping<Group> result = new ModelStatusMapping<>();

        Map<String, Group> ref = new HashMap<>();
        for (SlbArchiveGroup d : smartArchiveGroupMapper.findVersionizedByIds(Arrays.asList(groupIds))) {
            Group g = ContentReaders.readGroupContent(d.getContent());
            g.setCreatedTime(d.getDatachangeLasttime());
            for (GroupVirtualServer e : g.getGroupVirtualServers()) {
                e.setVirtualServer(new VirtualServer().setId(e.getVirtualServer().getId()));
            }
            autoFiller.autofill(g);
            ref.put(d.getGroupId() + "," + d.getVersion(), g);
        }

        for (SlbGroupStatusR d : smartGroupStatusRMapper.selectByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdIn(Arrays.asList(groupIds)).example())) {
            result.addOffline(d.getGroupId(), ref.get(d.getGroupId() + "," + d.getOfflineVersion()));
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getGroupId(), ref.get(d.getGroupId() + "," + d.getOnlineVersion()));
            }
        }
        return result;
    }

    @Override
    public ModelStatusMapping<TrafficPolicy> getPoliciesByIds(Long[] policyIds) throws Exception {
        ModelStatusMapping<TrafficPolicy> result = new ModelStatusMapping<>();
        if (policyIds == null || policyIds.length == 0) return result;

        Map<Long, TrafficPolicy> offlineResult = new HashMap<>();
        Map<Long, TrafficPolicy> onlineResult = new HashMap<>();
        Set<Integer> searchKeys = new HashSet<>();
        for (SlbTrafficPolicy e : slbTrafficPolicyMapper.selectByExample(new SlbTrafficPolicyExample().createCriteria().andIdIn(Arrays.asList(policyIds)).example())) {
            offlineResult.put(e.getId(), new TrafficPolicy().setId(e.getId()).setName(e.getName()).setVersion(e.getNxActiveVersion()));
            searchKeys.add(VersionUtils.getHash(e.getId(), e.getNxActiveVersion()));
            if (e.getActiveVersion() > 0) {
                onlineResult.put(e.getId(), new TrafficPolicy().setId(e.getId()).setVersion(e.getActiveVersion()));
                searchKeys.add(VersionUtils.getHash(e.getId(), e.getActiveVersion()));
            }
        }

        Integer[] hashes = searchKeys.toArray(new Integer[searchKeys.size()]);
        for (SlbTrafficPolicyGroupR e : slbTrafficPolicyGroupRMapper.selectByExample(new SlbTrafficPolicyGroupRExample().createCriteria().andHashIn(Arrays.asList(hashes)).example())) {
            TrafficPolicy v = offlineResult.get(e.getPolicyId());
            if (v != null && v.getVersion().equals(e.getPolicyVersion())) {
                v.addTrafficControl(new TrafficControl().setGroup(new Group().setId(e.getGroupId())).setWeight(e.getWeight()));
            }
            v = onlineResult.get(e.getPolicyId());
            if (v != null && v.getVersion().equals(e.getPolicyVersion())) {
                v.addTrafficControl(new TrafficControl().setGroup(new Group().setId(e.getGroupId())).setWeight(e.getWeight()));
            }
        }

        for (SlbTrafficPolicyVsR e : slbTrafficPolicyVsRMapper.selectByExample(new SlbTrafficPolicyVsRExample().createCriteria().andHashIn(Arrays.asList(hashes)).example())) {
            TrafficPolicy v = offlineResult.get(e.getPolicyId());
            if (v != null && v.getVersion().equals(e.getPolicyVersion())) {
                v.addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(new VirtualServer().setId(e.getVsId())).setPath(e.getPath()).setPriority(e.getPriority()));
            }
            v = onlineResult.get(e.getPolicyId());
            if (v != null && v.getVersion().equals(e.getPolicyVersion())) {
                v.addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(new VirtualServer().setId(e.getVsId())).setPath(e.getPath()).setPriority(e.getPriority()));
            }
        }

        for (Map.Entry<Long, TrafficPolicy> e : offlineResult.entrySet()) {
            result.addOffline(e.getKey(), e.getValue());
        }
        for (Map.Entry<Long, TrafficPolicy> e : onlineResult.entrySet()) {
            result.addOnline(e.getKey(), e.getValue());
        }
        return result;
    }

    @Override
    public ModelStatusMapping<TrafficPolicy> getPoliciesByVsIds(Long[] vsIds) throws Exception {
        Set<Long> offlinePolicyIds = new HashSet<>();
        if (vsIds == null || vsIds.length == 0) return new ModelStatusMapping<>();

        for (SlbTrafficPolicyVsR e : slbTrafficPolicyVsRMapper.findByVsesAndPolicyVersion(Arrays.asList(vsIds))) {
            offlinePolicyIds.add(e.getPolicyId());
        }
        return getPoliciesByIds(offlinePolicyIds.toArray(new Long[offlinePolicyIds.size()]));
    }

    @Override
    public ModelStatusMapping<Dr> getDrsByIds(Long[] drIds) throws Exception {
        if (drIds == null || drIds.length == 0) return new ModelStatusMapping<>();
        ModelStatusMapping<Dr> result = new ModelStatusMapping<>();
        Map<String, Dr> ref = new HashMap<>();
        if (drIds == null || drIds.length == 0) return result;

        for (SlbArchiveDr arvhive : slbArchiveDrMapper.findVersionizedByIds(Arrays.asList(drIds))) {
            Dr dr = ContentReaders.readDrContent(arvhive.getContent());
            dr.setCreatedTime(arvhive.getDatachangeLasttime());
            ref.put(arvhive.getDrId() + "," + arvhive.getVersion(), dr);
        }
        for (SlbDrStatusR d : slbDrStatusRMapper.selectByExample(new SlbDrStatusRExample().createCriteria().andDrIdIn(Arrays.asList(drIds)).example())) {
            result.addOffline(d.getDrId(), ref.get(d.getDrId() + "," + d.getOfflineVersion()));
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getDrId(), ref.get(d.getDrId() + "," + d.getOnlineVersion()));
            }
        }

        return result;
    }

    @Override
    public ModelStatusMapping<Dr> getDrsByVsIds(Long[] vsIds) throws Exception {
        ModelStatusMapping<Dr> result = new ModelStatusMapping<>();
        if (vsIds == null || vsIds.length == 0) return result;

        Set<Long> drIds = new HashSet<>();
        Map<String, Dr> ref = new HashMap<>();
        for (SlbArchiveDr d : slbArchiveDrMapper.findAllByVsIds(Arrays.asList(vsIds))) {
            drIds.add(d.getDrId());
            Dr dr = ContentReaders.readDrContent(d.getContent());
            dr.setCreatedTime(d.getDatachangeLasttime());
            ref.put(d.getDrId() + "," + d.getVersion(), dr);
        }
        if (drIds.size() == 0) return result;
        for (SlbDrStatusR d : slbDrStatusRMapper.selectByExample(new SlbDrStatusRExample().createCriteria().andDrIdIn(new ArrayList<>(drIds)).example())) {
            Dr tmp = ref.get(d.getDrId() + "," + d.getOfflineVersion());
            if (tmp != null) result.addOffline(d.getDrId(), tmp);
            if (d.getOnlineVersion() != 0) {
                tmp = ref.get(d.getDrId() + "," + d.getOnlineVersion());
                if (tmp != null) result.addOnline(d.getDrId(), tmp);
            }
        }
        return result;
    }

    @Override
    public Long[] getGroupIdsByGroupServerIp(String ip, SelectionMode mode) throws Exception {
        if (ip == null || ip.isEmpty()) return new Long[0];

        Set<Long> result = new HashSet<>();
        Set<String> range = new HashSet<>();
        for (SlbGroupGsR d : slbGroupGsRMapper.selectByExample(new SlbGroupGsRExample().createCriteria().andIpEqualTo(ip).example())) {
            result.add(d.getGroupId());
            range.add(d.getGroupId() + "," + d.getGroupVersion());
        }
        if (result.size() == 0) return result.toArray(new Long[result.size()]);
        for (SlbGroupStatusR d : slbGroupStatusRMapper.selectByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdIn(new ArrayList<Long>(result)).example())) {
            boolean contains = false;
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion(), d.getCanaryVersion())) {
                if (range.contains(d.getGroupId() + "," + v)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                result.remove(d.getGroupId());
            }
        }
        return result.toArray(new Long[result.size()]);
    }

    @Override
    public Long[] getSlbIdsByIp(String ip, SelectionMode mode) throws Exception {
        if (ip == null || ip.isEmpty()) return new Long[0];

        final Set<IdVersion> slbFilter = slbCriteriaQuery.queryBySlbServerIp(ip);
        final SelectionMode m = mode;
        return new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return slbFilter;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbFilter.size() != 0;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return slbCriteriaQuery.queryByIdsAndMode(VersionUtils.extractUniqIds(slbFilter), m);
                    }
                }).build(IdVersion.class)
                .run(new ResultHandler<IdVersion, Long>() {
                    @Override
                    public Long[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return new Long[0];
                        if (result.size() == 0) return new Long[0];
                        return VersionUtils.extractUniqIds(result);
                    }
                });
    }

    @Override
    public Long[] getVsIdsBySlbId(Long slbId, SelectionMode mode) throws Exception {
        if (slbId == null || slbId.longValue() == 0L) return new Long[0];

        final Set<IdVersion> vsFilter = virtualServerCriteriaQuery.queryBySlbId(slbId);
        final SelectionMode m = mode;
        return new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return vsFilter;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsFilter.size() != 0;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return virtualServerCriteriaQuery.queryByIdsAndMode(VersionUtils.extractUniqIds(vsFilter), m);
                    }
                }).build(IdVersion.class)
                .run(new ResultHandler<IdVersion, Long>() {
                    @Override
                    public Long[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return new Long[0];
                        if (result.size() == 0) return new Long[0];
                        return VersionUtils.extractUniqIds(result);
                    }
                });
    }
}
