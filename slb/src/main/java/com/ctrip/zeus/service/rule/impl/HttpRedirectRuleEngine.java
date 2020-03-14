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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service("HttpRedirectRuleEngine")
public class HttpRedirectRuleEngine extends AbstractRuleEngine {

    @Resource
    private ConfigHandler configHandler;

    public HttpRedirectRuleEngine() {
        registerStage(RuleStages.STAGE_SERVER_ACCESS, -100);
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
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=HTTP_REDIRECT_RULE]]Rule attributes shall not be null");
        Object enable = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        ValidateUtils.notNullAndEmpty(enable, "[[RuleEngine=HTTP_REDIRECT_RULE]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall not be null");
        ValidateUtils.isBooleanValue(enable, "[[RuleEngine=HTTP_REDIRECT_RULE]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall be bool value");

        Object code = ruleAttribute.get(RuleAttributeKeys.HTTP_REDIRECT_CODE);
        ValidateUtils.notNullAndEmpty(code, "[[RuleEngine=HTTP_REDIRECT_RULE]]Rule " + RuleAttributeKeys.HTTP_REDIRECT_CODE + " shall not be null");
        ValidateUtils.isIntValue(code, "[[RuleEngine=HTTP_REDIRECT_RULE]]Rule " + RuleAttributeKeys.HTTP_REDIRECT_CODE + " shall be Integer value");
        Integer codeValue = ParserUtils.intValue(code);
        if (codeValue == null || (codeValue != 301 && codeValue != 302 && codeValue != 307)) {
            throw new ValidationException("Code Should Be 301/302/307");
        }
    }

    @Override
    public String getType() {
        return RuleType.HTTP_REDIRECT_RULE.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=HTTP_REDIRECT_RULE]]HTTP_REDIRECT_RULE Rule Can't Use Multi Rules.");
        }

        Rule rule = rules.get(0);
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        if (ruleAttribute == null) return "";
        Boolean enable = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY));
        Integer code = ParserUtils.intValue(ruleAttribute.get(RuleAttributeKeys.HTTP_REDIRECT_CODE));
        String target = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.HTTP_REDIRECT_TARGETURL));
        if (target == null) {
            target = configHandler.getStringValue("http.redirect.rule.default.target", "https://$host$request_uri");
        }
        if (enable == null || !enable){
            return "";
        }

        ConfWriter confWriter = new ConfWriter();
        confWriter.writeLocationStart("~* ^/");
        confWriter.writeCommand("return", code + " \"" + target + "\"");
        confWriter.writeLocationEnd();
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
