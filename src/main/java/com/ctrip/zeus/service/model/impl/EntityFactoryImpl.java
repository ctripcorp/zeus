package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
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
    private ArchiveGroupDao archiveGroupDao;
    @Resource
    private ArchiveVsDao archiveVsDao;
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private RGroupGsDao rGroupGsDao;
    @Resource
    private RVsStatusDao rVsStatusDao;
    @Resource
    private RSlbStatusDao rSlbStatusDao;

    @Resource
    private AutoFiller autoFiller;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    @Override
    public ModelStatusMapping<Group> getGroupsByVsIds(Long[] vsIds) throws Exception {
        if (vsIds == null || vsIds.length == 0) return new ModelStatusMapping<>();

        Set<Long> groupIds = new HashSet<>();
        Map<String, Group> ref = new HashMap<>();

        for (ArchiveGroupDo d : archiveGroupDao.findAllByVsIds(vsIds, ArchiveGroupEntity.READSET_FULL)) {
            groupIds.add(d.getGroupId());
            Group g = ContentReaders.readGroupContent(d.getContent());
            for (GroupVirtualServer e : g.getGroupVirtualServers()) {
                e.setVirtualServer(new VirtualServer().setId(e.getVirtualServer().getId()));
            }
            autoFiller.autofill(g);
            ref.put(d.getGroupId() + "," + d.getVersion(), g);
        }

        ModelStatusMapping<Group> result = new ModelStatusMapping<>();
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(groupIds.toArray(new Long[groupIds.size()]), RGroupStatusEntity.READSET_FULL)) {
            Group tmp = ref.get(d.getGroupId() + "," + d.getOfflineVersion());
            if (tmp != null) result.addOffline(d.getGroupId(), tmp);
            if (d.getOnlineVersion() != 0) {
                tmp = ref.get(d.getGroupId() + "," + d.getOnlineVersion());
                if (tmp != null) result.addOnline(d.getGroupId(), tmp);
            }
        }
        return result;
    }

    @Override
    public ModelStatusMapping<VirtualServer> getVsesBySlbIds(Long slbId) throws Exception {
        if (slbId == null) return new ModelStatusMapping<>();

        Set<Long> vsIds = new HashSet<>();
        Map<String, VirtualServer> ref = new HashMap<>();

        for (MetaVsArchiveDo d : archiveVsDao.findAllBySlbId(slbId, ArchiveVsEntity.READSET_FULL)) {
            vsIds.add(d.getVsId());
            VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
            ref.put(vs.getId() + "," + d.getVersion(), vs);
        }

        ModelStatusMapping<VirtualServer> result = new ModelStatusMapping<>();
        for (RelVsStatusDo d : rVsStatusDao.findByVses(vsIds.toArray(new Long[vsIds.size()]), RVsStatusEntity.READSET_FULL)) {
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
        for (ArchiveSlbDo d : archiveSlbDao.findVersionizedByIds(slbIds, ArchiveSlbEntity.READSET_FULL)) {
            Slb slb = ContentReaders.readSlbContent(d.getContent());
            ref.put(slb.getId() + "," + slb.getVersion(), slb);
        }

        ModelStatusMapping<Slb> result = new ModelStatusMapping<>();
        for (RelSlbStatusDo d : rSlbStatusDao.findBySlbs(slbIds, RSlbStatusEntity.READSET_FULL)) {
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
        for (MetaVsArchiveDo d : archiveVsDao.findVersionizedByIds(vsIds, ArchiveVsEntity.READSET_FULL)) {
            VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
            ref.put(vs.getId() + "," + d.getVersion(), vs);
        }

        ModelStatusMapping<VirtualServer> result = new ModelStatusMapping<>();
        for (RelVsStatusDo d : rVsStatusDao.findByVses(vsIds, RVsStatusEntity.READSET_FULL)) {
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

        Map<String, Group> ref = new HashMap<>();
        for (ArchiveGroupDo d : archiveGroupDao.findVersionizedByIds(groupIds, ArchiveGroupEntity.READSET_FULL)) {
            Group g = ContentReaders.readGroupContent(d.getContent());
            for (GroupVirtualServer e : g.getGroupVirtualServers()) {
                e.setVirtualServer(new VirtualServer().setId(e.getVirtualServer().getId()));
            }
            autoFiller.autofill(g);
            ref.put(d.getGroupId() + "," + d.getVersion(), g);
        }

        ModelStatusMapping<Group> result = new ModelStatusMapping<>();
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(groupIds, RGroupStatusEntity.READSET_FULL)) {
            result.addOffline(d.getGroupId(), ref.get(d.getGroupId() + "," + d.getOfflineVersion()));
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getGroupId(), ref.get(d.getGroupId() + "," + d.getOnlineVersion()));
            }
        }
        return result;
    }

    @Override
    public Long[] getGroupIdsByGroupServerIp(String ip, SelectionMode mode) throws Exception {
        if (ip == null || ip.isEmpty()) return new Long[0];

        Set<Long> result = new HashSet<>();
        Set<String> range = new HashSet<>();
        for (RelGroupGsDo d : rGroupGsDao.findAllByIp(ip, RGroupGsEntity.READSET_FULL)) {
            result.add(d.getGroupId());
            range.add(d.getGroupId() + "," + d.getGroupVersion());
        }
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(result.toArray(new Long[result.size()]), RGroupStatusEntity.READSET_FULL)) {
            boolean contains = false;
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
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
