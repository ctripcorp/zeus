package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.build.conf.LogFormat;
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
@Service("defaultDownloadImageRuleEngine")
public class DefaultDownloadImageRuleEngine extends AbstractRuleEngine {

    @Resource
    private InheritedMergeStrategy inheritedMergeStrategy;

    public DefaultDownloadImageRuleEngine() {
        registerStage(RuleStages.STAGE_SERVER_BOTTOM, -200);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        HashMap<String, Object> attributes = getAttributes(rule);
        if (attributes == null) {
            throw new ValidationException("[" + this.getClass().getSimpleName() + "][Validate]Rule attributes shall not be null");
        }

        validateAttribute(attributes, RuleAttributeKeys.ENABLED_KEY, "Boolean");
        validateAttribute(attributes, RuleAttributeKeys.DEFAULT_DOWNLOAD_IMAGE_ROOT, "String");
    }

    @Override
    public String getType() {
        return RuleType.DEFAULT_DOWNLOAD_IMAGE.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() > 1) {
            throw new RuntimeException("[[RuleEngine= " + this.getClass().getSimpleName() + "]] Can't Use Multi Rules.");
        }
        Rule rule = rules.get(0);
        if (rule != null) {
            HashMap<String, Object> attributes = getAttributes(rule);
            if (attributes != null) {
                boolean enabled = Boolean.parseBoolean((String) attributes.get(RuleAttributeKeys.ENABLED_KEY));
                String root_path = (String) attributes.get(RuleAttributeKeys.DEFAULT_DOWNLOAD_IMAGE_ROOT);
                if (enabled) {
                    ConfWriter confWriter = new ConfWriter();
                    confWriter.writeLocationStart("= /slb_download_test_image.png");
                    confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
                    confWriter.writeCommand("root", root_path);
                    confWriter.writeLocationEnd();
                    return confWriter.toString();
                }
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
