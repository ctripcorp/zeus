package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.restful.filter.FilterSet;
import com.ctrip.zeus.restful.filter.QueryExecuter;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.ModelMode;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

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
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    @Override
    public ModelStatusMapping<Group> getByVsIds(Long[] vsIds) throws Exception {
        ModelStatusMapping<Group> result = new ModelStatusMapping<>();
        Set<Long> groupIds = new HashSet<>();
        Map<String, Group> mapping = new HashMap<>();
        for (ArchiveGroupDo d : archiveGroupDao.findAllByVsIds(vsIds, ArchiveGroupEntity.READSET_FULL)) {
            groupIds.add(d.getGroupId());
            Group g = ContentReaders.readGroupContent(d.getContent());
            for (GroupVirtualServer e : g.getGroupVirtualServers()) {
                e.setVirtualServer(new VirtualServer().setId(e.getVirtualServer().getId()));
            }
            mapping.put(d.getGroupId() + "," + d.getVersion(), g);
        }
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(groupIds.toArray(new Long[groupIds.size()]), RGroupStatusEntity.READSET_FULL)) {
            if (d.getOnlineVersion() == 0 || d.getOnlineVersion() != d.getOfflineVersion()) {
                result.addOffline(d.getGroupId(), mapping.get(d.getGroupId() + "," + d.getOfflineVersion()));
            }
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getGroupId(), mapping.get(d.getGroupId() + "," + d.getOnlineVersion()));
            }
        }
        return result;
    }

    @Override
    public ModelStatusMapping<VirtualServer> getBySlbIds(Long slbId) throws Exception {
        ModelStatusMapping<VirtualServer> result = new ModelStatusMapping<>();
        Set<Long> vsIds = new HashSet<>();
        Map<String, VirtualServer> mapping = new HashMap<>();

        for (MetaVsArchiveDo d : archiveVsDao.findAllBySlbId(slbId, ArchiveVsEntity.READSET_FULL)) {
            vsIds.add(d.getVsId());
            VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
            mapping.put(vs.getId() + "," + d.getVersion(), vs);
        }
        for (RelVsStatusDo d : rVsStatusDao.findByVses(vsIds.toArray(new Long[vsIds.size()]), RVsStatusEntity.READSET_FULL)) {
            if (d.getOnlineVersion() == 0 || d.getOnlineVersion() != d.getOfflineVersion()) {
                result.addOffline(d.getVsId(), mapping.get(d.getVsId() + "," + d.getOfflineVersion()));
            }
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getVsId(), mapping.get(d.getVsId() + "," + d.getOnlineVersion()));
            }
        }
        return result;
    }

    @Override
    public Long[] getSlbIdsByIp(String ip, ModelMode mode) throws Exception {
        final Set<IdVersion> range = slbCriteriaQuery.queryBySlbServerIp(ip);
        final ModelMode m = mode;
        IdVersion[] keys = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return range;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<Long> slbIds = new HashSet<>();
                        for (IdVersion r : range) {
                            slbIds.add(r.getId());
                        }
                        return slbCriteriaQuery.queryByIdsAndMode(slbIds.toArray(new Long[slbIds.size()]), m);
                    }
                }).build(IdVersion.class).run();

        Set<Long> result = new HashSet<>();
        for (IdVersion key : keys) {
            result.add(key.getId());
        }
        return result.toArray(new Long[result.size()]);
    }

    @Override
    public Long[] getVsIdsBySlbId(Long slbId, ModelMode mode) throws Exception {
        final Set<IdVersion> range = virtualServerCriteriaQuery.queryBySlbId(slbId);
        final ModelMode m = mode;
        IdVersion[] keys = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return range;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<Long> vsIds = new HashSet<>();
                        for (IdVersion r : range) {
                            vsIds.add(r.getId());
                        }
                        return virtualServerCriteriaQuery.queryByIdsAndMode(vsIds.toArray(new Long[vsIds.size()]), m);
                    }
                }).build(IdVersion.class).run();

        Set<Long> result = new HashSet<>();
        for (IdVersion key : keys) {
            result.add(key.getId());
        }
        return result.toArray(new Long[result.size()]);
    }

    @Override
    public Long[] getGroupIdsByVsIds(Long[] vsIds, ModelMode mode) throws Exception {
        final Set<IdVersion> range = groupCriteriaQuery.queryByVsIds(vsIds);
        final ModelMode m = mode;
        IdVersion[] keys = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return range;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<Long> groupIds = new HashSet<>();
                        for (IdVersion r : range) {
                            groupIds.add(r.getId());
                        }
                        return virtualServerCriteriaQuery.queryByIdsAndMode(groupIds.toArray(new Long[groupIds.size()]), m);
                    }
                }).build(IdVersion.class).run();

        Set<Long> result = new HashSet<>();
        for (IdVersion key : keys) {
            result.add(key.getId());
        }
        return result.toArray(new Long[result.size()]);
    }
}
