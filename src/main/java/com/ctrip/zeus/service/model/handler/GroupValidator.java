package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.TrafficPolicy;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface GroupValidator extends ModelValidator<Group> {

    void validate(Group target, boolean escapePathValidation) throws Exception;

    void validateForMerge(Long[] toBeMergedItems, Long vsId, Map<Long, Group> groupRef, Map<Long, TrafficPolicy> policyRef, boolean escapePathValidation) throws Exception;

    void validateGroupVirtualServers(Group target, boolean escapePathValidation) throws Exception;

    void validateGroupServers(List<GroupServer> groupServers) throws Exception;
}
