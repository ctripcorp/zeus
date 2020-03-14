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

@Service("LargeClientHeaderRuleEngine")
public class LargeClientHeaderRuleEngine extends AbstractRuleEngine {

    final static String ENGINE_NAME = "LargeClientHeaderRuleEngine";

    public LargeClientHeaderRuleEngine() {
        registerStage(RuleStages.STAGE_SERVER_ACCESS, -2);
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
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=" + ENGINE_NAME + "]]Rule attributes shall not be null");

        Object count = ruleAttribute.get(RuleAttributeKeys.LARGE_CLIENT_HEADER_COUNT);
        Object size = ruleAttribute.get(RuleAttributeKeys.LARGE_CLIENT_HEADER_SIZE);
        ValidateUtils.notNullAndEmpty(count, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.LARGE_CLIENT_HEADER_COUNT + " shall not be null");
        ValidateUtils.notNullAndEmpty(size, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.LARGE_CLIENT_HEADER_SIZE + " shall not be null");

        ValidateUtils.isIntValue(count, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.LARGE_CLIENT_HEADER_COUNT + " shall be int value");
        ValidateUtils.isIntValue(size, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.LARGE_CLIENT_HEADER_SIZE + " shall be int value");
    }

    @Override
    public String getType() {
        return RuleType.LARGE_CLIENT_HEADER.name();
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
        String count = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.LARGE_CLIENT_HEADER_COUNT));
        String size = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.LARGE_CLIENT_HEADER_SIZE));

        ConfWriter confWriter = new ConfWriter();
        confWriter.writeCommand("large_client_header_buffers", count + " " + size + "k");
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
