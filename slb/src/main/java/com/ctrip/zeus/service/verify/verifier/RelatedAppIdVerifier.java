package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.model.model.TrafficControl;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.GroupListView;
import com.ctrip.zeus.restful.message.view.TrafficPolicyListView;
import com.ctrip.zeus.service.verify.IllegalMarkTypes;
import com.ctrip.zeus.service.verify.VerifyContext;
import com.ctrip.zeus.service.verify.VerifyResult;
import com.ctrip.zeus.service.verify.VerifyTaggingResult;
import com.ctrip.zeus.tag.ItemTypes;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.jvnet.hk2.component.MultiMap;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

;

/**
 * @Discription
 **/
@Component("relatedAppIdVerifier")
public class RelatedAppIdVerifier extends AbstractIllegalDataVerifier {
    @Override
    public List<VerifyResult> verify() throws Exception {
        // return a single VerifyResult containing all groups' id whose relatedappid property needs to be removed
        List<VerifyResult> results = new ArrayList<>(1);
        VerifyContext context = getContext();
        if (context != null) {
            GroupListView groupListView = context.getGroups();
            List<ExtendedView.ExtendedGroup> extendedGroups = groupListView.getGroups();
            if (extendedGroups != null) {
                List<Long> filteredGroupIds = extendedGroups.stream()
                        .filter(this::predicate)
                        .map(ExtendedView.ExtendedGroup::getId)
                        .collect(Collectors.toList());
                results.add(new VerifyTaggingResult(ItemTypes.GROUP, filteredGroupIds, getMarkName()));
            }
        }
        return results;
    }

    private boolean predicate(ExtendedView.ExtendedGroup group) {
        // return True if group's relatedappid property needs to be removed
        if (group == null) {
            return false;
        }
        Property targetProperty = null;// relatedappid property instance
        List<Property> properties = group.getProperties();
        if (properties == null) {
            return false;
        }
        for (Property property : group.getProperties()) {
            if ("relatedappid".equalsIgnoreCase(property.getName())) {
                targetProperty = property;
                break;
            }
        }
        if (targetProperty == null) {
            return false;
        }

        List<ExtendedView.ExtendedGroup> relatedGroups = getGroupsByAppId(targetProperty.getValue());
        if (relatedGroups.size() == 0) {
            return true;
        } else if (relatedGroups.size() == 1 && group.getId().equals(relatedGroups.get(0).getId())) {
            return true;
        } else {
            Long groupId = group.getId();
            for (ExtendedView.ExtendedGroup relatedGroup : relatedGroups) {
                if (groupId.equals(relatedGroup.getId())) {
                    continue;
                }
                if (shareOnePolicy(group, relatedGroup) || shareVsPathInfo(group, relatedGroup)) {
                    return false;
                }
            }
        }

        return true;
    }

    private List<ExtendedView.ExtendedGroup> getGroupsByAppId(String appId) {
        List<ExtendedView.ExtendedGroup> results = new ArrayList<>();

        GroupListView groupListView = getContext().getGroups();
        if (groupListView != null) {
            MultiMap<String, ExtendedView.ExtendedGroup> map = new MultiMap<>();
            for (ExtendedView.ExtendedGroup group : groupListView.getGroups()) {
                String tempAppId = group.getAppId();
                if (!Strings.isNullOrEmpty(tempAppId)) {
                    map.add(tempAppId, group);
                }
            }
            if (map.containsKey(appId)) {
                return map.get(appId);
            }
        }

        return results;
    }

    private boolean shareOnePolicy(ExtendedView.ExtendedGroup group1, ExtendedView.ExtendedGroup group2) {
        // build {groupId : policyId} map
        Map<Long, Long> groupIdPolicyIdMap = buildGroupPolicyMap();

        Long policyId1 = groupIdPolicyIdMap.get(group1.getId());
        Long policyId2 = groupIdPolicyIdMap.get(group2.getId());

        return policyId1 != null && Objects.equal(policyId1, policyId2);
    }

    private Map<Long, Long> buildGroupPolicyMap() {
        Map<Long, Long> result = new HashMap<>();// {groupId : policyId}
        TrafficPolicyListView listView = getContext().getPolicies();
        for (ExtendedView.ExtendedTrafficPolicy policy : listView.getTrafficPolicies()) {
            if (policy != null) {
                for (TrafficControl control : policy.getControls()) {
                    if (control != null) {
                        result.put(control.getGroup().getId(), policy.getId());
                    }
                }
            }
        }

        return result;
    }

    private boolean shareVsPathInfo(ExtendedView.ExtendedGroup group1, ExtendedView.ExtendedGroup group2) {
        Map<Long, String> vsIdPathMap1 = new HashMap<>();
        Map<Long, String> vsIdPathMap2 = new HashMap<>();

        for (GroupVirtualServer gvs : group1.getInstance().getGroupVirtualServers()) {
            vsIdPathMap1.put(gvs.getVirtualServer().getId(), gvs.getPath());
        }
        for (GroupVirtualServer gvs : group2.getInstance().getGroupVirtualServers()) {
            vsIdPathMap2.put(gvs.getVirtualServer().getId(), gvs.getPath());
        }

        for (Long vsId : vsIdPathMap1.keySet()) {
            if (vsIdPathMap2.containsKey(vsId) && vsIdPathMap2.get(vsId).equals(vsIdPathMap1.get(vsId))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMarkName() {
        return "ILLEGAL_RELATED_APP_ID_GROUP";
    }

    @Override
    public String getTargetItemType() {
        return ItemTypes.GROUP;
    }

    @Override
    public String getMarkType() {
        return IllegalMarkTypes.TAG;
    }

    @Override
    public String getDisplayName() {
        return "dirty-relatedappid-property";
    }
}
