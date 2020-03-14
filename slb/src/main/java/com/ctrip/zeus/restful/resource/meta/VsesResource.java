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
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 2016/8/11.
 */
@Component
@Path("/meta/vses")
public class VsesResource extends MetaSearchResource {
    @Resource
    private VirtualServerRepository repository;
    @Resource
    private VirtualServerCriteriaQuery queryCriteria;

    StrCache cache;

    public VsesResource() {
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
            List<CacheItem> all = new ArrayList<>(list.size());
            for (VirtualServer item : list) {
                StringBuilder ds = new StringBuilder(128);
                for (Domain d : item.getDomains()) {
                    ds.append(d.getName()).append("; ");
                }

                all.add(new CacheItem(item.getId().toString(), item.getName() + " : " + ds.toString(), null));
            }
            return all;
        }
    }
}
