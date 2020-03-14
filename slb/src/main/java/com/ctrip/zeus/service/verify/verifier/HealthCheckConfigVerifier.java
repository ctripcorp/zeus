package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.HealthCheck;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.GroupListView;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.verify.IllegalMarkTypes;
import com.ctrip.zeus.service.verify.VerifyContext;
import com.ctrip.zeus.service.verify.VerifyResult;
import com.ctrip.zeus.service.verify.VerifyTaggingResult;
import com.ctrip.zeus.tag.ItemTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Discription
 **/
@Component("healthCheckConfigVerifier")
public class HealthCheckConfigVerifier extends AbstractIllegalDataVerifier {

    @Resource
    private ConfigHandler configHandler;

    private final Logger logger = LoggerFactory.getLogger(HealthCheckConfigVerifier.class);

    @Override
    public List<VerifyResult> verify() throws Exception {
        VerifyContext context = getContext();
        if (context != null) {
            GroupListView groupListView = context.getGroups();
            List<Long> groupIds = new ArrayList<>();
            for (ExtendedView.ExtendedGroup extendedGroup : groupListView.getGroups()) {
                Group group = extendedGroup.getInstance();
                if (group == null || group.getHealthCheck() == null) {
                    continue;
                }
                if (isIllegal(group.getHealthCheck())) {
                    groupIds.add(group.getId());
                }
            }
            return Arrays.asList(new VerifyTaggingResult(getTargetItemType(), groupIds, getMarkName()));
        }
        return null;
    }

    private boolean isIllegal(HealthCheck healthCheck) {
        try {
            int timeoutThreshold, failTimeThreshold;
            timeoutThreshold = configHandler.getIntValue("health.check.timeout.upper.threshold", 30000);
            failTimeThreshold = configHandler.getIntValue("health.check.fail.time.threshold", 60000);
            if (healthCheck != null) {
                return healthCheck.getTimeout() >= timeoutThreshold
                        || healthCheck.getIntervals() * healthCheck.getFails() >= failTimeThreshold;
            }
        } catch (Exception e) {
            logger.warn("Exception happens when getting int property through ConfigHandler, message: " + e.getMessage());
            if (healthCheck != null) {
                return healthCheck.getTimeout() >= 30000
                        || healthCheck.getIntervals() * healthCheck.getFails() >= 60000;
            }
        }
        return false;
    }

    @Override
    public String getMarkName() {
        return "ILLEGAL_HEALTH_CHECK_CONFIG";
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
        return "health-check-config-not-validated";
    }
}
