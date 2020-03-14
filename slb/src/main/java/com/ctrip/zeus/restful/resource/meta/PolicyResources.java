package com.ctrip.zeus.restful.resource.meta;

import com.ctrip.zeus.model.model.TrafficPolicy;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.TrafficPolicyRepository;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ygshen on 2017/2/20.
 */
@Component
@Path("/meta/policies")
public class PolicyResources extends MetaSearchResource{
    @Resource
    private TrafficPolicyRepository repository;
    @Resource
    private TrafficPolicyQuery queryCriteria;

    StrCache cache;
    public PolicyResources() {
        cache = new StrCache();
    }

    @Override
    MetaDataCache<List<CacheItem>> getCache() {
        return cache;
    }

    class StrCache extends MetaDataCache<List<CacheItem>>{
        @Override
        List<CacheItem> queryData() throws Exception {
            Set<IdVersion> idVersionSet = queryCriteria.queryAll(SelectionMode.OFFLINE_FIRST);
            List<TrafficPolicy> list = repository.list(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
            List<CacheItem> all = new ArrayList<>(list.size());
            for (TrafficPolicy item : list) {
                all.add(new CacheItem(item.getId().toString(), item.getName(), null));
            }
            return all;
        }
    }
}
