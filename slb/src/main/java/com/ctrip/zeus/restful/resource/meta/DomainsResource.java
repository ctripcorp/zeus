package com.ctrip.zeus.restful.resource.meta;

import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 2016/8/11.
 */
@Component
@Path("/meta/domains")
public class DomainsResource extends MetaSearchResource{
    @Resource
    private VirtualServerRepository repository;
    @Resource
    private VirtualServerCriteriaQuery queryCriteria;

    StrCache cache;
    public DomainsResource() {
        cache = new StrCache();
    }

    @Override
    MetaDataCache<List<CacheItem>> getCache() {
        return cache;
    }

    class StrCache extends MetaDataCache<List<CacheItem>> {
        @Override
        List<CacheItem> queryData() throws Exception {
            Set<IdVersion> idVersionSet = queryCriteria.queryAll(SelectionMode.OFFLINE_FIRST);
            List<VirtualServer> list = repository.listAll(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
            Set<CacheItem> set = new HashSet<>(list.size());
            for (VirtualServer item : list) {
                for (Domain jtem : item.getDomains()) {
                    set.add(new CacheItem(jtem.getName(),jtem.getName(),null));
                }
            }
            List<CacheItem> all = new ArrayList<>(list.size());
            all.addAll(set);
            return all;
        }
    }
}
