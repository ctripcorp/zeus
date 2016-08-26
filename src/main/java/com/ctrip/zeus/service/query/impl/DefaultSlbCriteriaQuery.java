package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.command.SlbQueryCommand;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/8/27.
 */
@Component("slbCriteriaQuery")
public class DefaultSlbCriteriaQuery implements SlbCriteriaQuery {
    @Resource
    private SlbDao slbDao;
    @Resource
    private RSlbSlbServerDao rSlbSlbServerDao;
    @Resource
    private RVsSlbDao rVsSlbDao;
    @Resource
    private RSlbStatusDao rSlbStatusDao;

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
        SlbDo s = slbDao.findByName(name, SlbEntity.READSET_IDONLY);
        return s == null ? 0L : s.getId();
    }

    @Override
    public Set<Long> fuzzyQueryByName(String name) throws Exception {
        name = String.format("%%%s%%", name);
        Set<Long> result = new HashSet<>();
        for (SlbDo e : slbDao.searchByName(name, SlbEntity.READSET_IDONLY)) {
            result.add(e.getId());
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelSlbStatusDo d : rSlbStatusDao.findBySlbs(ids, RSlbStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getSlbId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception {
        RelSlbStatusDo d = rSlbStatusDao.findBySlb(id, RSlbStatusEntity.READSET_FULL);
        if (d == null) return new IdVersion[0];

        int[] v = VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion());

        IdVersion[] result = new IdVersion[v.length];
        for (int i = 0; i < result.length && i < v.length; i++) {
            result[i] = new IdVersion(id, v[i]);
        }
        return result;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> slbIds = new HashSet<>();
        for (SlbDo slbDo : slbDao.findAll(SlbEntity.READSET_IDONLY)) {
            slbIds.add(slbDo.getId());
        }
        return slbIds;
    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> slbIds = queryAll();
        for (RelSlbStatusDo d : rSlbStatusDao.findBySlbs(slbIds.toArray(new Long[slbIds.size()]), RSlbStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getSlbId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryBySlbServerIp(String ip) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelSlbSlbServerDo relSlbSlbServerDo : rSlbSlbServerDao.findByIp(ip, RSlbSlbServerEntity.READSET_FULL)) {
            result.add(new IdVersion(relSlbSlbServerDo.getSlbId(), relSlbSlbServerDo.getSlbVersion()));
        }
        return result;
    }

    @Override
    public Set<Long> queryByVs(IdVersion vsIdVersion) throws Exception {
        Set<Long> result = new HashSet<>();
        for (RelVsSlbDo relVsSlbDo : rVsSlbDao.findByVs(vsIdVersion.getId(), RVsSlbEntity.READSET_FULL)) {
            if (vsIdVersion.getVersion().equals(relVsSlbDo.getVsVersion()))
                result.add(relVsSlbDo.getSlbId());
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
        for (RelVsSlbDo relVsSlbDo : rVsSlbDao.findByVses(map.values().toArray(new Long[map.size()]), RVsSlbEntity.READSET_FULL)) {
            if (result.contains(relVsSlbDo.getSlbId()))
                continue;
            if (map.keySet().contains(new IdVersion(relVsSlbDo.getVsId(), relVsSlbDo.getVsVersion())))
                result.add(relVsSlbDo.getSlbId());
        }
        return result;
    }
}
