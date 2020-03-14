package com.ctrip.zeus.restful.resource.meta;

import com.ctrip.zeus.model.model.Dr;
import com.ctrip.zeus.service.model.DrRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.DrCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Path("/meta/drs")
public class DrResources extends MetaSearchResource {
    @Resource
    private DrRepository repository;
    @Resource
    private DrCriteriaQuery queryCriteria;

    StrCache cache;

    public DrResources() {
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
            List<Dr> list = repository.list(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
            List<CacheItem> all = new ArrayList<>(list.size());
            for (Dr item : list) {
                all.add(new CacheItem(item.getId().toString(), item.getName(), null));
            }
            return all;
        }
    }
}
