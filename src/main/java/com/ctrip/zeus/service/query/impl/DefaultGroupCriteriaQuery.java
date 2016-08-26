package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.GroupQueryCommand;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/8/7.
 */
@Component("groupCriteriaQuery")
public class DefaultGroupCriteriaQuery implements GroupCriteriaQuery {
    @Resource
    private GroupDao groupDao;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RGroupGsDao rGroupGsDao;
    @Resource
    private RGroupVgDao rGroupVgDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    @Override
    public IdVersion[] queryByCommand(QueryCommand query, final SelectionMode mode) throws Exception {
        final GroupQueryCommand groupQuery = (GroupQueryCommand) query;
        final Long[] filteredGroupIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupQuery.hasValue(groupQuery.id);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : groupQuery.getValue(groupQuery.id)) {
                            result.add(Long.parseLong(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupQuery.hasValue(groupQuery.name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : groupQuery.getValue(groupQuery.name)) {
                            result.add(queryByName(s.trim()));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupQuery.hasValue(groupQuery.fuzzy_name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (String s : groupQuery.getValue(groupQuery.fuzzy_name)) {
                            result.addAll(fuzzyQueryByName(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupQuery.hasValue(groupQuery.app_id);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : groupQuery.getValue(groupQuery.app_id)) {
                            result.addAll(queryByAppId(s.trim()));
                        }
                        return result;
                    }
                }).build(Long.class).run();

        IdVersion[] filteredGroupKeys = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return filteredGroupIds != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<Long> groupIds = new HashSet<Long>();
                        for (Long i : filteredGroupIds) {
                            groupIds.add(i);
                        }
                        return queryByIdsAndMode(groupIds.toArray(new Long[groupIds.size()]), mode);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupQuery.hasValue(groupQuery.member_ip);
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<IdVersion> result = new HashSet<>();
                        for (String s : groupQuery.getValue(groupQuery.member_ip)) {
                            result.addAll(queryByGroupServer(s.trim()));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupQuery.hasValue(groupQuery.vs_id);
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        List<Long> vsIds = new ArrayList<Long>();
                        for (String s : groupQuery.getValue(groupQuery.vs_id)) {
                            vsIds.add(Long.parseLong(s));
                        }
                        return queryByVsIds(vsIds.toArray(new Long[vsIds.size()]));
                    }
                }).build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return null;
                        if (result.size() == 0) return new IdVersion[0];
                        if (filteredGroupIds == null) {
                            Long[] arr = new Long[result.size()];
                            int i = 0;
                            for (IdVersion e : result) {
                                arr[i] = e.getId();
                                i++;
                            }
                            result.retainAll(queryByIdsAndMode(arr, mode));
                        }
                        return result.toArray(new IdVersion[result.size()]);
                    }
                });

        return filteredGroupKeys;
    }

    @Override
    public Long queryByName(String name) throws Exception {
        GroupDo g = groupDao.findByName(name, GroupEntity.READSET_IDONLY);
        return g == null ? 0L : g.getId();
    }

    @Override
    public Set<Long> fuzzyQueryByName(String name) throws Exception {
        name = String.format("%%%s%%", name);
        Set<Long> result = new HashSet<>();
        for (GroupDo e : groupDao.searchByName(name, GroupEntity.READSET_IDONLY)) {
            result.add(e.getId());
        }
        return result;
    }

    @Override
    public Set<Long> queryByAppId(String appId) throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (GroupDo groupDo : groupDao.findByAppId(appId, GroupEntity.READSET_IDONLY)) {
            groupIds.add(groupDo.getId());
        }
        return groupIds;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (GroupDo groupDo : groupDao.findAll(GroupEntity.READSET_IDONLY)) {
            groupIds.add(groupDo.getId());
        }
        return groupIds;
    }

    @Override
    public Set<Long> queryAllVGroups() throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (RelGroupVgDo relGroupVgDo : rGroupVgDao.findAll(RGroupVgEntity.READSET_FULL)) {
            groupIds.add(relGroupVgDo.getGroupId());
        }
        return groupIds;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(ids, RGroupStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getGroupId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception {
        RelGroupStatusDo d = rGroupStatusDao.findByGroup(id, RGroupStatusEntity.READSET_FULL);
        if (d == null) return new IdVersion[0];

        int[] v = VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion());

        IdVersion[] result = new IdVersion[v.length];
        for (int i = 0; i < result.length && i < v.length; i++) {
            result[i] = new IdVersion(id, v[i]);
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> groupIds = queryAll();
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(groupIds.toArray(new Long[groupIds.size()]), RGroupStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getGroupId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryAllVGroups(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> groupIds = queryAllVGroups();
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(groupIds.toArray(new Long[groupIds.size()]), RGroupStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getGroupId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByGroupServer(String groupServer) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelGroupGsDo d : rGroupGsDao.findAllByIp(groupServer, RGroupGsEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getGroupId(), d.getGroupVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByVsId(Long vsId) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelGroupVsDo relGroupVsDo : rGroupVsDao.findAllByVs(vsId, RGroupVsEntity.READSET_FULL)) {
            result.add(new IdVersion(relGroupVsDo.getGroupId(), relGroupVsDo.getGroupVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByVsIds(Long[] vsIds) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelGroupVsDo relGroupVsDo : rGroupVsDao.findAllByVses(vsIds, RGroupVsEntity.READSET_FULL)) {
            result.add(new IdVersion(relGroupVsDo.getGroupId(), relGroupVsDo.getGroupVersion()));
        }
        return result;
    }
}
