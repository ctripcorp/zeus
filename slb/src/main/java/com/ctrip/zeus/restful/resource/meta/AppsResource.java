package com.ctrip.zeus.restful.resource.meta;

import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.service.app.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 2016/8/11.
 */
@Component
@Path("/meta/apps")
public class AppsResource extends MetaSearchResource {
    @Autowired
    private AppService appService;

    StrCache cache;

    public AppsResource() {
        cache = new StrCache();
    }

    @Override
    MetaDataCache<List<CacheItem>> getCache() {
        return cache;
    }

    class StrCache extends MetaDataCache<List<CacheItem>> {
        @Override
        List<CacheItem> queryData() throws Exception {
            Collection<App> apps = appService.getAllAppsInSlb();
            List<CacheItem> all = new ArrayList<>(apps.size());
            for (App item : apps) {
                all.add(new CacheItem(item.getAppId(), item.getChineseName(), PinyinUtils.getPinyin(item.getChineseName())));
            }
            return all;
        }
    }
}
