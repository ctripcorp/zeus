package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.build.conf.LocationConf;
import com.ctrip.zeus.service.ipblock.impl.IpBlockKeyConsts;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.AbstractRuleEngine;
import com.ctrip.zeus.service.rule.MergeStrategy;
import com.ctrip.zeus.service.rule.model.RuleAttributeKeys;
import com.ctrip.zeus.service.rule.model.RuleDataContext;
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

import static com.ctrip.zeus.service.rule.impl.LargeClientHeaderRuleEngine.ENGINE_NAME;

@Service("RequestInterceptForBlackListRuleEngine")
public class RequestInterceptForBlackListRuleEngine extends AbstractRuleEngine {
    @Resource
    private MergeStrategy inheritedMergeStrategy;
    @Resource
    private ConfigHandler configHandler;


    public RequestInterceptForBlackListRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_LUA_REWRITE, -200);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
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

        Object enable = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        Object globalEnable = ruleAttribute.get(RuleAttributeKeys.GLOBAL_LIST_ENABLED_KEY);
        Object code = ruleAttribute.get(RuleAttributeKeys.REJECT_CODE);
        Object message = ruleAttribute.get(RuleAttributeKeys.REJECT_MESSAGE);
        ValidateUtils.notNullAndEmpty(enable, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall not be null");
        ValidateUtils.notNullAndEmpty(globalEnable, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.GLOBAL_LIST_ENABLED_KEY + " shall not be null");
        ValidateUtils.isBooleanValue(enable, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall be Boolean value");
        ValidateUtils.isBooleanValue(globalEnable, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.GLOBAL_LIST_ENABLED_KEY + " shall be Boolean value");
        ValidateUtils.isIntValue(code, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall be int value");
        int codeValue = Integer.parseInt(code.toString());
        if (codeValue < 400 || codeValue > 499) {
            throw new ValidationException("Code Should In Range(400,499)");
        }
    }

    @Override
    public String getType() {
        return RuleType.REQUEST_INTERCEPT_FOR_IP_BLACK_LIST_RULE.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        throw new ValidationException("This Rule Engine Need Context Value. This Method Should Not Be Used");
    }

    public void generate(List<Rule> rules, ConfWriter confWriter, String stage, RuleDataContext ruleDataContext) throws ValidationException {
        if (rules == null || rules.size() != 1) {
            return;
        }
        String attributes = rules.get(0).getAttributes();

        if (ruleDataContext.getGroup() == null) {
            return;
        }
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });

        Object enable = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        Object globalEnable = ruleAttribute.get(RuleAttributeKeys.GLOBAL_LIST_ENABLED_KEY);
        Object code = ruleAttribute.get(RuleAttributeKeys.REJECT_CODE);
        Object message = ruleAttribute.get(RuleAttributeKeys.REJECT_MESSAGE);
        Boolean enableValue = ParserUtils.booleanValue(enable);
        Boolean globalEnableValue = ParserUtils.booleanValue(globalEnable);
        String msg = message == null ? "" : message.toString();

        if (enableValue == null || !enableValue) {
            return;
        }

        String appId = ruleDataContext.getGroup().getAppId();
        if (appId == null) {
            return;
        }
        String clientIp = "ngx.var." + LocationConf.TRUE_CLIENT_IP_VAR;
        confWriter.startLuaZone();

        if (globalEnableValue != null && globalEnableValue) {
            confWriter.writeLine("if (( nil ~= ipList[\"" + appId + "\"]  and ipList[\"" + appId + "\"][" + clientIp +
                    "] ~= nil ) or (nil ~= ipList[\"" + IpBlockKeyConsts.GLOBAL_KEY + "\"]  and ipList[\""
                    + IpBlockKeyConsts.GLOBAL_KEY + "\"][" + clientIp + "] ~= nil )) then");
        } else {
            confWriter.writeLine("if ( nil ~= ipList[\"" + appId + "\"]  and ipList[\"" + appId + "\"][" + clientIp +
                    "] ~= nil ) then");
        }

        confWriter.writeLine("ngx.header.content_type = \"text/plain\"");
        confWriter.writeLine("ngx.status = " + code);

        confWriter.writeLine("ngx.say(" + parserValue(msg) + ")");
        confWriter.writeLine("return ngx.exit(" + code + ")");
        confWriter.writeLine("end");
        confWriter.endLuaZone();
    }

    private String parserValue(String value) {
        return " \"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\" ";
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        throw new ValidationException("This Rule Engine Need Context Value. This Method Should Not Be Used");
    }
}
