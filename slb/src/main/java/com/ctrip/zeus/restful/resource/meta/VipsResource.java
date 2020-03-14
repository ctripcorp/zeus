package com.ctrip.zeus.restful.resource.meta;

import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.Vip;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
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
@Path("/meta/vips")
public class VipsResource extends MetaSearchResource{
    @Resource
    private SlbRepository repository;
    @Resource
    private SlbCriteriaQuery queryCriteria;

    StrCache cache;
    public VipsResource() {
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
            List<Slb> list = repository.list(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
            List<CacheItem> all = new ArrayList<>(list.size());
            for (Slb item : list) {
                for (Vip jtem : item.getVips()) {
                    CacheItem newItem = new CacheItem(jtem.getIp(),jtem.getIp(),null);
                    if(!all.contains(newItem))
                        all.add(newItem);
                }
            }
            return all;
        }
    }
}
