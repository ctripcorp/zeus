package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.rule.model.RuleType;
import org.springframework.stereotype.Service;

@Service("defaultErrorPageRuleEngine")
public class DefaultErrorPageRuleEngine extends ErrorPageRuleEngine {

    public DefaultErrorPageRuleEngine() {
        registerStage(RuleStages.STAGE_DEFAULT_SERVER_ERROR_PAGE, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
    }

    @Override
    public String getType() {
        return RuleType.DEFAULT_ERROR_PAGE.getName();
    }
}
