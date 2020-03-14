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

@Service("requestTimeOutRuleEngine")
public class RequestTimeOutRuleEngine extends AbstractRuleEngine {
    private final String ENGINE_NAME = this.getClass().getSimpleName();

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    private DynamicIntProperty proxyReadTimeoutProperty = DynamicPropertyFactory.getInstance().getIntProperty("system.max.proxy.readtimeout.size", 60 * 60);

    public RequestTimeOutRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_ACCESS, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    public String getType() {
        return RuleType.PROXY_READ_TIMEOUT.getName();
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
        String requestTimeoutKey = RuleAttributeKeys.REQUEST_TIMEOUT_KEY;
        if (ruleAttribute == null) {
            throw new ValidationException("[Request timeout Rule Engine][Validate]Rule attributes shall not be null");
        }

        Object sizeObject = ruleAttribute.get(requestTimeoutKey);
        if (sizeObject == null)
            throw new ValidationException("[Request timeout Rule Engine][Validate]Rule attribute does not contain key: " + requestTimeoutKey);

        Integer size;
        try {
            size = Integer.parseInt(sizeObject.toString());
        } catch (NumberFormatException ne) {
            throw new ValidationException("[Request timeout Rule Engine][Validate]Rule attribute key: " + requestTimeoutKey + " shall be in Integer format");
        }
        Integer maxSize = proxyReadTimeoutProperty.getValue();
        if (size <= 0) {
            throw new ValidationException("[Request timeout Rule Engine][Validate]Request timeout is not in expected scope (0," + maxSize + "]");
        }
    }

    @Override
    public String generate(List<Rule> rules,String stage) {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=" + ENGINE_NAME + "]]" + ENGINE_NAME + " Rule Can't Use Multi Rules.");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Rule rule : rules) {
            String attributes = rule.getAttributes();
            HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
            });
            String value = ruleAttribute.get(RuleAttributeKeys.REQUEST_TIMEOUT_KEY).toString();
            stringBuilder.append("proxy_read_timeout");
            stringBuilder.append(" ");
            stringBuilder.append(value);
            stringBuilder.append("s");
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
