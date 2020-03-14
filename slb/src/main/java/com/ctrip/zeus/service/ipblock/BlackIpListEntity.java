package com.ctrip.zeus.service.ipblock;

import java.util.List;
import java.util.Map;

public class BlackIpListEntity {
    private List<String> global;
    private Map<String, List<String>> globalList;
    private Map<String, List<String>> appList;

    public List<String> getGlobal() {
        return global;
    }

    public BlackIpListEntity setGlobal(List<String> global) {
        this.global = global;
        return this;
    }

    public Map<String, List<String>> getAppList() {
        return appList;
    }

    public BlackIpListEntity setAppList(Map<String, List<String>> appList) {
        this.appList = appList;
        return this;
    }

    public Map<String, List<String>> getGlobalList() {
        return globalList;
    }

    public BlackIpListEntity setGlobalList(Map<String, List<String>> globalList) {
        this.globalList = globalList;
        return this;
    }
}
