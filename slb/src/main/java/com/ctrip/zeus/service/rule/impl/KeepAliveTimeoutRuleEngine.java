package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.AbstractRuleEngine;
import com.ctrip.zeus.service.rule.MergeStrategy;
import com.ctrip.zeus.service.rule.model.RuleAttributeKeys;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.rule.model.RuleType;
import com.ctrip.zeus.service.rule.util.ParserUtils;
import com.ctrip.zeus.service.rule.util.ValidateUtils;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service("KeepAliveTimeoutRuleEngine")
public class KeepAliveTimeoutRuleEngine extends AbstractRuleEngine {

    public KeepAliveTimeoutRuleEngine() {
        registerStage(RuleStages.STAGE_SERVER_ACCESS, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();

        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=KeepAliveTimeoutRuleEngine]]Rule attributes shall not be null");
        Object timeout = ruleAttribute.get(RuleAttributeKeys.KEEP_ALIVE_TIMEOUT);
        ValidateUtils.notNullAndEmpty(timeout, "[[RuleEngine=KeepAliveTimeoutRuleEngine]]Rule " + RuleAttributeKeys.KEEP_ALIVE_TIMEOUT + " shall not be null");
        ValidateUtils.isIntValue(timeout, "[[RuleEngine=KeepAliveTimeoutRuleEngine]]Rule " + RuleAttributeKeys.KEEP_ALIVE_TIMEOUT + " shall be int value");
    }

    @Override
    public String getType() {
        return RuleType.KEEP_ALIVE_TIMEOUT.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=KeepAliveTimeoutRuleEngine]]KeepAliveTimeoutRuleEngine Rule Can't Use Multi Rules.");
        }

        Rule rule = rules.get(0);
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        if (ruleAttribute == null) return "";
        String timeout = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.KEEP_ALIVE_TIMEOUT));

        ConfWriter confWriter = new ConfWriter();
        confWriter.writeCommand("keepalive_timeout", timeout);
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
