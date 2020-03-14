package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.AbstractRuleEngine;
import com.ctrip.zeus.service.rule.MergeStrategy;
import com.ctrip.zeus.service.rule.merges.InheritedMergeStrategy;
import com.ctrip.zeus.service.rule.model.RuleAttributeKeys;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.rule.model.RuleType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @Discription
 **/
@Service("pageIdRuleEngine")
public class PageIdRuleEngine extends AbstractRuleEngine {

    private static final String LUA_SCRIPT = "\n local tmpPageId = ngx.req.get_headers()[\"x-ctrip-pageid\"];\n" +
            "if tmpPageId ~= nil and tmpPageId ~= \"\" then\n" +
            "ngx.var.x_ctrip_pageid = tmpPageId;\n" +
            "end\n";

    @Resource
    private InheritedMergeStrategy inheritedMergeStrategy;

    public PageIdRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_LUA_ACCESS, -30);
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
        if (attributes == null) {
            throw new ValidationException("");
        }
        validateAttribute(attributes, RuleAttributeKeys.ENABLED_KEY, "Boolean");
    }

    @Override
    public String getType() {
        return RuleType.PAGE_ID.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() > 1) {
            throw new RuntimeException("");
        }
        Rule rule = rules.get(0);
        HashMap<String, Object> attributes = getAttributes(rule);
        if (attributes != null) {
            boolean enabled = Boolean.parseBoolean((String) attributes.get(RuleAttributeKeys.ENABLED_KEY));
            if (enabled) {
                ConfWriter confWriter = new ConfWriter();
                confWriter.startLuaZone();
                confWriter.writeLine(LUA_SCRIPT);
                confWriter.endLuaZone();
                return confWriter.toString();
            }
        }
        return "";
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        String line = generate(rules, stage);
        if (line != null && line.length() > 0) {
            confWriter.writeLine(line);
        }
    }
}