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

@Service("ProxyRequestBufferRuleEngine")
public class ProxyRequestBufferRuleEngine extends AbstractRuleEngine {

    public ProxyRequestBufferRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_ACCESS, -3);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    private final String ENGINE_NAME = this.getClass().getSimpleName();
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
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=" + ENGINE_NAME + "]]Rule attributes shall not be null");

        Object enable = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        ValidateUtils.notNullAndEmpty(enable, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall not be null");
        ValidateUtils.isBooleanValue(enable, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall be int value");
    }

    @Override
    public String getType() {
        return RuleType.PROXY_REQUEST_BUFFER_ENABLE.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=" + ENGINE_NAME + "]]" + ENGINE_NAME + " Rule Can't Use Multi Rules.");
        }

        Rule rule = rules.get(0);
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        if (ruleAttribute == null) return "";
        Boolean enable = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY));
        if (enable != null && enable) {
            ConfWriter confWriter = new ConfWriter();
            confWriter.writeCommand("proxy_request_buffering", "on");
            return confWriter.toString();
        } else {
            ConfWriter confWriter = new ConfWriter();
            confWriter.writeCommand("proxy_request_buffering", "off");
            return confWriter.toString();
        }

    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }

}
