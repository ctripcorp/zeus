package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbSlbMapper;
import com.ctrip.zeus.dao.mapper.SlbSlbServerRMapper;
import com.ctrip.zeus.dao.mapper.SlbSlbStatusRMapper;
import com.ctrip.zeus.dao.mapper.SlbVsSlbRMapper;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.SlbQueryCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/8/27.
 */
@Component("slbCriteriaQuery")
public class DefaultSlbCriteriaQuery implements SlbCriteriaQuery {
    @Resource
    private SlbSlbMapper slbSlbMapper;
    @Resource
    private SlbSlbServerRMapper slbSlbServerRMapper;
    @Resource
    private SlbVsSlbRMapper slbVsSlbRMapper;
    @Resource
    private SlbSlbStatusRMapper slbSlbStatusRMapper;
    @Override
    public IdVersion[] queryByCommand(QueryCommand query, final SelectionMode mode) throws Exception {
        final SlbQueryCommand slbQuery = (SlbQueryCommand) query;
        final Long[] filteredSlbIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbQuery.hasValue(slbQuery.id);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : slbQuery.getValue(slbQuery.id)) {
                            result.add(Long.parseLong(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbQuery.hasValue(slbQuery.name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (String s : slbQuery.getValue(slbQuery.name)) {
                            result.add(queryByName(s.trim()));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbQuery.hasValue(slbQuery.fuzzy_name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (String s : slbQuery.getValue(slbQuery.fuzzy_name)) {
                            result.addAll(fuzzyQueryByName(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbQuery.hasValue(slbQuery.vs_search_key);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        List<IdVersion> searchKey = new ArrayList<>();
                        for (String s : slbQuery.getValue(slbQuery.vs_search_key)) {
                            searchKey.add(new IdVersion(s));
                        }
                        return queryByVses(searchKey.toArray(new IdVersion[searchKey.size()]));
                    }
                }).build(Long.class).run();

        IdVersion[] filteredSlbKeys = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return filteredSlbIds != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        List<Long> slbIds = new ArrayList<>();
                        for (Long i : filteredSlbIds) {
                            slbIds.add(i);
                        }
                        return queryByIdsAndMode(slbIds.toArray(new Long[slbIds.size()]), mode);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbQuery.hasValue(slbQuery.ip);
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<IdVersion> result = new HashSet<>();
                        for (String s : slbQuery.getValue(slbQuery.ip)) {
                            result.addAll(queryBySlbServerIp(s.trim()));
                        }
                        return result;
                    }
                }).build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return null;
                        if (result.size() == 0) return new IdVersion[0];
                        if (filteredSlbIds == null) {
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

        return filteredSlbKeys;
    }

    @Override
    public Long queryByName(String name) throws Exception {
        SlbSlb s = slbSlbMapper.selectOneByExample(new SlbSlbExample().createCriteria().andNameEqualTo(name).example());
        return s == null ? 0L : s.getId();
    }

    @Override
    public Set<Long> fuzzyQueryByName(String name) throws Exception {
        Set<Long> result = new HashSet<>();

        name = String.format("%%%s%%", name);
        for (SlbSlb e : slbSlbMapper.selectByExampleSelective(new SlbSlbExample().
                        createCriteria().
                        andNameLike(name).
                        example(),
                SlbSlb.Column.id)) {
            result.add(e.getId());
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        if (ids == null || ids.length == 0) return result;
        for (SlbSlbStatusR slbSlbStatusR : slbSlbStatusRMapper.selectByExample(new SlbSlbStatusRExample().createCriteria().andSlbIdIn(Arrays.asList(ids)).example())) {
            for (int v : VersionUtils.getVersionByMode(mode, slbSlbStatusR.getOfflineVersion(), slbSlbStatusR.getOnlineVersion())) {
                result.add(new IdVersion(slbSlbStatusR.getSlbId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception {
        SlbSlbStatusR slbSlbStatusR = slbSlbStatusRMapper.selectOneByExample(new SlbSlbStatusRExample().createCriteria().andSlbIdEqualTo(id).example());
        if (slbSlbStatusR == null) return new IdVersion[0];
        int[] v = VersionUtils.getVersionByMode(mode, slbSlbStatusR.getOfflineVersion(), slbSlbStatusR.getOnlineVersion());

        IdVersion[] result = new IdVersion[v.length];
        for (int i = 0; i < result.length && i < v.length; i++) {
            result[i] = new IdVersion(id, v[i]);
        }
        return result;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> slbIds = new HashSet<>();
        for (SlbSlb slb : slbSlbMapper.selectByExampleSelective(new SlbSlbExample().createCriteria().example(), SlbSlb.Column.id)) {
            slbIds.add(slb.getId());
        }
        return slbIds;
    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> slbIds = queryAll();
        if (slbIds.size() > 0) {
            for (SlbSlbStatusR slbSlbStatusR : slbSlbStatusRMapper.selectByExample(new SlbSlbStatusRExample().createCriteria().andSlbIdIn(new ArrayList<>(slbIds)).example())) {
                for (int v : VersionUtils.getVersionByMode(mode, slbSlbStatusR.getOfflineVersion(), slbSlbStatusR.getOnlineVersion())) {
                    result.add(new IdVersion(slbSlbStatusR.getSlbId(), v));
                }
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryBySlbServerIp(String ip) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (SlbSlbServerR server : slbSlbServerRMapper.selectByExample(new SlbSlbServerRExample().
                createCriteria().
                andIpEqualTo(ip).
                example())) {
            result.add(new IdVersion(server.getSlbId(), server.getSlbVersion()));
        }

        return result;
    }



    @Override
    public Set<Long> queryByVs(IdVersion vsIdVersion) throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbVsSlbR slbVsSlbR : slbVsSlbRMapper.selectByExample(new SlbVsSlbRExample().createCriteria().andVsIdEqualTo(vsIdVersion.getId()).example())) {
            if (vsIdVersion.getVersion().equals(slbVsSlbR.getVsVersion()))
                result.add(slbVsSlbR.getSlbId());
        }
        return result;
    }

    @Override
    public Set<Long> queryByVses(IdVersion[] vsIdVersions) throws Exception {
        Set<Long> result = new HashSet<>();
        Map<IdVersion, Long> map = new HashMap();
        for (IdVersion vsIdVersion : vsIdVersions) {
            map.put(vsIdVersion, vsIdVersion.getId());
        }
        if (map.values().size() > 0) {
            for (SlbVsSlbR slbVsSlbR : slbVsSlbRMapper.selectByExample(new SlbVsSlbRExample().createCriteria().andVsIdIn(new ArrayList<>(map.values())).example())) {
                if (result.contains(slbVsSlbR.getSlbId()))
                    continue;
                if (map.keySet().contains(new IdVersion(slbVsSlbR.getVsId(), slbVsSlbR.getVsVersion())))
                    result.add(slbVsSlbR.getSlbId());
            }
        }
        return result;
    }

    @Override
    public Map<Long, Set<Long>> batchQueryByVses(IdVersion[] keys) throws Exception {
        if (keys == null || keys.length == 0) {
            return new HashMap<>();
        }
        Map<Long, Set<Long>> slbIdsByVs = new HashMap<>();

        List<Long> vsIds = new ArrayList<>(keys.length);
        for (IdVersion vsKey : keys) {
            vsIds.add(vsKey.getId());
        }
        List<SlbVsSlbR> vsSlbRecords = slbVsSlbRMapper.selectByExample(SlbVsSlbRExample.newAndCreateCriteria().andVsIdIn(vsIds).example());
        vsSlbRecords.forEach(record -> {
            slbIdsByVs.putIfAbsent(record.getVsId(), new HashSet<>());
            slbIdsByVs.get(record.getVsId()).add(record.getSlbId());
        });

        return slbIdsByVs;
    }
}
