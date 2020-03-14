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

@Service("serverProxyBufferSizeRule")
public class ServerProxyBufferSizeRule extends AbstractRuleEngine {
    private final String ENGINE_NAME = this.getClass().getSimpleName();

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    public ServerProxyBufferSizeRule() {
        registerStage(RuleStages.STAGE_SERVER_ACCESS, 1);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    public String getType() {
        return RuleType.SERVER_PROXY_BUFFER_SIZE_RULE.getName();
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
            throw new ValidationException("[Server Proxy Buffer Rule Engine][Validate]Rule attributes shall not be null");
        }

        Object enableSetting = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        if (enableSetting == null) {
            throw new ValidationException("[Server Proxy Buffer Rule Engine][Validate] Rule enable attribute is required");
        } else {
            ValidateUtils.isBooleanValue(enableSetting, "[Server Proxy Buffer Rule Engine][Validate] Rule enable attribute shall be boolean value");
        }

        validateSize(ruleAttribute, RuleAttributeKeys.PROXY_BUFFER_SIZE,"[Server Proxy Buffer Rule Engine][Validate] Rule Proxy Buffer Size attribute shall be int value");

        validateSize(ruleAttribute, RuleAttributeKeys.PROXY_BUFFERS_SIZE,"[Server Proxy Buffer Rule Engine][Validate] Rule Proxy Buffer Size attribute shall be int value");

        validateSize(ruleAttribute, RuleAttributeKeys.PROXY_BUFFERS_COUNT,"[Server Proxy Buffer Rule Engine][Validate] Rule Buffers Count attribute shall be int value");

        validateSize(ruleAttribute, RuleAttributeKeys.PROXY_BUSY_BUFFERS_SIZE,"[Server Proxy Buffer Rule Engine][Validate] Rule Proxy Busy Buffer Size attribute shall be int value");
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
        StringBuilder stringBuilder = new StringBuilder();
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });

        Boolean enable = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY));
        if (enable == null || !enable) {
            return "";
        }
        StringBuilder proxyBuffersSb = new StringBuilder();
        proxyBuffersSb.append(ruleAttribute.get(RuleAttributeKeys.PROXY_BUFFERS_COUNT).toString());
        proxyBuffersSb.append(" ");
        proxyBuffersSb.append(ruleAttribute.get(RuleAttributeKeys.PROXY_BUFFERS_SIZE).toString());
        proxyBuffersSb.append("k");

        StringBuilder proxyBufferSizeSb = new StringBuilder();
        proxyBufferSizeSb.append(ruleAttribute.get(RuleAttributeKeys.PROXY_BUFFER_SIZE).toString());
        proxyBufferSizeSb.append("k");

        StringBuilder proxyBusySizeSb = new StringBuilder();
        proxyBusySizeSb.append(ruleAttribute.get(RuleAttributeKeys.PROXY_BUSY_BUFFERS_SIZE).toString());
        proxyBusySizeSb.append("k");

        writeCommand(stringBuilder, "proxy_buffer_size", proxyBufferSizeSb);
        writeCommand(stringBuilder, "proxy_buffers", proxyBuffersSb);
        writeCommand(stringBuilder, "proxy_busy_buffers_size", proxyBusySizeSb);

        return stringBuilder.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }


    private void validateSize(HashMap<String, Object> ruleAttribute, String sizeKey, String errorMsg) throws ValidationException {
        Object size = ruleAttribute.get(sizeKey);
        if (size != null) {
            ValidateUtils.isIntValue(size, errorMsg);
        } else {
            throw new ValidationException(errorMsg);
        }
    }

    private void writeCommand(StringBuilder sb, String key, StringBuilder value) {
        if (key != null && value != null) {
            sb.append(key);
            sb.append(" ");
            sb.append(value.toString());
            sb.append(";");
            sb.append("\n");
        }
    }
}
