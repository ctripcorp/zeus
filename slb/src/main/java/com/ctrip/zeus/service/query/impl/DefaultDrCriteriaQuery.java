package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbDrMapper;
import com.ctrip.zeus.dao.mapper.SlbDrStatusRMapper;
import com.ctrip.zeus.dao.mapper.SlbDrVsRMapper;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.DrCriteriaQuery;
import com.ctrip.zeus.service.query.command.DrQueryCommand;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.tag.TagService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component("drCriteriaQuery")
public class DefaultDrCriteriaQuery implements DrCriteriaQuery {
    @Resource
    private SlbDrVsRMapper slbDrVsRMapper;
    @Resource
    private SlbDrStatusRMapper slbDrStatusRMapper;
    @Resource
    private SlbDrMapper slbDrMapper;
    @Resource
    private TagService tagService;

    private final static String GROUPID_PREFIX = "groupid_";
    @Override
    public Set<IdVersion> queryByVsId(Long vsId) throws Exception {
        Set<IdVersion> drs = new HashSet<>();
        for (SlbDrVsR drVsDo : slbDrVsRMapper.selectByExample(new SlbDrVsRExample().createCriteria().andVsIdEqualTo(vsId).example())) {
            drs.add(new IdVersion(drVsDo.getDrId(), drVsDo.getDrVersion()));
        }
        return drs;
    }

    @Override
    public Set<Long> queryByGroupId(Long groupId) throws Exception {
        Set<Long> drIds = new HashSet<>();
        for (Long drId : tagService.query(GROUPID_PREFIX + groupId, "dr")) {
            drIds.add(drId);
        }
        return drIds;
    }

    @Override
    public Long queryByName(String name) throws Exception {
        SlbDr dr = slbDrMapper.selectOneByExampleSelective(new SlbDrExample().createCriteria().andNameEqualTo(name).example());
        return dr == null ? 0L : dr.getId();
    }

    @Override
    public Set<Long> fuzzyQueryByName(String name) throws Exception {
        Set<Long> result = new HashSet<>();
        name = String.format("%%%s%%", name);
        for (SlbDr e : slbDrMapper.selectByExampleSelective(new SlbDrExample().createCriteria().andNameLike(name).example(), SlbDr.Column.id)) {
            result.add(e.getId());
        }
        return result;
    }

    @Override
    public IdVersion[] queryByCommand(QueryCommand query, final SelectionMode mode) throws Exception {
        final DrQueryCommand queryCommand = (DrQueryCommand) query;
        final Long[] filteredIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return queryCommand.hasValue(queryCommand.id);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : queryCommand.getValue(queryCommand.id)) {
                            result.add(Long.parseLong(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return queryCommand.hasValue(queryCommand.name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : queryCommand.getValue(queryCommand.name)) {
                            result.add(queryByName(s.trim()));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return queryCommand.hasValue(queryCommand.fuzzy_name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (String s : queryCommand.getValue(queryCommand.fuzzy_name)) {
                            result.addAll(fuzzyQueryByName(s));
                        }
                        return result;
                    }
                }).addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return queryCommand.hasValue(queryCommand.group_id);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (String s : queryCommand.getValue(queryCommand.group_id)) {
                            result.addAll(queryByGroupId(Long.parseLong(s)));
                        }
                        return result;
                    }
                }).build(Long.class).run();

        IdVersion[] result = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return filteredIds != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return queryByIdsAndMode(filteredIds, mode);
                    }
                }).addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return queryCommand.hasValue(queryCommand.vs_id);
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<IdVersion> result = new HashSet<>();
                        for (String s : queryCommand.getValue(queryCommand.vs_id)) {
                            result.addAll(queryByVsId(Long.parseLong(s)));
                        }
                        return result;
                    }
                }).build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return null;
                        if (result.size() == 0) return new IdVersion[0];
                        if (filteredIds == null) {
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

        return result;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> drIds = new HashSet<>();
        for (SlbDr dr : slbDrMapper.selectByExampleSelective(new SlbDrExample().createCriteria().example(), SlbDr.Column.id)) {
            drIds.add(dr.getId());
        }
        return drIds;
    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> drIds = queryAll();
        if (drIds.size() == 0) return result;
        for (SlbDrStatusR d : slbDrStatusRMapper.selectByExample(new SlbDrStatusRExample().createCriteria().andDrIdIn(new ArrayList<>(drIds)).example())) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getDrId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        if (ids == null || ids.length == 0) return result;

        for (SlbDrStatusR d : slbDrStatusRMapper.selectByExample(new SlbDrStatusRExample().createCriteria().andDrIdIn(Arrays.asList(ids)).example())) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getDrId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception {
        SlbDrStatusR d = slbDrStatusRMapper.selectOneByExample(new SlbDrStatusRExample().createCriteria().andDrIdEqualTo(id).example());
        if (d == null) return new IdVersion[0];

        int[] v = VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion());

        IdVersion[] result = new IdVersion[v.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new IdVersion(id, v[i]);
        }
        return result;
    }
}
