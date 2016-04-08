package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface GroupValidator extends ModelValidator<Group> {

    void validate(Group target, boolean escapePathValidation) throws Exception;

    void validateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers, boolean escapePathValidation) throws Exception;

    void validateGroupServers(List<GroupServer> groupServers) throws Exception;
}
