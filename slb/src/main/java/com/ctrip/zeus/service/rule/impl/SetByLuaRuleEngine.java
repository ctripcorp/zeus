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
import java.util.Map;

@Service("SetByLuaRuleEngine")
public class SetByLuaRuleEngine extends AbstractRuleEngine {

    private final String ENGINE_NAME = this.getClass().getSimpleName();

    public SetByLuaRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_ACCESS, 1);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
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
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=" + ENGINE_NAME + "]]Rule attributes shall not be null");

        Object lua = ruleAttribute.get(RuleAttributeKeys.LUA_COMMAND);
        Object luaVar = ruleAttribute.get(RuleAttributeKeys.LUA_VAR);
        ValidateUtils.notNullAndEmpty(lua, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.LUA_COMMAND + " shall not be null");
        ValidateUtils.notNullAndEmpty(luaVar, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.LUA_VAR + " shall not be null");
        if (!luaVar.toString().startsWith("$")) {
            throw new ValidationException("[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.LUA_VAR + " shall start with $");
        }
    }

    @Override
    public String getType() {
        return RuleType.SET_BY_LUA.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        Map<String, String> luaVars = new HashMap<>();
        for (Rule rule : rules) {

            String attributes = rule.getAttributes();
            HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
            });
            if (ruleAttribute == null) continue;
            String lua = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.LUA_COMMAND));
            String luaVar = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.LUA_VAR));
            if (lua != null && !lua.isEmpty() && luaVar != null && !luaVar.isEmpty()) {
                luaVars.put(luaVar, lua);
            }
        }

        ConfWriter confWriter = new ConfWriter();
        for (String key : luaVars.keySet()) {
            confWriter.writeCommand("set_by_lua " + key, "'" + luaVars.get(key) + "'");
        }
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
