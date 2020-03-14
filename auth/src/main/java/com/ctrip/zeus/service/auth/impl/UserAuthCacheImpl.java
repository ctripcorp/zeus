package com.ctrip.zeus.service.auth.impl;

import com.ctrip.zeus.auth.entity.DataResource;
import com.ctrip.zeus.service.auth.UserAuthCache;
import com.ctrip.zeus.service.auth.UserService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by fanqq on 2016/7/22.
 */
@Service("userAuthCache")
public class UserAuthCacheImpl implements UserAuthCache {

    @Resource
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private LoadingCache<String, Map<String, Map<String,DataResource>>> cache = CacheBuilder.newBuilder().maximumSize(300)
            .expireAfterAccess(2, TimeUnit.SECONDS)
            .build(new CacheLoader<String, Map<String, Map<String,DataResource>>> () {
                @Override
                public Map<String, Map<String,DataResource>> load(String key) throws Exception {
                    return userService.getAuthResourcesByUserName(key);
                }
            });

    @Override
    public Map<String, Map<String,DataResource>> getAuthResource(String name) throws Exception {
        try {
            return cache.get(name);
        } catch (Exception e) {
            logger.warn("Get auth resource failed. User:" + name, e);
            return null;
        }
    }

    @Override
    public DataResource getAuthResource(String name, String type, Long id) throws Exception {
        try {
            return cache.get(name).get(type).get(id);
        } catch (Exception e) {
            logger.warn("Get auth resource failed. User:" + name + " Type:" + type + " ID:" + id, e);
            return null;
        }
    }
}
