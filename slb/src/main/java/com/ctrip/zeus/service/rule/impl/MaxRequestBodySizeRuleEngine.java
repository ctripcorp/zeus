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
import com.ctrip.zeus.support.ObjectJsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service("maxRequestBodySizeRuleEngine")
public class MaxRequestBodySizeRuleEngine extends AbstractRuleEngine {

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    private DynamicIntProperty maxBodySizeProperty = DynamicPropertyFactory.getInstance().getIntProperty("system.max.client.max.body.size", 1024 * 100);

    public MaxRequestBodySizeRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_REQ_RESTRICTIONS, 1);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    public String getType() {
        return RuleType.CLIENT_MAX_BODY_SIZE.getName();
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    public void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes,new TypeReference<HashMap<String, Object>>() {
        });
        String maxBodySizeKey = RuleAttributeKeys.CLIENT_MAX_BODY_SIZE_KEY;
        if (ruleAttribute == null) {
            throw new ValidationException("[MaxBody Size Rule Engine][Validate]Rule attributes does not contains Request Max Body Size value");
        }

        Object sizeObject = ruleAttribute.get(maxBodySizeKey);
        if (sizeObject == null)
            throw new ValidationException("[MaxBody Size Rule Engine][Validate] Rule attribute does not contain key: " + maxBodySizeKey);

        Integer size;
        try {
            size = Integer.parseInt(sizeObject.toString());
        } catch (NumberFormatException ne) {
            throw new ValidationException("[MaxBody Size Rule Engine][Validate] Rule attribute key: " + maxBodySizeKey + "'s shall be in Integer format");
        }
        if (size <= 0) {
            throw new ValidationException("[MaxBody Size Rule Engine][Validate]Client Max body size shall be greater than 0");
        }
    }

    @Override
    public String generate(List<Rule> rules,String stage) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Rule rule : rules) {
            String attributes = rule.getAttributes();
            HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
            });
            String value = ruleAttribute.get(RuleAttributeKeys.CLIENT_MAX_BODY_SIZE_KEY).toString();
            stringBuilder.append("client_max_body_size");
            stringBuilder.append(" ");
            stringBuilder.append(value);
            stringBuilder.append("m");

            stringBuilder.append(";");
        }

        return stringBuilder.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules,stage);
        confWriter.writeLine(line);
    }
}
