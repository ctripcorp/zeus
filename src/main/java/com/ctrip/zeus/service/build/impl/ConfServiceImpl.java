package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.util.S;
import com.netflix.config.*;
import org.springframework.stereotype.Service;

/**
 * Created by lu.wang on 2016/4/19.
 */
@Service("confService")
public class ConfServiceImpl implements ConfService {

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

    private String getFullKey(String key) {
        return "nginx." + key;
    }

    private String getEnableFullKey(String key) {
        return "nginx." + key + ".enable";
    }

}
