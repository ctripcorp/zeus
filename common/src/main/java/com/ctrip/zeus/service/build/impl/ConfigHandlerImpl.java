package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.util.LocalInfoPack;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lu.wang on 2016/4/19.
 *
 *
 * !!VERY IMPORTANT!!
 * If you are trying to replace DynamicPropertyFactory with ConfigurationManager, please read the following content carefully.
 *
 * Difference between <code>ConfigurationManager.getInstance().getString()</code> and <code>DynamicPropertyFactory.getInstance().getStringProperty().get()</code>:
 * Their behaviors only vary when the raw value contains any value separator character ",".
 * - <code>ConfigurationManager.getInstance().getString()</code>:
 *      it will interpolate the raw value to a string array and only return the first element.
 * - <code>DynamicPropertyFactory.getInstance().getStringProperty().get()</code>:
 *      it will return the raw value without interpolation.
 *
 * This difference may cause some severe logical issues
 * when you are doing a manual interpolation after getting the value
 * or the raw value is in a designed format containing ",", not a simple array.
 *
 * So before you think this through and figure out how to deal with it, DO NOT change DynamicPropertyFactory to ConfigurationManager.
 *
 * NOTE: This may only apply to archaius 0.4.1. If we upgrade it to a later version, the behavior needs to be tested.
 *
 * - dongyq
 */
@Service("configHandler")
public class ConfigHandlerImpl implements ConfigHandler {

    private DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getStringValue(String key, Long slbId, Long vsId, Long groupId, String defaultValue) throws Exception {

        String fullKey = getFullKey(key);

        DynamicStringProperty stringValue =
                factory.getStringProperty(fullKey + ".ip." + LocalInfoPack.INSTANCE.getIp(), null);
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
                factory.getIntProperty(fullKey + ".ip." + LocalInfoPack.INSTANCE.getIp(), -1);
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
    public int getIntValue(String key, int defaultValue) throws Exception {
        DynamicIntProperty stringValue =
                factory.getIntProperty(key, defaultValue);
        return stringValue.get();
    }

    @Override
    public boolean getEnable(String key, Long slbId, Long vsId, Long groupId, boolean defaultValue) throws Exception {
        String enableFullKey = getEnableFullKey(key);
        String defaultEnable = String.valueOf(defaultValue);

        DynamicStringProperty stringValue =
                factory.getStringProperty(enableFullKey + ".ip." + LocalInfoPack.INSTANCE.getIp(), null);
        if (stringValue.get() == null && groupId != null) {
            stringValue = factory.getStringProperty(enableFullKey + ".group." + groupId, null);
        }
        if (stringValue.get() == null && vsId != null) {
            stringValue = factory.getStringProperty(enableFullKey + ".vs." + vsId, null);
        }
        if (stringValue.get() == null && slbId != null) {
            stringValue = factory.getStringProperty(enableFullKey + ".slb." + slbId, null);
        }
        if (stringValue.get() == null) {
            stringValue = factory.getStringProperty(enableFullKey + ".default", defaultEnable);
        }

        return stringValue.get().equalsIgnoreCase("true") ? true : false;
    }

    @Override
    public String getStringValue(String key, String defaultValue) {
        DynamicStringProperty stringValue =
                factory.getStringProperty(key, defaultValue);
        return stringValue.get();
    }

    @Override
    public boolean getEnable(String key, boolean defaultValue) {
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

    private Map<String, String> parseIpUserStr(String ipConfig) {
        Map<String, String> result = new HashMap<>();
        if (ipConfig == null || ipConfig.isEmpty()) {
            return result;
        }
        String[] configs = ipConfig.split("#");
        for (String config : configs) {
            String[] parts = config.split("=", -1);
            if (parts == null || parts.length != 2) {
                continue;
            }
            String[] ips = parts[0].split(",");
            String userName = parts[1];
            for (String ip : ips) {
                result.put(ip, userName);
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
