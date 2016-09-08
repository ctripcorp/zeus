package com.ctrip.zeus.service.message.queue;

/**
 * Created by fanqq on 2016/9/6.
 */
public enum MessageType {
    NewGroup,
    UpdateGroup,
    DeleteGroup,
    ActivateGroup,
    DeactivateGroup,
    NewVs,
    UpdateVs,
    DeleteVs,
    ActivateVs,
    DeactivateVs,
    NewSlb,
    UpdateSlb,
    DeleteSlb,
    ActivateSlb,
    DeactivateSlb,
    OpsPull,
    OpsMember,
    OpsServer,
    OpsHealthy;

    MessageType getByName(String name) {
        return valueOf(name);
    }
}
