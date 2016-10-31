package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.query.command.VsQueryCommand;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/9/11.
 */
@Component("virtualServerCriteriaQuery")
public class DefaultVirtualServerCriteriaQuery implements VirtualServerCriteriaQuery {
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private RVsSlbDao rVsSlbDao;
    @Resource
    private RVsDomainDao rVsDomainDao;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    @Override
    public IdVersion[] queryByCommand(final QueryCommand query, final SelectionMode mode) throws Exception {
        final VsQueryCommand vsQuery = (VsQueryCommand) query;
        final Long[] filteredVsIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsQuery.hasValue(vsQuery.id);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : vsQuery.getValue(vsQuery.id)) {
                            result.add(Long.parseLong(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsQuery.hasValue(vsQuery.name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : vsQuery.getValue(vsQuery.name)) {
                            result.add(queryByName(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsQuery.hasValue(vsQuery.fuzzy_name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (String s : vsQuery.getValue(vsQuery.fuzzy_name)) {
                            result.addAll(fuzzyQueryByName(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsQuery.hasValue(vsQuery.ssl);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : vsQuery.getValue(vsQuery.ssl)) {
                            result.addAll(queryBySsl(Boolean.parseBoolean(s)));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsQuery.hasValue(vsQuery.group_search_key);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        List<IdVersion> searchKeys = new ArrayList<>();
                        for (String s : vsQuery.getValue(vsQuery.group_search_key)) {
                            searchKeys.add(new IdVersion(s));
                        }
                        return queryByGroup(searchKeys.toArray(new IdVersion[searchKeys.size()]));
                    }
                }).build(Long.class).run();

        IdVersion[] filteredVsKeys = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return filteredVsIds != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<Long> vsIds = new HashSet<>();
                        for (Long i : filteredVsIds) {
                            vsIds.add(i);
                        }
                        return queryByIdsAndMode(vsIds.toArray(new Long[vsIds.size()]), mode);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsQuery.hasValue(vsQuery.domain);
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Set<IdVersion> result = new HashSet<>();
                        for (String s : vsQuery.getValue(vsQuery.domain)) {
                            result.addAll(queryByDomain(s.trim()));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return vsQuery.hasValue(vsQuery.slb_id);
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        List<Long> slbIds = new ArrayList();
                        for (String s : vsQuery.getValue(vsQuery.slb_id)) {
                            slbIds.add(Long.parseLong(s));
                        }
                        return queryBySlbIds(slbIds.toArray(new Long[slbIds.size()]));
                    }
                }).build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return null;
                        if (result.size() == 0) return new IdVersion[0];
                        if (filteredVsIds == null) {
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

        return filteredVsKeys;
    }

    @Override
    public Long queryByName(String name) throws Exception {
        SlbVirtualServerDo d = slbVirtualServerDao.findByName(name, SlbVirtualServerEntity.READSET_IDONLY);
        return d == null ? 0L : d.getId();
    }

    @Override
    public Set<Long> fuzzyQueryByName(String name) throws Exception {
        name = String.format("%%%s%%", name);
        Set<Long> result = new HashSet<>();
        for (SlbVirtualServerDo d : slbVirtualServerDao.searchByName(name, SlbVirtualServerEntity.READSET_IDONLY)) {
            result.add(d.getId());
        }
        return result;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbVirtualServerDo slbVirtualServerDo : slbVirtualServerDao.findAll(SlbVirtualServerEntity.READSET_IDONLY)) {
            result.add(slbVirtualServerDo.getId());
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> vsIds = queryAll();
        for (RelVsStatusDo d : rVsStatusDao.findByVses(vsIds.toArray(new Long[vsIds.size()]), RVsStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getVsId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsStatusDo d : rVsStatusDao.findByVses(ids, RVsStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getVsId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception {
        RelVsStatusDo d = rVsStatusDao.findByVs(id, RVsStatusEntity.READSET_FULL);
        if (d == null) return new IdVersion[0];

        int[] v = VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion());

        IdVersion[] result = new IdVersion[v.length];
        for (int i = 0; i < result.length && i < v.length; i++) {
            result[i] = new IdVersion(id, v[i]);
        }
        return result;
    }

    @Override
    public Set<Long> queryByGroup(IdVersion[] searchKeys) throws Exception {
        Set<Long> result = new HashSet<>();
        Map<IdVersion, Long> map = new HashMap();
        for (IdVersion sk : searchKeys) {
            map.put(sk, sk.getId());
        }
        for (RelGroupVsDo e : rGroupVsDao.findAllByGroups(map.values().toArray(new Long[map.size()]), RGroupVsEntity.READSET_FULL)) {
            if (result.contains(e.getVsId()))
                continue;
            if (map.keySet().contains(new IdVersion(e.getGroupId(), e.getGroupVersion())))
                result.add(e.getVsId());
        }
        return result;
    }

    @Override
    public Set<Long> queryBySsl(boolean ssl) throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbVirtualServerDo e : slbVirtualServerDao.findAllBySsl(ssl, SlbVirtualServerEntity.READSET_IDONLY)) {
            result.add(e.getId());
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryBySlbId(Long slbId) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsSlbDo d : rVsSlbDao.findAllBySlb(slbId, RVsSlbEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getVsId(), d.getVsVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryBySlbIds(Long[] slbIds) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsSlbDo d : rVsSlbDao.findAllBySlbs(slbIds, RVsSlbEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getVsId(), d.getVsVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByDomain(String domain) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsDomainDo d : rVsDomainDao.findAllByDomain(domain.toLowerCase(), RVsDomainEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getVsId(), d.getVsVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByDomains(String[] domains) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsDomainDo d : rVsDomainDao.findAllByDomains(domains, RVsDomainEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getVsId(), d.getVsVersion()));
        }
        return result;
    }
}
