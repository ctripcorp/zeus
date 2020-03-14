package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.*;
import com.ctrip.zeus.domain.GroupType;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.service.SmartGroupStatusRMapper;
import com.ctrip.zeus.service.SmartGroupVsRMapper;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.command.GroupQueryCommand;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.tag.ItemTypes;
import com.ctrip.zeus.tag.PropertyNames;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/8/7.
 */
@Component("groupCriteriaQuery")
public class DefaultGroupCriteriaQuery implements GroupCriteriaQuery {
    @Resource
    private SlbGroupMapper slbGroupMapper;

    @Resource
    private SlbGroupVsRMapper slbGroupVsRMapper;

    @Resource
    private SlbGroupGsRMapper slbGroupGsRMapper;

    @Resource
    private SlbGroupStatusRMapper slbGroupStatusRMapper;

    @Resource
    private TagPropertyMapper tagPropertyMapper;

    @Resource
    private TagPropertyItemRMapper tagPropertyItemRMapper;

    @Resource
    private SmartGroupStatusRMapper smartGroupStatusRMapper;

    @Resource
    private SmartGroupVsRMapper smartGroupVsRMapper;

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
        return filterGroupKeys(filteredGroupIds, mode, groupQuery);
    }

    private IdVersion[] filterGroupKeys(final Long[] filteredGroupIds, final SelectionMode mode, final GroupQueryCommand groupQuery) throws Exception {
        return new QueryExecuter.Builder<IdVersion>()
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
    }

    @Override
    public Long queryByName(String name) throws Exception {
        SlbGroup g = slbGroupMapper.selectOneByExampleSelective(new SlbGroupExample().createCriteria().andNameEqualTo(name).example(), SlbGroup.Column.id);
        return g == null ? 0L : g.getId();
    }

    @Override
    public Set<Long> fuzzyQueryByName(String name) throws Exception {
        Set<Long> result = new HashSet<>();
        name = String.format("%%%s%%", name);
        for (SlbGroup e : slbGroupMapper.selectByExampleSelective(new SlbGroupExample().createCriteria().andNameLike(name).example(), SlbGroup.Column.id)) {
            result.add(e.getId());
        }
        return result;
    }

    @Override
    public Set<Long> queryByAppId(String appId) throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (SlbGroup e : slbGroupMapper.selectByExampleSelective(new SlbGroupExample().createCriteria().andAppIdEqualTo(appId).example(), SlbGroup.Column.id)) {
            groupIds.add(e.getId());
        }
        return groupIds;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (SlbGroup e : slbGroupMapper.selectByExampleSelective(new SlbGroupExample().createCriteria().example(), SlbGroup.Column.id)) {
            groupIds.add(e.getId());
        }

        return groupIds;
    }

    @Override
    public Set<Long> queryAllVGroups() throws Exception {
        TagProperty d = tagPropertyMapper.selectOneByExample(new TagPropertyExample().createCriteria()
                .andPropertyNameEqualTo(PropertyNames.GROUP_TYPE).andPropertyValueEqualTo(GroupType.VGROUP.toString()).example());
        if (d == null) {
            return Collections.emptySet();
        }

        List<TagPropertyItemR> propertyItemDos = tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria()
                .andPropertyIdEqualTo(d.getId()).andTypeEqualTo(ItemTypes.GROUP).example());
        Set<Long> groupIds = new HashSet<>(propertyItemDos.size());
        for (TagPropertyItemR propertyItemDo : propertyItemDos) {
            groupIds.add(propertyItemDo.getItemId());
        }
        return groupIds;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        if (ids == null || ids.length == 0) return result;
        for (SlbGroupStatusR d : smartGroupStatusRMapper.selectByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdIn(Arrays.asList(ids)).example())) {
                for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion(), d.getCanaryVersion())) {
                result.add(new IdVersion(d.getGroupId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception {
        SlbGroupStatusR d = slbGroupStatusRMapper.selectOneByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdEqualTo(id).example());
        if (d == null) return new IdVersion[0];

        int[] v = VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion(), d.getCanaryVersion());

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

        if (groupIds == null || groupIds.size() == 0) return result;
        for (SlbGroupStatusR d : smartGroupStatusRMapper.selectByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdIn(new ArrayList<>(groupIds)).example())) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion(), d.getCanaryVersion())) {
                result.add(new IdVersion(d.getGroupId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryAllVGroups(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> groupIds = queryAllVGroups();
        if (groupIds.size() == 0) return result;
        for (SlbGroupStatusR d : smartGroupStatusRMapper.selectByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdIn(new ArrayList<>(groupIds)).example())) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion(), d.getCanaryVersion())) {
                result.add(new IdVersion(d.getGroupId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByGroupServer(String groupServer) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (SlbGroupGsR d : slbGroupGsRMapper.selectByExample(new SlbGroupGsRExample().createCriteria().andIpEqualTo(groupServer).example())) {
            result.add(new IdVersion(d.getGroupId(), d.getGroupVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByVsId(Long vsId) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (SlbGroupVsR slbGroupVsR : slbGroupVsRMapper.selectByExample(new SlbGroupVsRExample().createCriteria().andVsIdEqualTo(vsId).example())) {
            result.add(new IdVersion(slbGroupVsR.getGroupId(), slbGroupVsR.getGroupVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByVsIds(Long[] vsIds) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        if (vsIds == null || vsIds.length == 0) return result;
        for (SlbGroupVsR slbGroupVsR : smartGroupVsRMapper.selectByExample(new SlbGroupVsRExample().createCriteria().andVsIdIn(Arrays.asList(vsIds)).example())) {
            result.add(new IdVersion(slbGroupVsR.getGroupId(), slbGroupVsR.getGroupVersion()));
        }

        return result;
    }
}
