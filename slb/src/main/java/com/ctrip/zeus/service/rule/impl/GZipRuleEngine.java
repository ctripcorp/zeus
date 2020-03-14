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

@Service("GZipRuleEngine")
public class GZipRuleEngine extends AbstractRuleEngine {

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    public GZipRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_ACCESS, -100);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();

        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        if (ruleAttribute == null) {
            throw new ValidationException("[[RuleEngine=GZipRuleEngine]]Rule attributes shall not be null");
        }
        String enabledKey = RuleAttributeKeys.ENABLED_KEY;

        Object enabledObject = ruleAttribute.get(enabledKey);
        ValidateUtils.notNullAndEmpty(enabledObject, "[[RuleEngine=GZipRuleEngine]][Validate]Rule attribute does not contain key: " + enabledKey);
        Object level = ruleAttribute.get(RuleAttributeKeys.GZIP_COMP_LEVEL);
        Object len = ruleAttribute.get(RuleAttributeKeys.GZIP_MIN_LEN);
        Object count = ruleAttribute.get(RuleAttributeKeys.GZIP_BUFFER_COUNT);
        Object size = ruleAttribute.get(RuleAttributeKeys.GZIP_BUFFER_SIZE);

        ValidateUtils.isBooleanValue(enabledObject, "[[RuleEngine=GZipRuleEngine]][Validate]Rule attribute key: " + enabledKey + " shall be in Boolean format");
        ValidateUtils.isIntValue(level, "[[RuleEngine=GZipRuleEngine]][Validate]Rule attribute key: " + RuleAttributeKeys.GZIP_COMP_LEVEL + " shall be in Int format");
        ValidateUtils.isIntValue(len, "[[RuleEngine=GZipRuleEngine]][Validate]Rule attribute key: " + RuleAttributeKeys.GZIP_MIN_LEN + " shall be in Int format");
        ValidateUtils.isIntValue(count, "[[RuleEngine=GZipRuleEngine]][Validate]Rule attribute key: " + RuleAttributeKeys.GZIP_BUFFER_COUNT + " shall be in Int format");
        ValidateUtils.isIntValue(size, "[[RuleEngine=GZipRuleEngine]][Validate]Rule attribute key: " + RuleAttributeKeys.GZIP_BUFFER_SIZE + " shall be in Int format");
    }

    @Override
    public String getType() {
        return RuleType.GZIP.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=GZipRuleEngine]]GZIP Rule Can't Set Multi Rules.");
        }

        Rule rule = rules.get(0);
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        if (ruleAttribute == null) return "";
        Boolean enable = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY));
        String type = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.GZIP_TYPES));
        String level = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.GZIP_COMP_LEVEL));
        String len = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.GZIP_MIN_LEN));
        String bufferCount = ruleAttribute.get(RuleAttributeKeys.GZIP_BUFFER_COUNT).toString();
        String bufferSize = ruleAttribute.get(RuleAttributeKeys.GZIP_BUFFER_SIZE).toString();

        if (enable == null || !enable) {
            return "";
        }

        ConfWriter writer = new ConfWriter();
        writer.writeCommand("gzip", "on");
        writer.writeCommand("gzip_types", type);
        writer.writeCommand("gzip_min_length", len);
        writer.writeCommand("gzip_comp_level", level);
        writer.writeCommand("gzip_buffers", bufferCount + " " + bufferSize + "k");
        return writer.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
