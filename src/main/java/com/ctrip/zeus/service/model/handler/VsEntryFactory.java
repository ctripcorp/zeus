package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.ValidationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2017/2/4.
 */
public interface VsEntryFactory {

    List<LocationEntry> getGroupRelatedPolicyEntries(Long groupId) throws Exception;

    Map<Long, List<LocationEntry>> getGroupRelatedPolicyEntriesByVs(Long[] groupIds) throws Exception;

    Map<Long, List<LocationEntry>> getGroupEntriesByVs(Long[] groupIds) throws Exception;

    Map<Long, LocationEntry> mapPolicyEntriesByGroup(Long vsId, List<TrafficPolicy> policies, ValidationContext context);

    List<LocationEntry> filterGroupEntriesByVs(Long vsId, List<Group> groups, ValidationContext context);

    Map<Long, List<LocationEntry>> buildLocationEntriesByVs(Long[] vsIds, Long[] escapedGroups, Long[] escapedPolicies) throws ValidationException;
}
