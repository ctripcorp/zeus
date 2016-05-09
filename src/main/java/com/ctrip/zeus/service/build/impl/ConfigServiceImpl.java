package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.service.build.ConfigService;
import com.ctrip.zeus.util.S;
import com.netflix.config.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lu.wang on 2016/4/19.
 */
@Service("configService")
public class ConfigServiceImpl implements ConfigService {

    private DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();

    @Override
    public String getStringValue(String key, Long slbId, Long vsId, Long groupId, String defaultValue) throws Exception {

        String fullKey = getFullKey(key);

        DynamicStringProperty stringValue =
                factory.getStringProperty(fullKey + ".ip." + S.getIp(), null);
        if (stringValue.get() == null && groupId != null) {

            stringValue = factory.getStringProperty(fullKey + ".group." + groupId, null);
        }
        if (stringValue.get() == null && vsId != null) {
            stringValue = factory.getStringProperty(fullKey + ".vs." + vsId, null);
        }
        if (stringValue.get() == null && slbId != null) {
            stringValue = factory.getStringProperty(fullKey + ".slb." + slbId, null);
        }
        if (stringValue.get() == null) {
            stringValue = factory.getStringProperty(fullKey + ".default", defaultValue);
        }

        return stringValue.get();
    }

    @Override
    public int getIntValue(String key, Long slbId, Long vsId, Long groupId, int defaultValue) throws Exception {
        String fullKey = getFullKey(key);

        DynamicIntProperty intValue =
                factory.getIntProperty(fullKey + ".ip." + S.getIp(), -1);
        if (intValue.get() == -1 && groupId != null) {
            intValue = factory.getIntProperty(fullKey + ".group." + groupId, -1);
        }
        if (intValue.get() == -1 && vsId != null) {
            intValue = factory.getIntProperty(fullKey + ".vs." + vsId, -1);
        }
        if (intValue.get() == -1 && slbId != null) {
            intValue = factory.getIntProperty(fullKey + ".slb." + slbId, -1);
        }
        if (intValue.get() == -1) {
            intValue = factory.getIntProperty(fullKey + ".default", defaultValue);
        }

        return intValue.get();
    }

    @Override
    public boolean getEnable(String key, Long slbId, Long vsId, Long groupId, boolean defaultValue) throws Exception {
        String enableFullKey = getEnableFullKey(key);

        DynamicBooleanProperty value =
                factory.getBooleanProperty(enableFullKey + ".ip." + S.getIp(), false);
        if (!value.get() && groupId != null) {
            value = factory.getBooleanProperty(enableFullKey + ".group." + groupId, false);
        }
        if (!value.get() && vsId != null) {
            value = factory.getBooleanProperty(enableFullKey + ".vs." + vsId, false);
        }
        if (!value.get() && slbId != null) {
            value = factory.getBooleanProperty(enableFullKey + ".slb." + slbId, false);
        }
        if (!value.get()) {
            value = factory.getBooleanProperty(enableFullKey + ".default", defaultValue);
        }

        return value.get();
    }

    @Override
    public String getStringValue(String key, String defaultValue) {
        DynamicStringProperty stringValue =
                factory.getStringProperty(key, defaultValue);
        return stringValue.get();
    }

    @Override
    public boolean getEnable(String key, boolean defaultValue){
        DynamicBooleanProperty value =
                factory.getBooleanProperty(key + ".enable", defaultValue);
        return value.get();
    }

    @Override
    public String getKeyType(String prefix, Set<String> types, String value) {
        if (prefix == null || types == null || types.size() == 0 || value == null)
            return null;

        String typeValue;
        for (String typeName : types) {
            typeValue = factory.getStringProperty(prefix + "." + typeName, null).get();
            if (typeValue != null && Arrays.asList(typeValue.split(",")).contains(value)) {
                return typeName;
            }
        }

        String defaultValue = factory.getStringProperty(prefix + ".default", null).get();
        Map<String, String> valueKeyMap = parseIpUserStr(defaultValue);

        for (Map.Entry entry : valueKeyMap.entrySet()) {
            if (entry.getKey().equals(value))
                return entry.getValue().toString();
        }
        
        return null;
    }

    private Map<String,String> parseIpUserStr(String ipConfig){
        Map<String, String> result = new HashMap<>();
        if (ipConfig == null || ipConfig.isEmpty()) {
            return result;
        }
        String[] configs = ipConfig.split("#");
        for(String config : configs) {
            String[] parts = config.split("=", -1);
            if (parts == null || parts.length != 2){
                continue;
            }
            String[] ips = parts[0].split(",");
            String userName = parts[1];
            for (String ip : ips) {
                result.put(ip,userName);
            }
        }
        return result;
    }

    private String getFullKey(String key) {
        return "nginx." + key;
    }

    private String getEnableFullKey(String key) {
        return "nginx." + key + ".enable";
    }

}
