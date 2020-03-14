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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("DirectiveConfigRuleEngine")
public class DirectiveConfigRuleEngine extends AbstractRuleEngine {

    private final String ENGINE_NAME = this.getClass().getSimpleName();

    public DirectiveConfigRuleEngine() {
        registerStage(RuleStages.STAGE_SERVER_ACCESS, -100);
        registerStage(RuleStages.STAGE_SERVER_BOTTOM, -100);
        registerStage(RuleStages.STAGE_LOCATION_REQ_RESTRICTIONS, -100);
        registerStage(RuleStages.STAGE_LOCATION_HSTS_SUPPORT, -100);
        registerStage(RuleStages.STAGE_LOCATION_ACCESS, -100);
        registerStage(RuleStages.STAGE_LOCATION_LUA_REWRITE, -100);
        registerStage(RuleStages.STAGE_LOCATION_LUA_ACCESS, -100);
        registerStage(RuleStages.STAGE_LOCATION_LUA_HEADER_FILTER, -100);
        registerStage(RuleStages.STAGE_LOCATION_LUA_INIT, -100);
        registerStage(RuleStages.STAGE_LOCATION_BOTTOM, -100);
        registerStage(RuleStages.STAGE_LOCATION_UPSTREAM_ACCESS, -100);
        registerStage(RuleStages.STAGE_LOCATION_UPSTREAM_BOTTOM, -100);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

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

        Object key = ruleAttribute.get(RuleAttributeKeys.DIRECTIVE_KEY);
        Object value = ruleAttribute.get(RuleAttributeKeys.DIRECTIVE_VALUE);
        Object stage = ruleAttribute.get(RuleAttributeKeys.DIRECTIVE_STAGE);
        ValidateUtils.notNullAndEmpty(key, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.DIRECTIVE_KEY + " shall not be null");
        ValidateUtils.notNullAndEmpty(value, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.DIRECTIVE_VALUE + " shall not be null");
        ValidateUtils.notNullAndEmpty(stage, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.DIRECTIVE_STAGE + " shall not be null");
        if (!getStageOrders().containsKey(stage.toString().toUpperCase())) {
            throw new ValidationException("[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.DIRECTIVE_STAGE + " is invalidate.");
        }
    }

    @Override
    public String getType() {
        return RuleType.DIRECTIVE_CONFIG.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        Map<String, String> commandMap = new LinkedHashMap<>();
        for (Rule rule : rules) {
            String attributes = rule.getAttributes();
            HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, HashMap.class);
            if (ruleAttribute == null) continue;
            String ruleStage = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.DIRECTIVE_STAGE));
            String key = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.DIRECTIVE_KEY));
            String value = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.DIRECTIVE_VALUE));
            if (!Strings.isNullOrEmpty(ruleStage) && !Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(value)
                    && ruleStage.equalsIgnoreCase(stage) && !commandMap.containsKey(key)) {
                commandMap.put(key, value);
            }
        }

        ConfWriter confWriter = new ConfWriter();
        for (String key : commandMap.keySet()) {
            confWriter.writeCommand(key, commandMap.get(key));
        }
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
