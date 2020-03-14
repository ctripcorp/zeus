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
import com.ctrip.zeus.service.rule.util.ValidateUtils;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service("groupSocketIORuleEngine")
public class GroupSocketIORuleEngine extends AbstractRuleEngine {

    private final String ENGINE_NAME = this.getClass().getSimpleName();

    @Resource
    private MergeStrategy inheritedMergeStrategy;


    public GroupSocketIORuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_ENABLE_SOCKET_IO, 1);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    public String getType() {
        return RuleType.SOCKET_IO_ENABLED.getName();
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    public void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        String enabledKey = RuleAttributeKeys.ENABLED_KEY;
        if (ruleAttribute == null) {
            throw new ValidationException("[Group Error Page Rule Engine][Validate]Rule attributes shall not be null");
        }

        Object enable = ruleAttribute.get(enabledKey);
        if (enable == null)
            throw new ValidationException("[Group Error Page Rule Engine][Validate]Rule attribute does not contain key: " + enabledKey);
        ValidateUtils.isBooleanValue(enable, "[Group Error Page Rule Engine][Validate]Rule attribute " + enabledKey + "should be boolean.");
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        ConfWriter confWriter = new ConfWriter();
        generate(rules, confWriter, stage);
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        if (rules == null || rules.size() == 0) {
            return;
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=" + ENGINE_NAME + "]]" + ENGINE_NAME + " Rule Can't Use Multi Rules.");
        }
        Rule rule = rules.get(0);
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });

        Object enabledObj = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        boolean isEnabled = enabledObj == null || Boolean.parseBoolean(enabledObj.toString());
        if (isEnabled) {
            confWriter.writeCommand("proxy_set_header", "Upgrade $http_upgrade");
            confWriter.writeCommand("set" , " $conn_value \"upgrade\"");
            confWriter.writeCommand("proxy_http_version", "1.1");
        }
    }
}
