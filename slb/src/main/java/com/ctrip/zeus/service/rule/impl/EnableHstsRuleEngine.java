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
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service("enableHstsRuleEngine")
public class EnableHstsRuleEngine extends AbstractRuleEngine {
    private final String ENGINE_NAME = this.getClass().getSimpleName();

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    private DynamicStringProperty defaultHstsHeader = DynamicPropertyFactory.getInstance().getStringProperty("system.default.hsts.header", "Strict-Transport-Security");
    private DynamicStringProperty defaultHstsMaxAge = DynamicPropertyFactory.getInstance().getStringProperty("system.default.hsts.max-age", "3600");

    public EnableHstsRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_HSTS_SUPPORT, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
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
            throw new ValidationException("[Enable HSTS Rule Engine][Validate]Rule attributes is null");
        }

        Object maxAgeObject = ruleAttribute.get(RuleAttributeKeys.HSTS_MAX_AGE_KEY);
        maxAgeObject = maxAgeObject == null ? defaultHstsMaxAge.get() : maxAgeObject;
        try {
            Integer.parseInt(maxAgeObject.toString());
        } catch (NumberFormatException ne) {
            throw new ValidationException("[Enable HSTS Rule Engine][Validate]HSTS max age " + maxAgeObject + " is not in Integer format");
        }
    }

    @Override
    public String getType() {
        return RuleType.ENABLE_HSTS.getName();
    }

    @Override
    public String generate(List<Rule> rules,String stage) {
        if (rules == null || rules.size() == 0) {
            return null;
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=" + ENGINE_NAME + "]]" + ENGINE_NAME + " Rule Can't Use Multi Rules.");
        }

        Rule rule = rules.get(0);

        StringBuilder stringBuilder = new StringBuilder();
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes,new TypeReference<HashMap<String, Object>>() {
        });

        String header = defaultHstsHeader.get();
        Object maxAge = ruleAttribute.get(RuleAttributeKeys.HSTS_MAX_AGE_KEY);
        maxAge = maxAge == null ? defaultHstsMaxAge.get() : maxAge.toString();

        Object enabledObj = ruleAttribute.get(RuleAttributeKeys.HSTS_ENABLED_KEY);
        boolean isEnableHstsRule = enabledObj == null || Boolean.parseBoolean(enabledObj.toString());
        if (!isEnableHstsRule) return null;

        stringBuilder.append("proxy_hide_header ");
        stringBuilder.append(header);
        stringBuilder.append(";");

        stringBuilder.append("\n");

        stringBuilder.append("add_header ");
        stringBuilder.append(header + " ");
        stringBuilder.append("\"max-age=" + maxAge + "\"");

        stringBuilder.append(";");

        return stringBuilder.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules,stage);
        if (line != null) {
            confWriter.writeLine(line);
        }
    }
}
