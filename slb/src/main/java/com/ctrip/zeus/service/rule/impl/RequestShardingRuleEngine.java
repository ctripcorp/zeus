package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Condition;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.model.model.ShardingRuleAttribute;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.AbstractRuleEngine;
import com.ctrip.zeus.service.rule.MergeStrategy;
import com.ctrip.zeus.service.rule.model.*;
import com.ctrip.zeus.service.rule.util.ValidateUtils;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service("requestShardingRuleEngine")
public class RequestShardingRuleEngine extends AbstractRuleEngine {
    @Resource
    private MergeStrategy inheritedMergeStrategy;
    @Resource
    private ConfigHandler configHandler;

    private Rule defaultRule;


    public RequestShardingRuleEngine() {
        registerStage(RuleStages.STAGE_REQUEST_INTERCEPTION, 0);
        registerStage(RuleStages.STAGE_LOCATION_LUA_REWRITE, -101);
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

        ShardingRuleAttribute attribute = ObjectJsonParser.parse(attributes, ShardingRuleAttribute.class);
        ValidateUtils.notNullAndEmpty(attribute, "[[RuleEngine=" + getEngineName() + "]]Rule attribute shall not be null");

        Condition condition = attribute.getCondition();
        Double percent = attribute.getPercent();
        validateCondition(condition);
        if (percent < 0 || percent > 1) {
            throw new ValidationException("[[RuleEngine=" + getEngineName() + "]]Percent Should Be A Number In [0,1]");
        }
    }

