package com.ctrip.zeus.service.model;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.model.common.ValidationContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

;

/**
 * Created by zhoumy on 2017/2/4.
 */
public interface ValidationFacade {

    void validateGroup(Group group, ValidationContext context);

    void validateGroup(Group group, ValidationContext context, boolean checkPathOverlap);

    Map<Long, ValidationContext> validateGroupPathOverlap(Group group, Long[] vsIds) throws ValidationException;

    void validatePolicy(TrafficPolicy policy, ValidationContext context);

    void validateDr(Dr dr, ValidationContext context);

    void validateRule(Rule rule, ValidationContext context);

    void validateVs(VirtualServer vs, ValidationContext context);

    void validateSlb(Slb slb, ValidationContext context);

    void validateSlbNodes(Collection<Slb> slbs, ValidationContext context);

    void validateVsesOnSlb(Long slbId, Collection<VirtualServer> vses, ValidationContext context);

    void validateEntriesOnVs(Long vsId, List<Group> groups, List<TrafficPolicy> policies, ValidationContext context);

    void validateSyngeneticVs(Set<Long> vsIds, Collection<Group> groups, Collection<TrafficPolicy> policies, Collection<Dr> drs, ValidationContext context);

    void validateForSplitVs(Long vs, Long id) throws Exception;

    void validateForMergeVs(List<Long> vses, Long id) throws Exception;

    /**
     * Validate a group against its related app's groups.
     *
     * @param group        the group to be validated
     * @param relatedAppId the app which the group is related to. If null, the value will be read from "relatedAppId" property of the group
     * @param relationType the type of the relation. If null, the value will be read from "relationType" property of the group
     * @param targetVsId   the VS which the validation shall be applied to. If null, all the VSes the group binds to will be used as a default.
     * @param targetGroups the groups to be validated against. If null, all the groups bound to the target VSes will be used as a default.
     * @return true if validation finishes successfully and the group data passes the validation, or no related app is found. Otherwise, return false.
     */
    boolean validateRelatedGroup(Group group, String relatedAppId, String relationType, Long targetVsId, List<Group> targetGroups);

    @Deprecated
    boolean validateRelatedGroup(Long vsId, List<Group> vsGroups, Group group, String relatedAppId);

    @Deprecated
    boolean validateRelatedGroup(Group group, String relatedAppId);

    void validateSkipErrorsOfWhiteList(String type, ValidationContext context);

    void validateSkipErrorsOfRelatedGroup(Group group, ValidationContext context);

    Long validateDrDestination(Group sourceGroup, Group desGroup, VirtualServer sourceVs, Map<Long, VirtualServer> vsLookup, Map<Long, Property> slbIdcInfo, Map<Long, Property> groupIdcInfo) throws Exception;
}