package com.ctrip.zeus.service.config;


import com.ctrip.zeus.dao.entity.SlbConfig;
import com.ctrip.zeus.exceptions.ValidationException;

import java.util.List;
import java.util.Map;

/**
 * @Discription
 **/
public interface SlbConfigService {

    int batchUpdate(Map<String, String> configMap) throws ValidationException;

    int batchUpdate(Map<String, String> configMap, boolean systemConfig) throws ValidationException;

    int batchInsert(Map<String, String> keyValueMap);

    int batchInsert(Map<String, String> keyValueMap, boolean systemConfig);

    List<SlbConfig> all(boolean systemConfig);

    int batchDelete(List<String> keys) throws ValidationException;

    int batchDelete(List<String> keys, boolean systemConfig) throws ValidationException;

    Map<String, String> query(List<String> keys);

    Map<String, String> query(List<String> keys, boolean systemConfig);

    int batchUpsertValue(Map<String, String> kvMap, Boolean system);
}
