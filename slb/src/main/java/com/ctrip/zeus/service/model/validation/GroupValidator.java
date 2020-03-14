package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupServer;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.ValidationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface GroupValidator extends ModelValidator<Group> {

    void validateFields(Group group, ValidationContext context);

    void validateCanaryVersion(Group group) throws ValidationException;

    Map<Long, GroupVirtualServer> validateGroupOnVses(List<GroupVirtualServer> groupOnVses, boolean virtual) throws ValidationException;

    void validateMembers(List<GroupServer> servers) throws ValidationException;

    void validatePolicyRestriction(Map<Long, GroupVirtualServer> groupOnVses, List<LocationEntry> policyEntries) throws ValidationException;
}
