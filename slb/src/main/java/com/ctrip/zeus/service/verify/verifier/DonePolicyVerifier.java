package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.TrafficControl;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.TrafficPolicyListView;
import com.ctrip.zeus.service.verify.IllegalMarkTypes;
import com.ctrip.zeus.service.verify.VerifyContext;
import com.ctrip.zeus.service.verify.VerifyResult;
import com.ctrip.zeus.service.verify.VerifyTaggingResult;
import com.ctrip.zeus.tag.ItemTypes;
import com.ctrip.zeus.tag.PropertyService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

;

/**
 * @Discription
 **/
@Component("donePolicyVerifier")
public class DonePolicyVerifier extends AbstractIllegalDataVerifier {

    @Resource
    private PropertyService propertyService;

    private Logger logger = LoggerFactory.getLogger(DonePolicyVerifier.class);

    @VisibleForTesting
    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public String getMarkName() {
        return "ILLEGAL_ALREADY_DOWN_POLICY";
    }

    @Override
    public List<VerifyResult> verify() throws Exception {
        if (getContext() != null) {
            VerifyContext context = getContext();

            TrafficPolicyListView policies = context.getPolicies();
            List<ExtendedView.ExtendedTrafficPolicy> donePolicies =
                    policies.getTrafficPolicies().stream().filter(this::isPolicyDone).collect(Collectors.toList());
            List<Long> donePolicyIds = Lists.transform(donePolicies, ExtendedView.ExtendedTrafficPolicy::getId);

            return Arrays.asList(new VerifyTaggingResult(ItemTypes.POLICY, donePolicyIds, getMarkName()));
        }

        return new ArrayList<>();
    }

    private boolean isPolicyDone(ExtendedView.ExtendedTrafficPolicy policy) {
        if (policy != null && policy.getInstance().getControls() != null) {
            Set<String> appIds = new HashSet<>();
            List<TrafficControl> controls = policy.getInstance().getControls();

            for (TrafficControl control : controls) {
                Group group = control.getGroup();
                if (group != null && group.getAppId() != null) {
                    appIds.add(control.getGroup().getAppId());
                }
            }
            for (TrafficControl control : controls) {
                if (control.getWeight() == 100) {
                    String relatedAppId = getRelatedAppId(control.getGroup());
                    if (relatedAppId != null && appIds.contains(relatedAppId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getRelatedAppId(Group group) {
        try {
            if (group != null && group.getId() != null) {
                Property property = propertyService.getProperty("relatedappid", group.getId(), "group");
                if (property != null) {
                    return property.getValue();
                }
            }
        } catch (Exception e) {
            logger.warn("[[DonePolicyVerifier]]Getting relatedappid of group throws Exception. groupId: " + group.getId());
        }
        return null;
    }

    @Override
    public String getTargetItemType() {
        return ItemTypes.POLICY;
    }

    @Override
    public String getMarkType() {
        return IllegalMarkTypes.TAG;
    }

    @Override
    public String getDisplayName() {
        return "done-policy-not-deleted";
    }
}
