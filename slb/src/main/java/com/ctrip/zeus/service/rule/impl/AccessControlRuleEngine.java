package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.AbstractRuleEngine;
import com.ctrip.zeus.service.rule.MergeStrategy;
import com.ctrip.zeus.service.rule.merges.InheritedMergeStrategy;
import com.ctrip.zeus.service.rule.model.RuleAttributeKeys;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.rule.model.RuleType;
import com.ctrip.zeus.service.rule.util.ParserUtils;
import com.ctrip.zeus.tag.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

;

/**
 * @Discription
 **/
@Service("accessControlRuleEngine")
public class AccessControlRuleEngine extends AbstractRuleEngine {

    @Resource
    private InheritedMergeStrategy inheritedMergeStrategy;

    @Resource
    private PropertyService propertyService;

    private Logger logger = LoggerFactory.getLogger(AccessControlRuleEngine.class);

    private static final String ACCESS_CONTROL_PROPERTY_KEY = "accessControl";
    private static final String ACCESS_CONTROL_LIST_SPLIT_PATTERN = ";";

    public AccessControlRuleEngine() {
        registerStage(RuleStages.STAGE_LOCATION_LUA_ACCESS, 10);
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
            throw new ValidationException("[[RuleEngine=AccessControlRuleEngine]]Rule attributes shall not be null or empty");
        }
        validateAttribute(attributes, RuleAttributeKeys.ACCESS_CONTROL_GROUP_ID, "long");
        Long groupId = ParserUtils.longValue(attributes.get(RuleAttributeKeys.ACCESS_CONTROL_GROUP_ID));

        if (checkAccessControlProperty(groupId)) {
            validateAttribute(attributes, RuleAttributeKeys.ACCESS_CONTROL_WHITE_LIST_ENABLED, "Boolean");
            Boolean whileListEnabled = Boolean.valueOf((String) attributes.get(RuleAttributeKeys.ACCESS_CONTROL_WHITE_LIST_ENABLED));
            if (whileListEnabled) {
                validateAttribute(attributes, RuleAttributeKeys.ACCESS_CONTROL_ALLOW_LIST, "string");
            } else {
                validateAttribute(attributes, RuleAttributeKeys.ACCESS_CONTROL_DENY_LIST, "string");
            }
        }
    }

    private boolean checkAccessControlProperty(Long groupId) {
        // Check whether accessControl is enabled
        // Before calling method, groupId has been validated
        try {
            Property property = propertyService.getProperty(ACCESS_CONTROL_PROPERTY_KEY, groupId, "group");
            if (property != null) {
                String accessType = property.getValue();
                return "true".equalsIgnoreCase(accessType);
            }
        } catch (Exception e) {
            logger.error("Get Property of group failed. GroupId:" + groupId + ";Pname:accessType");
        }
        return false;
    }

    @Override
    public String getType() {
        return RuleType.ACCESS_CONTROL.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules != null && rules.size() > 0) {
            if (rules.size() > 1) {
                throw new RuntimeException("[[RuleEngine=AccessControlRuleEngine]]AccessControlRuleEngine Can't Use Multi Rules.");
            }

            Rule rule = rules.get(0);
            HashMap<String, Object> attributes = getAttributes(rule);
            Long groupId = ParserUtils.longValue(attributes.get(RuleAttributeKeys.ACCESS_CONTROL_GROUP_ID));
            if (!checkAccessControlProperty(groupId)) {
                return "";
            }

            ConfWriter confWriter = new ConfWriter();
            Boolean whileListEnabled = Boolean.valueOf((String) attributes.get(RuleAttributeKeys.ACCESS_CONTROL_WHITE_LIST_ENABLED));
            if (whileListEnabled) {
                String allowList = (String) attributes.get(RuleAttributeKeys.ACCESS_CONTROL_ALLOW_LIST);
                String[] allowItems = allowList.split(ACCESS_CONTROL_LIST_SPLIT_PATTERN);
                for (String allowItem : allowItems) {
                    confWriter.writeCommand("allow", allowItem);
                }
                confWriter.writeCommand("deny", "all");
            } else {
                String denyList = (String) attributes.get(RuleAttributeKeys.ACCESS_CONTROL_DENY_LIST);
                String[] denyItems = denyList.split(ACCESS_CONTROL_LIST_SPLIT_PATTERN);
                for (String denyItem : denyItems) {
                    confWriter.writeCommand("deny", denyItem);
                }
            }
            return confWriter.getValue();
        }
        return "";
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        String content = generate(rules, stage);
        if (content != null && !content.isEmpty()) {
            confWriter.writeLine(content);
        }
    }
}
