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
import com.ctrip.zeus.service.rule.util.ValidateUtils;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("SetRequestHeaderRuleEngine")
public class SetRequestHeaderRuleEngine extends AbstractRuleEngine {

    private final String ENGINE_NAME = this.getClass().getSimpleName();

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    private static final String SLB_NONE_VALUE_FLAG = "SlbNoneValue";

    public SetRequestHeaderRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_ACCESS, 1);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    public String getType() {
        return RuleType.SET_REQUEST_HEADER.getName();
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    public void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        if (ruleAttribute == null) {
            throw new ValidationException("[" + ENGINE_NAME + " Rule Engine][Validate]Rule attributes shall not be null");
        }

        Object key = ruleAttribute.get(RuleAttributeKeys.SET_REQUEST_HEADER_KEY);
        ValidateUtils.notNullAndEmpty(key, "[" + ENGINE_NAME + "  Rule Engine][Validate]Rule attribute does not contain key: " + RuleAttributeKeys.SET_REQUEST_HEADER_KEY);
        Object value = ruleAttribute.get(RuleAttributeKeys.SET_REQUEST_HEADER_VALUE);
        ValidateUtils.notNullAndEmpty(value, "[" + ENGINE_NAME + "  Rule Engine][Validate]Rule attribute does not contain value: " + RuleAttributeKeys.SET_REQUEST_HEADER_VALUE);
        String valueStr = value.toString();
        Pattern pattern = Pattern.compile("[;|\\{|\\}|\\[|\\]]");
        Matcher matcher = pattern.matcher(valueStr);
        if (matcher.find()) {
            throw new ValidationException("[" + ENGINE_NAME + "  Rule Engine][Validate]Invalidate Value. Contains invalid char: [ ] { } ");
        }
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(128);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        for (Rule rule : rules) {
            String attributes = rule.getAttributes();
            HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
            });
            String key = ruleAttribute.get(RuleAttributeKeys.SET_REQUEST_HEADER_KEY).toString();
            String value = ruleAttribute.get(RuleAttributeKeys.SET_REQUEST_HEADER_VALUE).toString();
            value = SLB_NONE_VALUE_FLAG.equalsIgnoreCase(value) ? "\"\"" : value;
            headers.put(key, value);
        }
        for (String key : headers.keySet()) {
            sb.append("proxy_set_header ").append(key).append(" ").append(headers.get(key)).append(";\n");
        }
        return sb.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        if (!Strings.isNullOrEmpty(line)) {
            confWriter.writeLine(line);
        }
    }
}
