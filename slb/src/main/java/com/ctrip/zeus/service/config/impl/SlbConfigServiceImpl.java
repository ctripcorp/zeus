package com.ctrip.zeus.service.config.impl;

import com.ctrip.zeus.dao.entity.SlbConfig;
import com.ctrip.zeus.dao.entity.SlbConfigExample;
import com.ctrip.zeus.dao.mapper.SlbConfigMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.config.SlbConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Discription
 **/
@Service("slbConfigService")
public class SlbConfigServiceImpl implements SlbConfigService {

    @Resource
    private SlbConfigMapper slbConfigMapper;

    private static final String SYSTEM_CONFIG_PREFIX = "System.";

    @Override
    public int batchUpdate(Map<String, String> configs) throws ValidationException {
        return batchUpdate(configs, false);
    }

    @Override
    public int batchUpdate(Map<String, String> configs, boolean systemConfig) throws ValidationException {
        if (!CollectionUtils.isEmpty(configs)) {
            List<SlbConfig> updateRecords = new ArrayList<>();

            for (Map.Entry<String, String> entry : configs.entrySet()) {
                if (systemConfig) {
                    updateRecords.add(SlbConfig.builder().propertyKey(SYSTEM_CONFIG_PREFIX + entry.getKey()).propertyValue(entry.getValue()).build());
                } else {
                    if (entry.getKey().startsWith(SYSTEM_CONFIG_PREFIX)){
                        throw new ValidationException("Property Key Should Not Start With System");
                    }
                    updateRecords.add(SlbConfig.builder().propertyKey(entry.getKey()).propertyValue(entry.getValue()).build());
                }
            }

            return slbConfigMapper.batchUpdate(updateRecords);
        }
        return 0;
    }

    @Override
    public List<SlbConfig> all(boolean systemConfig) {
        List<SlbConfig> tmp = slbConfigMapper.selectByExample(new SlbConfigExample());
        List<SlbConfig> result = new ArrayList<>();
        for (SlbConfig slbConfig : tmp) {
            if (systemConfig && slbConfig.getPropertyKey().startsWith(SYSTEM_CONFIG_PREFIX)) {
                slbConfig.setPropertyKey(slbConfig.getPropertyKey().replace(SYSTEM_CONFIG_PREFIX,""));
                result.add(slbConfig);
            } else if (!systemConfig && !slbConfig.getPropertyKey().startsWith(SYSTEM_CONFIG_PREFIX)) {
                result.add(slbConfig);
            }
        }
        return result;
    }

    @Override
    public int batchDelete(List<String> keys) throws ValidationException {
        return batchDelete(keys, false);
    }

    @Override
    public int batchDelete(List<String> keys, boolean systemConfig) throws ValidationException {
        List<String> toDelete = new ArrayList<>();
        if (systemConfig) {
            keys.forEach(k -> toDelete.add(SYSTEM_CONFIG_PREFIX + k));
            if (toDelete.size() > 0) {
                return slbConfigMapper.deleteByExample(new SlbConfigExample().createCriteria().
                        andPropertyKeyIn(toDelete).example());
            }
        }

        if (keys != null && keys.size() > 0) {
            for (String key : keys){
                if (key.startsWith(SYSTEM_CONFIG_PREFIX)){
                    throw new ValidationException("Property Key Should Not Start With System");
                }
            }
            return slbConfigMapper.deleteByExample(new SlbConfigExample().createCriteria().
                    andPropertyKeyIn(keys).example());
        }

        return 0;
    }

    @Override
    public Map<String, String> query(List<String> keys) {
        return query(keys, false);
    }

    @Override
    public Map<String, String> query(List<String> keys, boolean systemConfig) {
        if (keys != null && keys.size() > 0) {
            if (systemConfig) {
                keys = decorateWithSystemPrefix(keys);
            }
            List<SlbConfig> records = slbConfigMapper.selectByExample(new SlbConfigExample().createCriteria().
                    andPropertyKeyIn(keys).example());
            if (records.size() > 0) {
                Map<String, String> result = new HashMap<>();
                for (SlbConfig record : records) {
                    String value = record.getPropertyValue();
                    if (value == null || value.isEmpty()) {
                        continue;
                    }
                    if (systemConfig) {
                        if (record.getPropertyKey().startsWith(SYSTEM_CONFIG_PREFIX)) {
                            result.put(record.getPropertyKey().replace(SYSTEM_CONFIG_PREFIX, ""), record.getPropertyValue());
                        }
                    } else {
                        if (!record.getPropertyKey().startsWith(SYSTEM_CONFIG_PREFIX)) {
                            result.put(record.getPropertyKey(), record.getPropertyValue());
                        }
                    }
                }
                return result;
            }
        }
        return new HashMap<>();
    }

    @Override
    public int batchInsert(Map<String, String> keyValueMap) {
        if (!CollectionUtils.isEmpty(keyValueMap)) {
            // Skip checking key's existence in db because exception will be thrown in this situation
            List<SlbConfig> records = new ArrayList<>(keyValueMap.size());
            for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
                records.add(SlbConfig.builder().propertyKey(entry.getKey()).propertyValue(entry.getValue()).build());
            }

            return slbConfigMapper.batchInsert(records);
        }
        return 0;
    }

    @Override
    public int batchInsert(Map<String, String> keyValueMap, boolean systemConfig) {
        if (!CollectionUtils.isEmpty(keyValueMap)) {
            // Skip checking key's existence in db because exception will be thrown in this situation
            List<SlbConfig> records = new ArrayList<>(keyValueMap.size());
            for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
                if (systemConfig) {
                    records.add(SlbConfig.builder().propertyKey(SYSTEM_CONFIG_PREFIX + entry.getKey()).propertyValue(entry.getValue()).build());
                } else {
                    records.add(SlbConfig.builder().propertyKey(entry.getKey()).propertyValue(entry.getValue()).build());
                }
            }

            return slbConfigMapper.batchInsert(records);
        }
        return 0;
    }

    @Override
    public int batchUpsertValue(Map<String, String> kvMap, Boolean system) {
        if (CollectionUtils.isEmpty(kvMap)) {
            return 0;
        }

        List<SlbConfig> configRecords = SlbConfigsConvertor.convertFromMap(kvMap, system);
        return slbConfigMapper.batchUpsertValue(configRecords);
    }

    private List<String> decorateWithSystemPrefix(List<String> originalKeys) {
        if (CollectionUtils.isEmpty(originalKeys)) {
            return new ArrayList<>();
        }

        return originalKeys.stream().map(key -> SYSTEM_CONFIG_PREFIX + key).collect(Collectors.toList());
    }

    public static class SlbConfigsConvertor {
        public static Map<String, String> convertToMap(List<SlbConfig> configs) {
            Map<String, String> map = new HashMap<>();
            if (configs != null) {
                for (SlbConfig record : configs) {
                    map.put(record.getPropertyKey(), record.getPropertyValue());
                }
            }

            return map;
        }

        public static List<SlbConfig> convertFromMap(Map<String, String> map, Boolean system) {
            List<SlbConfig> records = new ArrayList<>();
            if (map != null) {
                for (Map.Entry<String, String> kvEntry : map.entrySet()) {
                    if (system) {
                        records.add(SlbConfig.builder().propertyKey(SYSTEM_CONFIG_PREFIX + kvEntry.getKey()).propertyValue(kvEntry.getValue()).build());
                    } else {
                        records.add(SlbConfig.builder().propertyKey(kvEntry.getKey()).propertyValue(kvEntry.getValue()).build());
                    }
                }
            }
            return records;
        }
    }

}
