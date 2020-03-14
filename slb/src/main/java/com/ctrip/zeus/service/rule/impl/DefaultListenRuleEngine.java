package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.ConfigHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.NotSupportedException;
import java.util.HashMap;
import java.util.List;

@Service("DefaultListenRuleEngine")
public class DefaultListenRuleEngine extends AbstractRuleEngine {

    @Resource
    private MergeStrategy inheritedMergeStrategy;
    @Resource
    private ConfigHandler configHandler;

    private final String ENGINE_NAME = this.getClass().getSimpleName();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultListenRuleEngine() {
        registerStage(RuleStages.STAGE_DEFAULT_SERVER_LISTEN_443, 0);
        registerStage(RuleStages.STAGE_DEFAULT_SERVER_LISTEN_80, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
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
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=" + ENGINE_NAME + "]]Rule attributes shall not be null");

        Object proxy = ruleAttribute.get(RuleAttributeKeys.PROXY_PROTOCOL);
        Object h2 = ruleAttribute.get(RuleAttributeKeys.HTTP_2);
        Object backlog = ruleAttribute.get(RuleAttributeKeys.BACKLOG);
        ValidateUtils.notNullAndEmpty(proxy, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.PROXY_PROTOCOL + " shall not be null");
        ValidateUtils.notNullAndEmpty(h2, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.HTTP_2 + " shall not be null");
        ValidateUtils.notNullAndEmpty(backlog, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.BACKLOG + " shall not be null");

        ValidateUtils.isBooleanValue(proxy, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.PROXY_PROTOCOL + " shall be boolean");
        ValidateUtils.isBooleanValue(h2, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.HTTP_2 + " shall  be boolean");
        ValidateUtils.isIntValue(backlog, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.BACKLOG + " shall  be int");
    }

    @Override
    public String getType() {
        return RuleType.DEFAULT_LISTEN_RULE.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
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

        if (ruleAttribute == null) {
            throw new RuntimeException("[[RuleEngine=" + ENGINE_NAME + "]]Rule attributes shall not be null,RuleID:" + rule.getId());
        }

        Boolean proxyProtocol = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.PROXY_PROTOCOL));
        Boolean http2 = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.HTTP_2));
        Integer backlog = ParserUtils.intValue(ruleAttribute.get(RuleAttributeKeys.BACKLOG));

        try {
            int tmp = configHandler.getIntValue("backlog.min.value", 551);
            if (backlog != null && backlog < tmp) {
                backlog = tmp;
            }
        } catch (Exception e) {
            logger.error("Get BackLog Min Value Failed", e);
            throw new ValidationException("Get BackLog Min Value Failed.");
        }
        StringBuilder sb = new StringBuilder(128);
        if (RuleStages.STAGE_DEFAULT_SERVER_LISTEN_443.equalsIgnoreCase(stage)) {
            sb.append("listen *:443 default_server ");
            if (backlog != null) {
                sb.append("backlog=").append(backlog).append(" ");
            }
            if (http2 != null && http2) {
                sb.append("http2 ");
            }
            if (proxyProtocol != null && proxyProtocol) {
                sb.append("proxy_protocol ");
            }
            sb.append(";");
            return sb.toString();
        } else if (RuleStages.STAGE_DEFAULT_SERVER_LISTEN_80.equalsIgnoreCase(stage)) {
            sb.append("listen *:80 default_server ");
            if (backlog != null) {
                sb.append("backlog=").append(backlog).append(" ");
            }
            if (proxyProtocol != null && proxyProtocol) {
                sb.append("proxy_protocol ");
            }
            sb.append(";");
            return sb.toString();
        } else {
            throw new NotSupportedException("Invalidate Stage For Default Listen Rule;Stage:" + stage);
        }
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
