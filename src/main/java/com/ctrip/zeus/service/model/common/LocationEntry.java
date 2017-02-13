package com.ctrip.zeus.service.model.common;

/**
 * Created by zhoumy on 2017/1/25.
 */
public class LocationEntry {
    Long vsId;
    Long entryId;
    MetaType entryType;
    String path;
    Integer priority;

    public Long getVsId() {
        return vsId;
    }

    public LocationEntry setVsId(Long vsId) {
        this.vsId = vsId;
        return this;
    }

    public Long getEntryId() {
        return entryId;
    }

    public LocationEntry setEntryId(Long entryId) {
        this.entryId = entryId;
        return this;
    }

    public MetaType getEntryType() {
        return entryType;
    }

    public LocationEntry setEntryType(MetaType entryType) {
        this.entryType = entryType;
        return this;
    }

    public String getPath() {
        return path;
    }

    public LocationEntry setPath(String path) {
        this.path = path;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public LocationEntry setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public String toString() {
        return entryType.toString() + "-" + entryId;
    }
}
