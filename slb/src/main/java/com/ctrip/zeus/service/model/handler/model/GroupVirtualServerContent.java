package com.ctrip.zeus.service.model.handler.model;

import com.ctrip.zeus.model.model.GroupVirtualServer;

public class GroupVirtualServerContent extends GroupVirtualServer {

    private Long groupId;
    private Integer groupVersion;

    public Long getGroupId() {
        return groupId;
    }

    public GroupVirtualServerContent setGroupId(Long groupId) {
        this.groupId = groupId;
        return this;
    }

    public Integer getGroupVersion() {
        return groupVersion;
    }

    public GroupVirtualServerContent setGroupVersion(Integer groupVersion) {
        this.groupVersion = groupVersion;
        return this;
    }
}
