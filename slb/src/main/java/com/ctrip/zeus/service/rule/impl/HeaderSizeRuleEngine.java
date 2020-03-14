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

/**
 * @Discription
 **/
@Service("headerSizeRuleEngine")
public class HeaderSizeRuleEngine extends AbstractRuleEngine {

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    private final String LUA_SCRIPTS = "local cookie = ngx.var.http_cookie;\n" +
            "ngx.var.cookie_size = 0;\n" +
            "if cookie ~= nil then\n" +
            "         ngx.var.cookie_size = string.len( cookie);\n" +
            "end\n" +
            "local headers = ngx.req.get_headers();\n" +
            "local headersString = \"\";\n" +
            "ngx.var.header_size = 0;\n" +
            "for k,v in pairs(headers) do\n" +
            "   if type(v) ~= \"table\" then\n" +
            "       ngx.var.header_size = ngx.var.header_size + string.len(k);\n" +
            "       ngx.var.header_size = ngx.var.header_size + string.len(v);\n" +
            "       if k ~= \"cookie\" then" +
            "           headersString = headersString..k..\":\"..v..\";\";\n" +
            "       end\n" +
            "   else\n" +
            "       for i,j in pairs(v) do\n" +
            "           ngx.var.header_size = ngx.var.header_size + string.len(i);\n" +
            "           ngx.var.header_size = ngx.var.header_size + string.len(j);\n" +
            "           headersString = headersString..i..\":\"..j..\";\";\n" +
            "       end\n" +
            "   end\n" +
            "end\n";

    private final String LOG_LARGE_HEADER_LUA_SCRIPT = "local hs = ngx.var.header_size - ngx.var.cookie_size;\n"
            + " if ngx.var.header_size ~= \"-\" and hs > " + "%d" + " then\n" +
            "     ngx.var.header_value = string.gsub(headersString,\" \", \"\");\n" +
            "end\n";

    public HeaderSizeRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_LUA_ACCESS, -20);
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
        HashMap<String, Object> attributes = getAttributes(rule);
        ValidateUtils.notNullAndEmpty(attributes, "[[RuleEngine=HeaderSizeRuleEngine]]Rule attributes shall not be null or empty");
        validateAttribute(attributes, RuleAttributeKeys.ENABLED_KEY, "boolean");
        validateAttribute(attributes, RuleAttributeKeys.HEADER_SIZE_INTEGER, "integer");
        validateAttribute(attributes, RuleAttributeKeys.HEADER_SIZE_LOG_LARGE_HEADERS, "boolean");
    }

    @Override
    public String getType() {
        return RuleType.LOG_LARGE_HEADER_SIZE.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules == null || rules.isEmpty()) {
            return "";
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=HeaderSizeRuleEngine]]HeaderSizeRuleEngine Rule Can't Use Multi Rules.");
        }

        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(rules.get(0).getAttributes(), new TypeReference<HashMap<String, Object>>() {
        });
        if (ruleAttribute == null) return "--Empty Command";
        Boolean headerSizeEnabled = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY));
        Boolean logLargeEnabled = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.HEADER_SIZE_LOG_LARGE_HEADERS));
        Integer headerSize = ParserUtils.intValue(ruleAttribute.get(RuleAttributeKeys.HEADER_SIZE_INTEGER));

        if (headerSizeEnabled != null && headerSizeEnabled) {
            String result = LUA_SCRIPTS;
            if (logLargeEnabled != null &&  logLargeEnabled) {
                result += String.format(LOG_LARGE_HEADER_LUA_SCRIPT, headerSize);
            }
            ConfWriter confWriter = new ConfWriter(16, true);
            confWriter.startLuaZone();
            confWriter.writeLine(result);
            confWriter.endLuaZone();
            return confWriter.toString();
        }
        return "--Empty Command";
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        String line = generate(rules, stage);
        if (line != null && !line.isEmpty()) {
            confWriter.writeLine(line);
        }
    }
}
