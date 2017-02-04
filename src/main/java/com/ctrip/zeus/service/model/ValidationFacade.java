package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.common.ValidationContext;

import java.util.Set;

/**
 * Created by zhoumy on 2017/2/4.
 */
public interface ValidationFacade {

    void validateGroup(Group group, ValidationContext context);

    void validatePolicy(TrafficPolicy policy, ValidationContext context);

    void validateVs(VirtualServer vs, ValidationContext context);

    void validateSlb(Slb slb, ValidationContext context);

    void validateEntriesOnVs(Long vsId, Set<Group> groups, Set<TrafficPolicy> policies, ValidationContext context);
}