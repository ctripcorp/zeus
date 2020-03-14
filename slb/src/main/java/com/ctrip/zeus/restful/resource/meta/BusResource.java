package com.ctrip.zeus.restful.resource.meta;

import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.service.app.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 2016/8/11.
 */
@Component
@Path("/meta/bus")
public class BusResource extends MetaSearchResource{
    @Autowired
    private AppService appService;

    StrCache cache;
    public BusResource() {
        cache = new StrCache();
    }

    @Override
    MetaDataCache<List<CacheItem>> getCache() {
        return cache;
    }

    class StrCache extends MetaDataCache<List<CacheItem>> {
        @Override
        List<CacheItem> queryData() throws Exception {
            Collection<App> list = appService.getAllAppsInSlb();
            Set<CacheItem> set = new HashSet<>(list.size());
            for (App item : list) {
                set.add(new CacheItem(item.getSbu(),item.getSbu(),PinyinUtils.getPinyin(item.getSbu())));
            }
            List<CacheItem> all = new ArrayList<>(list.size());
            all.addAll(set);
            return all;
        }
    }
}
