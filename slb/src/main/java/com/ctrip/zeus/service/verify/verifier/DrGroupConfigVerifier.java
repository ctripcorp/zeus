package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.verify.*;
import com.ctrip.zeus.tag.ItemTypes;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import org.springframework.stereotype.Component;

import java.util.*;

;

/**
 * @Discription Groups with same appId in different IDCs should have same configuration, including:
 * 1. domains
 * 2. ssl
 * 3. path
 * 4. rewrite
 **/
@Component("drGroupConfigVerifier")
public class DrGroupConfigVerifier extends AbstractIllegalDataVerifier {

    private final String IDC_PROPERTY_KEY = "idc";

    @Override
    public String getMarkName() {
        return "ILLEGAL_DR_GROUP_CONFIG";
    }

    @Override
    public List<VerifyResult> verify() throws Exception {
        if (getContext() != null) {
            List<VerifyResult> results = new ArrayList<>();
            VerifyContext context = getContext();
            Map<Long, List<ExtendedView.ExtendedGroup>> appIdGroupMap =
                    groupBy(context.getGroups().getGroups(), this::getGroupAppId);
            for (List<ExtendedView.ExtendedGroup> extendedGroups : appIdGroupMap.values()) {
                // skip verification if only one group attached to the appId
                if (extendedGroups.size() < 2) {
                    continue;
                }

                ExtendedView.ExtendedGroup[] groups = extendedGroups.toArray(new ExtendedView.ExtendedGroup[0]);
                for (int i = 0; i != groups.length; i++) {
                    for (int j = i + 1; j != groups.length; j++) {
                        ExtendedView.ExtendedGroup group = groups[i];
                        ExtendedView.ExtendedGroup group1 = groups[j];
                        if (group.getId().equals(group1.getId())) {
                            continue;
                        }
                        String idc1 = getIdc(group);
                        String idc2 = getIdc(group1);
                        if (!Objects.equal(idc1, idc2) && !isDrGroupsConfigSame(group, group1)) {
                            results.add(new VerifyPropertyResult(
                                    getTargetItemType(),
                                    Collections.singletonList(group.getId()),
                                    getMarkName(),
                                    PropertyValueUtils.write(Collections.singletonList(new IdItemType(group1.getId(), ItemTypes.GROUP)))));
                        }
                    }
                }
            }
            return results;
        }
        return new ArrayList<>();
    }

    private List<Long> getGroupAppId(ExtendedView.ExtendedGroup group) {
        if (group != null && group.getInstance() != null) {
            return Arrays.asList(Long.parseLong(group.getInstance().getAppId()));
        }
        return null;
    }

    private String getIdc(ExtendedView.ExtendedGroup group) {
        if (group != null) {
            for (Property property : group.getProperties()) {
                if (IDC_PROPERTY_KEY.equalsIgnoreCase(property.getName()) && !property.getValue().isEmpty()) {
                    return property.getValue();
                }
            }
        }
        return null;
    }

    private boolean isDrGroupsConfigSame(ExtendedView.ExtendedGroup group1, ExtendedView.ExtendedGroup group2) {
        if (group1 != null && group2 != null) {
            List<GroupVirtualServer> gvses1 = group1.getInstance().getGroupVirtualServers();
            List<GroupVirtualServer> gvses2 = group2.getInstance().getGroupVirtualServers();

            // group GroupVirtualServer by domains + ssl
            Map<String, List<GroupVirtualServer>> map1 = groupBy(gvses1, this::getGvsKey);
            Map<String, List<GroupVirtualServer>> map2 = groupBy(gvses2, this::getGvsKey);

            if (map1.keySet().equals(map2.keySet())) {
                for (String key : map1.keySet()) {
                    assert map1.get(key).size() == 1;
                    assert map2.get(key).size() == 1;

                    if (!compareGvs(map1.get(key).get(0), map2.get(key).get(0))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private List<String> getGvsKey(GroupVirtualServer object) {
        if (object != null) {
            List<Domain> domains = object.getVirtualServer().getDomains();
            domains.sort(Comparator.comparing(Domain::getName));

            String result = Joiner.on(",").join(domains) + "," + String.valueOf(object.getVirtualServer().getSsl());
            return Arrays.asList(result);
        }
        return new ArrayList<>();
    }

    private boolean compareGvs(GroupVirtualServer object1, GroupVirtualServer object2) {
        if (object1 != null && object2 != null) {
            return Objects.equal(object1.getPath(), object2.getPath()) && Objects.equal(object1.getRewrite(), object2.getRewrite());
        }
        return false;
    }

    @Override
    public String getTargetItemType() {
        return ItemTypes.GROUP;
    }

    @Override
    public String getMarkType() {
        return IllegalMarkTypes.PROPERTY;
    }

    @Override
    public String getDisplayName() {
        return "not-same-dr-group-config";
    }
}
