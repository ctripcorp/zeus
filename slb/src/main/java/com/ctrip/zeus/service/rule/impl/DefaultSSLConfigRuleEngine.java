package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.rule.model.RuleType;
import org.springframework.stereotype.Service;

@Service("DefaultSSLConfigRuleEngine")
public class DefaultSSLConfigRuleEngine extends SSLConfigRuleEngine {
    public DefaultSSLConfigRuleEngine() {
        registerStage(RuleStages.STAGE_DEFAULT_SERVER_SSL_CONFIG, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
    }

    @Override
    public String getType() {
        return RuleType.DEFAULT_SSL_CONFIG.name();
    }

}
