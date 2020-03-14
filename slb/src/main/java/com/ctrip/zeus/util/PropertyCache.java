package com.ctrip.zeus.util;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.tag.PropertyService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

;

/**
 * Created by fanqq on 2016/7/14.
 */
@Service("propertyCache")
public class PropertyCache {
    @Resource
    private PropertyService propertyService;

    private Map<String, LoadingCache<String, Map<String, String>>> cacheMap = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    protected void init() {
        LoadingCache<String, Map<String, String>> groupCache = CacheBuilder.newBuilder().maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Map<String, String>>() {
                           @Override
                           public Map<String, String> load(String id) throws Exception {
                               Long groupId;
                               Map<String, String> tmpMap = new HashMap<>();
                               try {
                                   groupId = Long.parseLong(id);
                               } catch (Exception e) {
                                   return tmpMap;
                               }
                               try {
                                   List<Property> propertyList = propertyService.getProperties("group", groupId);

                                   if (propertyList != null) {
                                       for (Property property : propertyList) {
                                           tmpMap.put(property.getName(), property.getValue());
                                       }
                                   }
                                   return tmpMap;
                               } catch (Exception e) {
                                   logger.error("Get Property Failed.", e);
                                   return tmpMap;
                               }
                           }
                       }
                );
        cacheMap.put("group", groupCache);

        LoadingCache<String, Map<String, String>> vsCache = CacheBuilder.newBuilder().maximumSize(2000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Map<String, String>>() {
                           @Override
                           public Map<String, String> load(String id) throws Exception {
                               Long vsId;
                               Map<String, String> tmpMap = new HashMap<>();
                               try {
                                   vsId = Long.parseLong(id);
                               } catch (Exception e) {
                                   return tmpMap;
                               }

                               try {
                                   List<Property> propertyList = propertyService.getProperties("vs", vsId);

                                   if (propertyList != null) {
                                       for (Property property : propertyList) {
                                           tmpMap.put(property.getName(), property.getValue());
                                       }
                                   }
                                   return tmpMap;
                               } catch (Exception e) {
                                   logger.error("Get Property Failed.", e);
                                   return tmpMap;
                               }
                           }
                       }
                );
        cacheMap.put("vs", vsCache);

        LoadingCache<String, Map<String, String>> slbCache = CacheBuilder.newBuilder().maximumSize(100)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Map<String, String>>() {
                           @Override
                           public Map<String, String> load(String id) throws Exception {
                               Long slbId;
                               Map<String, String> tmpMap = new HashMap<>();
                               try {
                                   slbId = Long.parseLong(id);
                               } catch (Exception e) {
                                   return tmpMap;
                               }
                               try {
                                   List<Property> propertyList = propertyService.getProperties("slb", slbId);
                                   if (propertyList != null) {
                                       for (Property property : propertyList) {
                                           tmpMap.put(property.getName(), property.getValue());
                                       }
                                   }
                                   return tmpMap;
                               } catch (Exception e) {
                                   logger.error("Get Property Failed.", e);
                                   return tmpMap;
                               }
                           }
                       }
                );
        cacheMap.put("slb", slbCache);
    }

    public String getPropertyValue(String type, String id, String propertyName) {
        try {
            return cacheMap.get(type).get(id).get(propertyName);
        } catch (Exception e) {
            logger.error("[PropertyCache] get Property Error. type: " + type + ";itemId: " + id + ";propertyName:" + propertyName, e);
            return null;
        }
    }

    public String getPropertyValue(String type, Long id, String propertyName) {
        try {
            return cacheMap.get(type).get(String.valueOf(id)).get(propertyName);
        } catch (Exception e) {
            logger.error("[PropertyCache] get Property Error. type: " + type + ";itemId: " + id + ";propertyName:" + propertyName, e);
            return null;
        }
    }
}
