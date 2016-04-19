package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

/**
 * Created by lu.wang on 2016/4/19.
 */
public class ConfServiceImpl implements ConfService {

    private static DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();

    @Override
    public String getStringValue(String key, Long slbId, Long vsId, Long groupId, String defaultValue) throws Exception {

        String fullKey = getFullKey(key);

        DynamicStringProperty value =
                factory.getStringProperty(fullKey + ".ip." + S.getIp(), null);
        if (value.get() == null && groupId != null) {
            value = factory.getStringProperty(fullKey + ".group." + groupId, null);
        }
        if (value.get() == null && vsId != null) {
            value = factory.getStringProperty(fullKey + ".vs." + vsId, null);
        }
        if (value.get() == null && slbId != null) {
            value = factory.getStringProperty(fullKey + ".slb." + slbId, null);
        }
        if (value.get() == null) {
            value = factory.getStringProperty(fullKey + ".default", defaultValue);
        }

        return value.get();
    }

    @Override
    public int getIntValue(String key, Long slbId, Long vsId, Long groupId, int defaultValue) throws Exception {
        String fullKey = getFullKey(key);

        DynamicIntProperty value =
                factory.getIntProperty(fullKey + ".ip." + S.getIp(), -1);
        if (value.get() == -1 && groupId != null) {
            value = factory.getIntProperty(fullKey + ".group." + groupId, -1);
        }
        if (value.get() == -1 && vsId != null) {
            value = factory.getIntProperty(fullKey + ".vs." + vsId, -1);
        }
        if (value.get() == -1 && slbId != null) {
            value = factory.getIntProperty(fullKey + ".slb." + slbId, -1);
        }
        if (value.get() == -1) {
            value = factory.getIntProperty(fullKey + ".default", defaultValue);
        }

        return value.get();
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

    private String getFullKey(String key) {
        return "nginx." + key;
    }

    private String getEnableFullKey(String key) {
        return "nginx." + key + "enable";
    }

}
