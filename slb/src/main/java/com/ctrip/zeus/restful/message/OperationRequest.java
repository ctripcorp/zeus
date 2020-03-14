package com.ctrip.zeus.restful.message;

import com.ctrip.zeus.service.auth.ResourceOperationType;

import java.util.List;

/**
 * @Discription
 **/
public class OperationRequest {
    private Long groupId;
    private String groupName;
    private List<String> ips;
    private String type;
    private boolean up;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public ResourceOperationType getResourceOperationType() {
        if (type == null || type.isEmpty()) {
            return null;
        }

        try {
            return ResourceOperationType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
