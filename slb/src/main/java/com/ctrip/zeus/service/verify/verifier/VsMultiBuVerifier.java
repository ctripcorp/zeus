package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.verify.IllegalMarkTypes;
import com.ctrip.zeus.service.verify.VerifyContext;
import com.ctrip.zeus.service.verify.VerifyResult;
import com.ctrip.zeus.service.verify.VerifyTaggingResult;
import com.ctrip.zeus.tag.ItemTypes;
import org.springframework.stereotype.Component;

import java.util.*;

;

/**
 * @Discription
 **/
@Component("vsMultiBuVerifier")
public class VsMultiBuVerifier extends AbstractIllegalDataVerifier {

    @Override
    public List<VerifyResult> verify() throws Exception {
        List<VerifyResult> results = new ArrayList<>();
        if (getContext() != null) {
            VerifyContext context = getContext();
            List<ExtendedView.ExtendedGroup> extendedGroups = context.getGroups().getGroups();
            Map<Long, List<ExtendedView.ExtendedGroup>> vsIdGroupsMap = groupBy(extendedGroups, this::getVsIds);
            List<Long> vsIds = new ArrayList<>();
            for (Map.Entry<Long, List<ExtendedView.ExtendedGroup>> entry : vsIdGroupsMap.entrySet()) {
                if (hasMultiBu(entry)) {
                    vsIds.add(entry.getKey());
                }
            }
            results.add(new VerifyTaggingResult(ItemTypes.VS, vsIds, getMarkName()));
        }
        return results;
    }

    private boolean hasMultiBu(Map.Entry<Long, List<ExtendedView.ExtendedGroup>> entry) {
        if (entry != null) {
            Set<String> bus = new HashSet<>();
            for (ExtendedView.ExtendedGroup extendedGroup : entry.getValue()) {
                for (Property property : extendedGroup.getProperties()) {
                    if ("sbu".equalsIgnoreCase(property.getName())) {
                        bus.add(property.getValue());
                        break;
                    }
                }
            }
            return bus.size() > 1;
        }
        return false;
    }

    private List<Long> getVsIds(ExtendedView.ExtendedGroup extendedGroup) {
        if (extendedGroup != null) {
            List<Long> vsIds = new ArrayList<>();
            for (GroupVirtualServer gvs : extendedGroup.getInstance().getGroupVirtualServers()) {
                if (gvs != null && gvs.getVirtualServer() != null) {
                    vsIds.add(gvs.getVirtualServer().getId());
                }
            }
            return vsIds;
        }
        return new ArrayList<>();
    }

    @Override
    public String getMarkName() {
        return "ILLEGAL_VS_MULTIPLE_BU";
    }

    @Override
    public String getTargetItemType() {
        return ItemTypes.VS;
    }

    @Override
    public String getMarkType() {
        return IllegalMarkTypes.TAG;
    }

    @Override
    public String getDisplayName() {
        return "illegal-vs-multiple-bu";
    }
}
