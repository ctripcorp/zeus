package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.build.conf.LocationConf;
import com.ctrip.zeus.service.build.conf.LogFormat;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.AbstractRuleEngine;
import com.ctrip.zeus.service.rule.MergeStrategy;
import com.ctrip.zeus.service.rule.model.*;
import com.ctrip.zeus.service.rule.util.ValidateUtils;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ctrip.zeus.service.rule.impl.LargeClientHeaderRuleEngine.ENGINE_NAME;

@Service("requestInterceptRuleEngine")
public class RequestInterceptRuleEngine extends AbstractRuleEngine {
    @Resource
    private MergeStrategy collectedMergeStrategy;
    @Resource
    private ConfigHandler configHandler;

    private final String TABLE_LEN_FUNCTION = "table_len";
    private final String COOKIE_LEN_FUNCTION = "cookie_len";

    public RequestInterceptRuleEngine() {
        registerStage(RuleStages.STAGE_REQUEST_INTERCEPTION, 0);
        registerStage(RuleStages.STAGE_LOCATION_LUA_REWRITE, -100);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return collectedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();

        ConditionRuleAttribute conditionRuleAttribute = ObjectJsonParser.parse(attributes, ConditionRuleAttribute.class);
        ValidateUtils.notNullAndEmpty(conditionRuleAttribute, "[[RuleEngine=" + ENGINE_NAME + "]]Rule attribute shall not be null");

        Condition condition = conditionRuleAttribute.getCondition();
        ConditionAction conditionAction = conditionRuleAttribute.getConditionAction();
        ValidateUtils.notNullAndEmpty(conditionAction, "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition action shall not be null");
        validateCondition(condition);
        validateAction(conditionAction);
    }

    @Override
    public String getType() {
        return RuleType.REQUEST_INTERCEPT_RULE.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        ConfWriter confWriter = new ConfWriter();
        generate(rules, confWriter, stage);
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        if (RuleStages.STAGE_REQUEST_INTERCEPTION.equalsIgnoreCase(stage)) {
            buildPreCondition(confWriter, rules);
        } else {
            buildActionInLua(confWriter, rules);
        }
    }

