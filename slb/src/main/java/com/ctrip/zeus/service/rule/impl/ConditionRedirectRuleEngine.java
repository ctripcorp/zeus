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
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("ConditionRedirectRuleEngine")
public class ConditionRedirectRuleEngine extends AbstractRuleEngine {

    public ConditionRedirectRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_ACCESS, -100);
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    private final String ENGINE_NAME = this.getClass().getSimpleName();
    @Resource
    private MergeStrategy collectedMergeStrategy;

    @Override
    protected MergeStrategy getMergeStrategy() {
        return collectedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();

        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=" + ENGINE_NAME + "]]Rule attributes shall not be null");

        Object condition = ruleAttribute.get(RuleAttributeKeys.REDIRECT_CONDITION);
        Object target = ruleAttribute.get(RuleAttributeKeys.REDIRECT_TARGET_URL);
        Object code = ruleAttribute.get(RuleAttributeKeys.REDIRECT_CODE);
        ValidateUtils.notNullAndEmpty(condition, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.REDIRECT_CONDITION + " shall not be null");
        ValidateUtils.notNullAndEmpty(target, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.REDIRECT_TARGET_URL + " shall not be null");
        ValidateUtils.notNullAndEmpty(code, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.REDIRECT_CODE + " shall not be null");
        ValidateUtils.isIntValue(code, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.REDIRECT_CODE + " shall be int value");
    }

    @Override
    public String getType() {
        return RuleType.CONDITION_REDIRECT.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        Map<String, String> conditions = new HashMap<>();
        for (Rule rule : rules) {
            String attributes = rule.getAttributes();
            HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
            });
            if (ruleAttribute == null) {
                continue;
            }
            String condition = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.REDIRECT_CONDITION));
            String target = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.REDIRECT_TARGET_URL));
            Integer code = ParserUtils.intValue(ruleAttribute.get(RuleAttributeKeys.REDIRECT_CODE));
            if (code != null && code > 0 && !Strings.isNullOrEmpty(condition) && !Strings.isNullOrEmpty(target)) {
                conditions.put(condition, code + " " + target );
            }
        }

        ConfWriter confWriter = new ConfWriter();
        for (String condition : conditions.keySet()) {
            confWriter.writeIfStart(condition);
            confWriter.writeCommand("return", conditions.get(condition));
            confWriter.writeIfEnd();
        }
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
