package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.build.conf.LocationConf;
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
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service("errorPageRuleEngine")
public class ErrorPageRuleEngine extends AbstractRuleEngine {

    @Resource
    private ConfigHandler configHandler;

    @Autowired
    private LocationConf locationConf;

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    public ErrorPageRuleEngine() {
        // Child rules escape
        String childClass = DefaultErrorPageRuleEngine.class.getSimpleName();
        if (this.getClass().getSimpleName().equalsIgnoreCase(childClass)) return;

        registerStage(RuleStages.STAGE_SERVER_ERROR_PAGE, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();
        ValidateUtils.notNullAndEmpty(attributes, "[[RuleEngine=ErrorPageRuleEngine]]Rule attributes shall not be null");
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=ErrorPageRuleEngine]]Rule attributes shall not be null");
        Object errorPageEnabled = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        ValidateUtils.notNullAndEmpty(errorPageEnabled, "[[RuleEngine=ErrorPageRuleEngine]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall not be null");
        ValidateUtils.isBooleanValue(errorPageEnabled, "[[RuleEngine=ErrorPageRuleEngine]][Validate]Rule attribute key: " + RuleAttributeKeys.ENABLED_KEY + " shall be in Boolean format");
    }

    @Override
    public String getType() {
        return RuleType.ERROR_PAGE.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=ErrorPageRuleEngine]]ErrorPageRuleEngine Rule Can't Use Multi Rules.");
        }

        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(rules.get(0).getAttributes(), new TypeReference<HashMap<String, Object>>() {
        });
        Boolean errorPageEnabled = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY));
        if (errorPageEnabled == null || !errorPageEnabled) {
            return null;
        }

        String errorPageAccept = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.ERROR_PAGE_ACCEPT_KEY));
        String errorHostUrl = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.ERROR_HOST_URL_KEY));
        try {
            if (Strings.isNullOrEmpty(errorPageAccept)) {
                errorPageAccept = configHandler.getStringValue("location.errorPage.accept", null, null, null, "text/html");
            }
            if (Strings.isNullOrEmpty(errorHostUrl)) {
                errorHostUrl = configHandler.getStringValue("location.errorPage.host.url", null, null, null, null);
            }
        } catch (Exception ex) {
            throw new ValidationException("[[RuleEngine=ErrorPageRuleEngine]] Validate Failed for getting default values, message: " + ex.getMessage());
        }

        if (Strings.isNullOrEmpty(errorHostUrl)) return null;

        ConfWriter confWriter = new ConfWriter(16, true);

        String path = configHandler.getStringValue("location.errorPage.path", "/ctrip_error_page");
        for (int sc = 400; sc <= 425; sc++) {
            confWriter.writeCommand("error_page", sc + " " + path + "?code=" + sc);
        }
        for (int sc = 500; sc <= 510; sc++) {
            confWriter.writeCommand("error_page", sc + " " + path + "?code=" + sc);
        }
        confWriter.writeLocationStart("= " + path);
        confWriter.writeLine("internal;");
        confWriter.writeCommand("proxy_set_header Accept", errorPageAccept);
        confWriter.writeCommand("rewrite_by_lua", locationConf.getErrLuaScripts());
        confWriter.writeCommand("rewrite", "\"" + path + "\" \"/errorpage/$arg_code\" break");
        confWriter.writeCommand("proxy_pass", errorHostUrl);
        confWriter.writeLocationEnd();

        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        String line = generate(rules, stage);
        if (!Strings.isNullOrEmpty(line)) {
            confWriter.writeLine(line);
        }
    }
}
