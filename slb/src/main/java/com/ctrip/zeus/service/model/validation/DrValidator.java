package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Dr;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.model.common.ValidationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

;

public interface DrValidator extends ModelValidator<Dr> {
    void validateFields(Dr dr, ValidationContext context) throws Exception;

    void validateLoop(List<Node> nodes) throws Exception;

    void checkDrProperties(Set<Long> sourceGroupIds) throws Exception;

    void checkGroupRelations(Dr dr, Set<Long> sourceGroupIds, Set<Long> desGroupIds) throws Exception;

    void checkGroupAvailability(Set<Long> groupIds) throws Exception;

    List<Node> checkGroupsAndVses(Dr dr, Map<Long, Group> drRelatedGroups, Map<Long, VirtualServer> vsLookup, Map<Long, Property> slbIdcInfo, Map<Long, Property> groupIdcInfo) throws Exception;

    VirtualServer checkAndGetDesVs(Group sourceGroup, Group desGroup, VirtualServer sourceVs, Map<Long, VirtualServer> vsLookup) throws Exception;

    Long checkAndGetDesSlbId(Long desGroupId, VirtualServer desVs, Map<Long, Property> slbIdcInfo, Map<Long, Property> groupIdcInfo) throws Exception;

    class Node {
        String source;
        String des;

        Node(String source, String des) {
            this.source = source;
            this.des = des;
        }
    }
}
