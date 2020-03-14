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
import com.ctrip.zeus.service.rule.util.ParserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @Discription
 **/
@Service("serverNameHashRuleEngine")
public class ServerNameHashRuleEngine extends AbstractRuleEngine {

    @Resource
    private InheritedMergeStrategy inheritedMergeStrategy;

    public ServerNameHashRuleEngine() {
        registerStage(RuleStages.STAGE_NGINX_CONF, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        HashMap<String, Object> attributes = getAttributes(rule);
        if (attributes == null) {
            throw new ValidationException("[[RuleEngine=ServerNameHashRuleEngine]]Rule attributes shall not be null or empty");
        }
        validateAttribute(attributes, RuleAttributeKeys.SERVER_NAME_HASH_MAX_SIZE, "integer");
        validateAttribute(attributes, RuleAttributeKeys.SERVER_NAME_HASH_BUCKET_SIZE, "integer");
    }

    @Override
    public String getType() {
        return RuleType.SERVER_NAME_HASH.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules != null && rules.size() > 0) {
            if (rules.size() > 1) {
                throw new RuntimeException("[[RuleEngine=ServerNameHashRuleEngine]]ServerNameHashRuleEngine Can't Use Multi Rules.");
            }
            Rule rule = rules.get(0);
            HashMap<String, Object> attributes = getAttributes(rule);
            Integer serverNameMaxSize = ParserUtils.intValue(attributes.get(RuleAttributeKeys.SERVER_NAME_HASH_MAX_SIZE));
            Integer serverNameBucketSize = ParserUtils.intValue(attributes.get(RuleAttributeKeys.SERVER_NAME_HASH_BUCKET_SIZE));

            ConfWriter confWriter = new ConfWriter();

            confWriter.writeCommand("server_names_hash_max_size", String.valueOf(serverNameMaxSize));
            confWriter.writeCommand("server_names_hash_bucket_size", String.valueOf(serverNameBucketSize));
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
