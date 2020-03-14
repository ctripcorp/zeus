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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service("HideHeaderRuleEngine")
public class HideHeaderRuleEngine extends AbstractRuleEngine {

    private final String ENGINE_NAME = this.getClass().getSimpleName();
    @Resource
    private MergeStrategy collectedMergeStrategy;

    public HideHeaderRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_ACCESS, -5);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return collectedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();

        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes,new TypeReference<HashMap<String, Object>>() {
        });
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=" + ENGINE_NAME + "]]Rule attributes shall not be null");

        Object key = ruleAttribute.get(RuleAttributeKeys.HEADER_KEY);
//        Object value = ruleAttribute.get(RuleAttributeKeys.HEADER_VALUE);
        ValidateUtils.notNullAndEmpty(key, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.HEADER_KEY + " shall not be null");
//        ValidateUtils.notNullAndEmpty(value, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.HEADER_VALUE + " shall be int value");
    }

    @Override
    public String getType() {
        return RuleType.HIDE_HEADER.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        Set<String> hideHeaders = new HashSet<>();
        for (Rule rule : rules) {

            String attributes = rule.getAttributes();
            HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
            });
            if (ruleAttribute == null) continue;
            String hideKey = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.HEADER_KEY));
            if (hideKey != null && !hideKey.isEmpty()) {
                hideHeaders.add(hideKey);
            }
        }

        ConfWriter confWriter = new ConfWriter();
        for (String hideKey : hideHeaders) {
            confWriter.writeCommand("proxy_hide_header", hideKey);
        }
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
