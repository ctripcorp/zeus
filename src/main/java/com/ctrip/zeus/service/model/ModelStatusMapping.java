package com.ctrip.zeus.service.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhoumy on 2016/1/19.
 */
public class ModelStatusMapping<T> {
    private Map<Long, T> onlineMapping;
    private Map<Long, T> offlineMapping;

    public ModelStatusMapping() {
        onlineMapping = new HashMap<>();
        offlineMapping = new HashMap<>();
    }

    public void addOnline(Long key, T value) {
        onlineMapping.put(key, value);
    }

    public void addOffline(Long key, T value) {
        offlineMapping.put(key, value);
    }

    public Map<Long, T> getOnlineMapping() {
        return onlineMapping;
    }

    public Map<Long, T> getOfflineMapping() {
        return offlineMapping;
    }
}
