package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.AbstractRuleEngine;
import com.ctrip.zeus.service.rule.MergeStrategy;
import com.ctrip.zeus.service.rule.merges.InheritedMergeStrategy;
import com.ctrip.zeus.service.rule.model.RuleAttributeKeys;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.rule.model.RuleType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @Discription
 **/
@Service("sslSessionCacheRuleEngine")
public class SslSessionCacheRuleEngine extends AbstractRuleEngine {

    @Resource
    private InheritedMergeStrategy inheritedMergeStrategy;

    public SslSessionCacheRuleEngine() {
        registerStage(RuleStages.STAGE_SERVER_SSL_CONFIG, -100);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        HashMap<String, Object> attributes = getAttributes(rule);
        if (attributes == null) {
            throw new ValidationException("[" + this.getClass().getSimpleName() + "][validate]attributes should be not null");
        }
        validateAttribute(attributes, RuleAttributeKeys.SSL_SESSION_CACHE, "String");
        validateAttribute(attributes, RuleAttributeKeys.SSL_SESSION_CACHE_TIMEOUT, "String");
    }

    @Override
    public String getType() {
        return RuleType.SSL_SESSION_CACHE.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() > 1) {
            throw new RuntimeException("[[RuleEngine=" + this.getClass().getSimpleName() + "]]" + this.getClass().getSimpleName() + " Rule Can't Use Multi Rules.");
        }
        HashMap<String, Object> attributes = getAttributes(rules.get(0));
        Object sslSessionCache = attributes.get(RuleAttributeKeys.SSL_SESSION_CACHE);
        Object sslSessionCacheTimeout = attributes.get(RuleAttributeKeys.SSL_SESSION_CACHE_TIMEOUT);

        ConfWriter confWriter = new ConfWriter();
        confWriter.writeCommand("ssl_session_cache", (String) sslSessionCache);
        confWriter.writeCommand("ssl_session_timeout", (String) sslSessionCacheTimeout);
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        String content = generate(rules, stage);
        if (content != null && !content.isEmpty()) {
            confWriter.writeLine(content);
        }
    }
}
