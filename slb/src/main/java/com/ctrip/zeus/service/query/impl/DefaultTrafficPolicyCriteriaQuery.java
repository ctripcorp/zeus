package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyGroupRMapper;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyMapper;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyVsRMapper;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.TrafficPolicyCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhoumy on 2017/1/18.
 */
@Service("trafficPolicyQuery")
public class DefaultTrafficPolicyCriteriaQuery implements TrafficPolicyQuery {
    @Resource
    private SlbTrafficPolicyMapper slbTrafficPolicyMapper;

    @Resource
    private SlbTrafficPolicyGroupRMapper slbTrafficPolicyGroupRMapper;

    @Resource
    private SlbTrafficPolicyVsRMapper slbTrafficPolicyVsRMapper;

    @Override
    public Long queryByName(String name) throws Exception {
        SlbTrafficPolicy d = slbTrafficPolicyMapper.selectOneByExampleSelective(new SlbTrafficPolicyExample().createCriteria().andNameEqualTo(name).example());
        return d == null ? 0L : d.getId();
    }

    @Override
    public Set<Long> fuzzyQueryByName(String name) throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbTrafficPolicy e : slbTrafficPolicyMapper.selectByExampleSelective(new SlbTrafficPolicyExample().createCriteria().andNameLike(name).example(), SlbTrafficPolicy.Column.id)) {
            result.add(e.getId());
        }
        return result;
    }

    @Override
    public IdVersion[] queryByCommand(QueryCommand query, final SelectionMode mode) throws Exception {
        final TrafficPolicyCommand policyQuery = (TrafficPolicyCommand) query;
        final Long[] filteredIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return policyQuery.hasValue(policyQuery.id);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : policyQuery.getValue(policyQuery.id)) {
                            result.add(Long.parseLong(s));
                        }
                        return result;
                    }
                }).addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return policyQuery.hasValue(policyQuery.name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : policyQuery.getValue(policyQuery.name)) {
                            result.add(queryByName(s));
                        }
                        return result;
                    }
                }).addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return policyQuery.hasValue(policyQuery.fuzzy_name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : policyQuery.getValue(policyQuery.fuzzy_name)) {
                            result.addAll(fuzzyQueryByName(s));
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
                        return policyQuery.hasValue(policyQuery.group_id);
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<IdVersion> result = new HashSet<>();
                        for (String s : policyQuery.getValue(policyQuery.group_id)) {
                            result.addAll(queryByGroupId(Long.parseLong(s)));
                        }
                        return result;
                    }
                }).addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return policyQuery.hasValue(policyQuery.vs_id);
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<IdVersion> result = new HashSet<>();
                        for (String s : policyQuery.getValue(policyQuery.vs_id)) {
                            result.addAll(queryByVsId(Long.parseLong(s)));
                        }
                        return result;
                    }
                }).build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return null;
                        if (result.size() == 0) return new IdVersion[0];
                        Set<Long> ids = new HashSet<>();
                        for (IdVersion e : result) {
                            ids.add(e.getId());
                        }
                        result.retainAll(queryByIdsAndMode(ids.toArray(new Long[ids.size()]), mode));
                        return result.toArray(new IdVersion[result.size()]);
                    }
                });

        return result;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbTrafficPolicy e : slbTrafficPolicyMapper.selectByExampleSelective(new SlbTrafficPolicyExample().createCriteria().example(), SlbTrafficPolicy.Column.id)) {
            result.add(e.getId());
        }
        return result;

    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (SlbTrafficPolicy e : slbTrafficPolicyMapper.selectByExample(new SlbTrafficPolicyExample().createCriteria().example())) {
            for (int i : VersionUtils.getVersionByMode(mode, e.getNxActiveVersion(), e.getActiveVersion())) {
                result.add(new IdVersion(e.getId(), i));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        if (ids == null || ids.length == 0) return result;
        for (SlbTrafficPolicy e : slbTrafficPolicyMapper.selectByExample(new SlbTrafficPolicyExample().createCriteria().andIdIn(Arrays.asList(ids)).example())) {
            for (int i : VersionUtils.getVersionByMode(mode, e.getNxActiveVersion(), e.getActiveVersion())) {
                result.add(new IdVersion(e.getId(), i));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception {
        SlbTrafficPolicy d = slbTrafficPolicyMapper.selectOneByExample(new SlbTrafficPolicyExample().createCriteria().andIdEqualTo(id).example());
        int[] v = VersionUtils.getVersionByMode(mode, d.getNxActiveVersion(), d.getActiveVersion());

        IdVersion[] result = new IdVersion[v.length];
        for (int i = 0; i < v.length; i++) {
            result[i] = new IdVersion(id, v[i]);
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByVsId(Long vsId) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> policyId = new HashSet<>();
        for (SlbTrafficPolicyVsR e : slbTrafficPolicyVsRMapper.selectByExample(new SlbTrafficPolicyVsRExample().createCriteria().andVsIdEqualTo(vsId).example())) {
            result.add(new IdVersion(e.getPolicyId(), e.getPolicyVersion()));
            policyId.add(e.getPolicyId());
        }
        if (policyId.size() == 0) return result;

        Set<IdVersion> inUse = new HashSet<>();
        for (SlbTrafficPolicy e : slbTrafficPolicyMapper.selectByExample(new SlbTrafficPolicyExample().createCriteria().andIdIn(new ArrayList<>(policyId)).example())) {
            if (e.getActiveVersion() > 0) {
                inUse.add(new IdVersion(e.getId(), e.getActiveVersion()));
            }
            inUse.add(new IdVersion(e.getId(), e.getNxActiveVersion()));
        }

        result.retainAll(inUse);
        return result;
    }

    @Override
    public Set<IdVersion> queryByGroupId(Long groupId) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> policyId = new HashSet<>();
        for (SlbTrafficPolicyGroupR e : slbTrafficPolicyGroupRMapper.selectByExample(new SlbTrafficPolicyGroupRExample().createCriteria().andGroupIdEqualTo(groupId).example())) {
            result.add(new IdVersion(e.getPolicyId(), e.getPolicyVersion()));
            policyId.add(e.getPolicyId());
        }

        Set<IdVersion> inUse = new HashSet<>();
        if (policyId.size() == 0) return result;

        for (SlbTrafficPolicy e : slbTrafficPolicyMapper.selectByExample(new SlbTrafficPolicyExample().createCriteria().andIdIn(new ArrayList<>(policyId)).example())) {
            if (e.getActiveVersion() > 0) {
                inUse.add(new IdVersion(e.getId(), e.getActiveVersion()));
            }
            inUse.add(new IdVersion(e.getId(), e.getNxActiveVersion()));
        }

        result.retainAll(inUse);
        return result;
    }
}
