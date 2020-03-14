package com.ctrip.zeus.restful.resource.meta;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.service.auth.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ygshen on 2016/12/5.
 */
@Component
@Path("/meta/users")
public class UsersResource extends MetaSearchResource{
    @Resource
    private UserService userService;

    StrCache cache;
    public UsersResource() {
        cache = new StrCache();
    }

    @Override
    MetaDataCache<List<CacheItem>> getCache() {
        return cache;
    }

    class StrCache extends MetaDataCache<List<CacheItem>> {
        @Override
        List<CacheItem> queryData() throws Exception {
            List<User> list = userService.getUsersSimpleInfo();
            List<CacheItem> all = new ArrayList<>(list.size());
            for (User item : userService.getUsers()) {
                String chineseName=(item.getChineseName()!=null ?item.getChineseName():"-");
                all.add( new CacheItem(item.getId().toString(), item.getUserName(), PinyinUtils.getPinyin(item.getUserName()),chineseName));
            }
            return all;
        }
    }
}