    private void buildActionInLua(ConfWriter confWriter, List<Rule> rules) {
        confWriter.startLuaZone();
        confWriter.writeLine("local actionFlag = false");
        for (Rule rule : rules) {
            ConditionRuleAttribute conditionRuleAttribute = ObjectJsonParser.parse(rule.getAttributes(), ConditionRuleAttribute.class);
            if (conditionRuleAttribute == null || conditionRuleAttribute.getCondition() == null
                    || conditionRuleAttribute.getConditionAction() == null) {
                continue;
            }
            Condition condition = conditionRuleAttribute.getCondition();
            ConditionAction action = conditionRuleAttribute.getConditionAction();
            if (action == null || action.getType() == null) {
                continue;
            }
            List<Condition> conditions = getPreConditions(condition);
            Collections.sort(conditions, new Comparator<Condition>() {
                @Override
                public int compare(Condition o1, Condition o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            for (int i = 0; i < conditions.size(); i++) {
                Condition c = conditions.get(i);
                c.setAlias(getTmpVarName(rule, i));
            }
            String command = getConditionCommand(condition);

//            if (command.contains(TABLE_LEN_FUNCTION)) {
//                writeTableLenFunction(confWriter);
//            }
//            if (command.contains(COOKIE_LEN_FUNCTION)) {
//                writeCookieLenFunction(confWriter);
//            }

            confWriter.writeLine("if ( actionFlag == false  and " + command + " ) then");
            confWriter.writeLine("actionFlag = true");
            confWriter.writeLine("ngx.var." + LogFormat.INTERCEPT_STATUS.replace("$", "") + " = 1");
            if (ConditionActionType.FLAG.getName().equalsIgnoreCase(action.getType())) {
                for (ConditionHeader header : action.getHeaders()) {
                    confWriter.writeLine("ngx.req.set_header(\"" + header.getKey() + "\",\"" + header.getValue() + "\")");
                }
                confWriter.writeLine("end");
            } else if (ConditionActionType.REJECT.getName().equalsIgnoreCase(action.getType())) {
                confWriter.writeLine("ngx.header.content_type = \"text/plain\"");
                confWriter.writeLine("ngx.status = " + action.getCode());
                confWriter.writeLine("ngx.say(" + parserValue(action.getMessage(), false) + ")");
                confWriter.writeLine("return ngx.exit(" + action.getCode() + ")");
                confWriter.writeLine("end");
            } else if (ConditionActionType.REDIRECT.getName().equalsIgnoreCase(action.getType())) {
                confWriter.writeLine(" return ngx.redirect(\"" + action.getTarget() + "\"," + action.getCode() + ")");
                confWriter.writeLine("end");
            } else if (ConditionActionType.PROXYPASS.getName().equalsIgnoreCase(action.getType())) {
                String target = action.getTarget();
                if (target.startsWith("http://")) {
                    confWriter.writeLine("ngx.var.upstream = \"" + target.replaceFirst("http://", "") + "\"");
                    confWriter.writeLine("ngx.var.upstream_scheme = \"http\"");
                    confWriter.writeLine("end");
                } else if (target.startsWith("https://")) {
                    confWriter.writeLine("ngx.var.upstream = \"" + target.replaceFirst("https://", "") + "\"");
                    confWriter.writeLine("ngx.var.upstream_scheme = \"https\"");
                    confWriter.writeLine("end");
                }
            }
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
            case CIP:
                switch (luaFunction) {
                    case LIKE:
                        return " ngx.re.match(" + getCipLuaVar() + ", [=[" + value.replaceAll("\\\\", "\\\\\\\\") + "]=]) ";
                    case NOTLIKE:
                        return " nil == ngx.re.match(" + getCipLuaVar() + ", [=[" + value.replaceAll("\\\\", "\\\\\\\\") + "]=]) ";
                    case LIKEIGNORECASE:
                        return " ngx.re.match(" + getCipLuaVar() + ", [=[" + value.replaceAll("\\\\", "\\\\\\\\") + "]=] , \"i\") ";
                    case NOTLIKEIGNORECASE:
                        return " nil == ngx.re.match(" + getCipLuaVar() + ", [=[" + value.replaceAll("\\\\", "\\\\\\\\") + "]=] , \"i\") ";
                    default:
                        return getCipLuaVar() + " " + luaFunction.getLuaFunc() + parserValueInLua(value, false);
                }
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
            case COUNTCOOKIE:
                return COOKIE_LEN_FUNCTION + "() " + luaFunction.getLuaFunc() + " " + value;
            case COUNTHEADER:
                return TABLE_LEN_FUNCTION + "(ngx.req.get_headers()) " + luaFunction.getLuaFunc() + " " + value;
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
            case CIP:
                return getCipNginxVar() + " " + conditionFunction.getNginxFunc() + parserValue(value, false);
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


    private void buildPreCondition(ConfWriter confWriter, List<Rule> rules) {
        confWriter.writeCommand("set", LogFormat.INTERCEPT_STATUS + " -");
        for (Rule rule : rules) {
            ConditionRuleAttribute conditionRuleAttribute = ObjectJsonParser.parse(rule.getAttributes(), ConditionRuleAttribute.class);
            if (conditionRuleAttribute == null || conditionRuleAttribute.getCondition() == null
                    || conditionRuleAttribute.getConditionAction() == null) {
                continue;
            }
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
        ValidateUtils.notNullAndEmpty(condition, "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition shall not be null");

        // condition type field validation
        ConditionType type = ConditionType.getType(condition.getType());
        ValidateUtils.notNullAndEmpty(type, "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition type is not allowed");
        if (type == ConditionType.SELF) {
            ConditionTarget target = ConditionTarget.parserFromTarget(condition.getTarget());
            ValidateUtils.notNullAndEmpty(target, "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition target " + condition.getTarget() + " is not allowed");
            ValidateUtils.notNullAndEmpty(target.getType(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition target " + condition.getTarget() + " is not allowed");
            switch (target.getType()) {
                case HEADER:
                case COOKIE:
                case ARG:
                    ValidateUtils.notNullAndEmpty(target.getKey(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition target " + condition.getTarget() + " with empty key is not allowed");
                    if (target.getKey().length() > 64) {
                        throw new ValidationException("[[RuleEngine=" + ENGINE_NAME + "]]Rule condition target " + condition.getTarget() + " with too long key name is not allowed");
                    }
                    Pattern pattern = Pattern.compile("^(-|\\w|\\.)+$");
                    Matcher matcher = pattern.matcher(target.getKey());
                    if (!matcher.find()) {
                        throw new ValidationException("Only [A-Za-z0-9_] is allowed for target key.");
                    }

                    break;
                case COUNTCOOKIE:
                case COUNTHEADER:
                    ValidateUtils.isIntValue(condition.getValue(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition target " + condition.getTarget() + " should get int value.");
                case CIP:
                    //DO Nothing.
            }


            String fn = condition.getFunction();
            ConditionFunction func = ConditionFunction.getFunction(fn);
            ValidateUtils.notNullAndEmpty(func, "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition Function " + fn + " is not allowed");
            ValidateUtils.notNullAndEmpty(condition.getValue(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition value " + condition.getValue() + " is not allowed");
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
            ValidateUtils.notNullAndEmpty(conditions, "[[RuleEngine=" + ENGINE_NAME + "]]Rule condition composite shall not be empty");
            for (Condition c : conditions) {
                validateCondition(c);
            }
        }
    }

    private void validateAction(ConditionAction action) throws ValidationException {
        ValidateUtils.notNullAndEmpty(action, "[[RuleEngine=" + ENGINE_NAME + "]]Rule action shall not be null");
        ValidateUtils.notNullAndEmpty(action.getType(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule action type shall not be null");

        if (ConditionActionType.FLAG.getName().equalsIgnoreCase(action.getType())) {
            ValidateUtils.notNullAndEmpty(action.getHeaders(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule action type shall not be null");
            if (action.getHeaders().size() == 0) {
                throw new ValidationException("[[RuleEngine=" + ENGINE_NAME + "]]Rule action headers shall not be null");
            }
            for (ConditionHeader header : action.getHeaders()) {
                ValidateUtils.notNullAndEmpty(header.getKey(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule header key shall not be null");
                ValidateUtils.notNullAndEmpty(header.getValue(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule header value shall not be null");
            }
        } else if (ConditionActionType.REJECT.getName().equalsIgnoreCase(action.getType())) {
            ValidateUtils.notNullAndEmpty(action.getCode(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule action code shall not be null");
            ValidateUtils.notNullAndEmpty(action.getMessage(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule action message shall not be null");
        } else if (ConditionActionType.REDIRECT.getName().equalsIgnoreCase(action.getType())) {
            ValidateUtils.notNullAndEmpty(action.getCode(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule action code shall not be null");
            ValidateUtils.notNullAndEmpty(action.getTarget(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule action target shall not be null");
        } else if (ConditionActionType.PROXYPASS.getName().equalsIgnoreCase(action.getType())) {
            ValidateUtils.notNullAndEmpty(action.getTarget(), "[[RuleEngine=" + ENGINE_NAME + "]]Rule action target shall not be null");
            if (!action.getTarget().startsWith("http://") && !action.getTarget().startsWith("https://")) {
                throw new ValidationException("[[RuleEngine=" + ENGINE_NAME + "]]Rule action target shall start with http:// or https://");
            }
        }
    }

    private String getCipNginxVar() {
        return "$" + LocationConf.TRUE_CLIENT_IP_VAR;
    }

    private String getCipLuaVar() {
        return "ngx.var." + LocationConf.TRUE_CLIENT_IP_VAR;
    }
}
