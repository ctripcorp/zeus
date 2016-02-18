package com.ctrip.zeus.service.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhoumy on 2016/1/19.
 */
public class ModelStatusMapping<T> {
    private final Map<String, Map> data;
    private Map<Long, T> online;
    private Map<Long, T> offline;

    public ModelStatusMapping() {
        data = new HashMap<>();
        online = new HashMap<>();
        offline = new HashMap<>();
        data.put("offline_first", offline);
        data.put("online", online);
    }

    public void addOnline(Long key, T value) {
        online.put(key, value);
    }

    public void addOffline(Long key, T value) {
        offline.put(key, value);
    }

    public Map<Long, T> getOnlineMapping() {
        return data.get("online");
    }

    public Map<Long, T> getOfflineMapping() {
        return data.get("offline_first");
    }
}