    @Override
    public String getType() {
        return RuleType.SHARDING_RULE.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules.size() > 1) {
            throw new ValidationException("[[RuleEngine=" + getEngineName() + "]]Multi Rules.");
        }
        ConfWriter confWriter = new ConfWriter();
        generate(rules, confWriter, stage);
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        if (RuleStages.STAGE_REQUEST_INTERCEPTION.equalsIgnoreCase(stage)) {
            buildPreCondition(confWriter, rules.get(0));
        } else {
            buildActionInLua(confWriter, rules.get(0));
        }
    }

    private void buildActionInLua(ConfWriter confWriter, Rule rule) {
        ShardingRuleAttribute sharedingRuleAttribute = ObjectJsonParser.parse(rule.getAttributes(), ShardingRuleAttribute.class);
        if (sharedingRuleAttribute == null || (sharedingRuleAttribute.getEnable() != null && !sharedingRuleAttribute.getEnable())) {
            return;
        }
        confWriter.startLuaZone();

        Condition condition = sharedingRuleAttribute.getCondition();
        Double percent = sharedingRuleAttribute.getPercent();
        if (percent == null) {
            percent = 0.0;
        }
        String command;
        if (condition == null) {
            command = "true";
        } else {
            List<Condition> conditions = getPreConditions(condition);
            conditions.sort(Comparator.comparing(Object::toString));
            for (int i = 0; i < conditions.size(); i++) {
                Condition c = conditions.get(i);
                c.setAlias(getTmpVarName(rule, i));
            }
            command = getConditionCommand(condition);
        }
        // if condition is true; exec sha group by percent.
        // if condition is false, exec sha group by default.
        // if condition is null, by percent.
        if (condition != null) {
            confWriter.writeLine("if ( " + command + " ) then");
        }
        confWriter.writeLine("local shardingRandom = math.random()");
        confWriter.writeLine("if ( shardingRandom < " + String.format("%.2f", percent) + " ) then");
        confWriter.writeLine("ngx.exec(\"@" + RuleShardingConstants.NAMED_GROUP_NAME + "\")");
        confWriter.writeLine("end");
        if (condition != null) {
            confWriter.writeLine("else");
            confWriter.writeLine("ngx.exec(\"@" + RuleShardingConstants.NAMED_GROUP_NAME + "\")");
            confWriter.writeLine("end");
        }
        confWriter.endLuaZone();
    }

    private String getConditionCommand(Condition condition) {
        if (ConditionType.SELF.getName().equalsIgnoreCase(condition.getType())) {
            ConditionFunction conditionFunction = ConditionFunction.getFunction(condition.getFunction());
            if (conditionFunction != null && isRegex(conditionFunction) && ConditionTarget.parserFromTarget(condition.getTarget()).getType() != ConditionTargetType.CIP) {
                return "ngx.var." + condition.getAlias() + " == \"true\"";
            } else {
                ConditionTarget t = ConditionTarget.parserFromTarget(condition.getTarget());
                if (t == null || t.getType() == null) {
                    return "";
                }
                return parserLuaTarget(t, condition.getFunction(), condition.getValue());
            }

        } else {
            List<String> cmds = new ArrayList<>();
            for (Condition c : condition.getComposit()) {
                cmds.add(getConditionCommand(c));
            }
            return "(" + Joiner.on(" " + condition.getType().toLowerCase() + " ").join(cmds) + ")";
        }
    }

    private String parserLuaTarget(ConditionTarget target, String function, String value) {
        ConditionFunction luaFunction = ConditionFunction.getFunction(function);
        if (luaFunction == null) {
            return "";
        }
        switch (target.getType()) {
            case ARG:
                return "ngx.var.arg_" + parserKey(target.getKey()) + " " + luaFunction.getLuaFunc() + parserValueInLua(value, true);
            case COOKIE:
                return "ngx.var.cookie_" + parserKey(target.getKey()) + " " + luaFunction.getLuaFunc() + parserValueInLua(value, true);
            case HEADER:
                return "ngx.var.http_" + parserKey(target.getKey()) + " " + luaFunction.getLuaFunc() + parserValueInLua(value, true);
            case URI:
                return "ngx.var.uri " + luaFunction.getLuaFunc() + parserValueInLua(value, true);
            case URL:
                return "ngx.var.request_uri " + luaFunction.getLuaFunc() + parserValueInLua(value, true);
            case METHOD:
                return "ngx.var.request_method " + luaFunction.getLuaFunc() + parserValueInLua(value, true);
            default:
                return "";
        }
    }

    private String parserTarget(ConditionTarget target, String function, String value) {
        ConditionFunction conditionFunction = ConditionFunction.getFunction(function);
        if (conditionFunction == null || conditionFunction != ConditionFunction.NOTLIKE && conditionFunction != ConditionFunction.NOTLIKEIGNORECASE
                && conditionFunction != ConditionFunction.LIKE && conditionFunction != ConditionFunction.LIKEIGNORECASE) {
            return "";
        }
        switch (target.getType()) {
            case ARG:
                return "$arg_" + parserKey(target.getKey()) + " " + conditionFunction.getNginxFunc() + parserValue(value, true);
            case COOKIE:
                return "$cookie_" + parserKey(target.getKey()) + " " + conditionFunction.getNginxFunc() + parserValue(value, true);
            case HEADER:
                return "$http_" + parserKey(target.getKey()) + " " + conditionFunction.getNginxFunc() + parserValue(value, true);
            case URI:
                return "$uri " + conditionFunction.getNginxFunc() + parserValue(value, true);
            case URL:
                return "$request_uri " + conditionFunction.getNginxFunc() + parserValue(value, true);
            case METHOD:
                return "$request_method " + conditionFunction.getLuaFunc() + parserValue(value, true);
            default:
                return "";
        }
    }

    private String parserKey(String key) {
        if (org.apache.commons.lang.StringUtils.isNotEmpty(key)) {
            return key.replace("-", "_");
        }
        return key;
    }

    private String parserValue(String value, boolean checkIsNil) {
        if (checkIsNil) {
            if (value != null && value.trim().equalsIgnoreCase("nil")) {
                return " nil ";
            } else {
                return " \"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\" ";
            }
        } else {
            return " \"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\" ";
        }
    }

    private String parserValueInLua(String value, boolean checkIsNil) {
        if (checkIsNil) {
            if (value != null && value.trim().equalsIgnoreCase("nil")) {
                return " nil ";
            } else {
                return " \"" + value.replace("\\", "\\\\\\\\").replace("\"", "\\\\\\\"") + "\" ";
            }
        } else {
            return " \"" + value.replace("\\", "\\\\\\\\").replace("\"", "\\\\\\\"") + "\" ";
        }
    }


    private void buildPreCondition(ConfWriter confWriter, Rule rule) {
        ShardingRuleAttribute conditionRuleAttribute = ObjectJsonParser.parse(rule.getAttributes(), ShardingRuleAttribute.class);
        if (conditionRuleAttribute == null || conditionRuleAttribute.getCondition() == null
                || (conditionRuleAttribute.getEnable() != null && !conditionRuleAttribute.getEnable())) return;
        Condition condition = conditionRuleAttribute.getCondition();
        List<Condition> conditions = getPreConditions(condition);
        conditions.sort(Comparator.comparing(Object::toString));
        for (int i = 0; i < conditions.size(); i++) {
            Condition c = conditions.get(i);
            ConditionTarget t = ConditionTarget.parserFromTarget(c.getTarget());
            if (t == null || t.getType() == null) {
                continue;
            }
            confWriter.writeIfStart(parserTarget(t, c.getFunction(), c.getValue()));
            confWriter.writeCommand("set", "$" + getTmpVarName(rule, i) + " \"true\"");
            confWriter.writeIfEnd();
        }
    }

    private String getTmpVarName(Rule rule, int index) {
        return "r_" + rule.getId() + "_" + index;
    }

    private List<Condition> getPreConditions(Condition condition) {
        List<Condition> result = new ArrayList<>();
        if (ConditionType.SELF.getName().equalsIgnoreCase(condition.getType())) {
            ConditionFunction conditionFunction = ConditionFunction.getFunction(condition.getFunction());
            if (isRegex(conditionFunction)) {
                // Cip Should Match In Lua.
                if (ConditionTarget.parserFromTarget(condition.getTarget()).getType() != ConditionTargetType.CIP) {
                    result.add(condition);
                }
            }
        } else {
            for (Condition c : condition.getComposit()) {
                result.addAll(getPreConditions(c));
            }
        }

        return result;
    }

    private boolean isRegex(ConditionFunction conditionFunction) {
        return conditionFunction == ConditionFunction.NOTLIKE || conditionFunction == ConditionFunction.NOTLIKEIGNORECASE
                || conditionFunction == ConditionFunction.LIKE || conditionFunction == ConditionFunction.LIKEIGNORECASE;
    }

    private void validateCondition(Condition condition) throws ValidationException {
        if (condition == null) {
            return;
        }
        // condition type field validation
        ConditionType type = ConditionType.getType(condition.getType());
        ValidateUtils.notNullAndEmpty(type, "[[RuleEngine=" + getEngineName() + "]]Rule condition type is not allowed");
        if (type == ConditionType.SELF) {
            ConditionTarget target = ConditionTarget.parserFromTarget(condition.getTarget());
            ValidateUtils.notNullAndEmpty(target, "[[RuleEngine=" + getEngineName() + "]]Rule condition target " + condition.getTarget() + " is not allowed");
            ValidateUtils.notNullAndEmpty(target.getType(), "[[RuleEngine=" + getEngineName() + "]]Rule condition target " + condition.getTarget() + " is not allowed");
            switch (target.getType()) {
                case HEADER:
                case COOKIE:
                case ARG:
                    ValidateUtils.notNullAndEmpty(target.getKey(), "[[RuleEngine=" + getEngineName() + "]]Rule condition target " + condition.getTarget() + " with empty key is not allowed");
                    if (target.getKey().length() > 64) {
                        throw new ValidationException("[[RuleEngine=" + getEngineName() + "]]Rule condition target " + condition.getTarget() + " with too long key name is not allowed");
                    }
                    Pattern pattern = Pattern.compile("^(-|\\w|\\.)+$");
                    Matcher matcher = pattern.matcher(target.getKey());
                    if (!matcher.find()) {
                        throw new ValidationException("Only [A-Za-z0-9_] is allowed for target key.");
                    }
                    break;
            }


            String fn = condition.getFunction();
            ConditionFunction func = ConditionFunction.getFunction(fn);
            ValidateUtils.notNullAndEmpty(func, "[[RuleEngine=" + getEngineName() + "]]Rule condition Function " + fn + " is not allowed");
            ValidateUtils.notNullAndEmpty(condition.getValue(), "[[RuleEngine=" + getEngineName() + "]]Rule condition value " + condition.getValue() + " is not allowed");
            if (func == ConditionFunction.LIKE || func == ConditionFunction.LIKEIGNORECASE ||
                    func == ConditionFunction.NOTLIKE || func == ConditionFunction.NOTLIKEIGNORECASE) {
                try {
                    Pattern pattern = Pattern.compile(condition.getValue());
                } catch (Exception e) {
                    throw new ValidationException("Value is invalidate.Value:" + condition.getValue());
                }
            }

        } else {
            List<Condition> conditions = condition.getComposit();
            ValidateUtils.notNullAndEmpty(conditions, "[[RuleEngine=" + getEngineName() + "]]Rule condition composite shall not be empty");
            for (Condition c : conditions) {
                validateCondition(c);
            }
        }
    }

    public Rule getDefaultRule() throws JsonProcessingException {
        if (defaultRule == null) {
            Rule tmp = new Rule();
            ShardingRuleAttribute attr = new ShardingRuleAttribute();
            attr.setCondition(null);
            attr.setPercent(1.0);
            tmp.setRuleType(RuleType.SHARDING_RULE.name()).setName("Default Rule").setTargetType(RuleTargetType.GROUP.getName())
                    .setAttributes(ObjectJsonWriter.write(attr));
            defaultRule = tmp;
        }
        return defaultRule;
    }
}
