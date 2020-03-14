package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.ConfigHandler;
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
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service("SSLConfigRuleEngine")
public class SSLConfigRuleEngine extends AbstractRuleEngine {

    @Resource
    private ConfigHandler configHandler;


    public SSLConfigRuleEngine() {
        // Child rules escape
        String childClass = DefaultSSLConfigRuleEngine.class.getSimpleName();
        if (this.getClass().getSimpleName().equalsIgnoreCase(childClass)) return;

        registerStage(RuleStages.STAGE_SERVER_SSL_CONFIG, 0);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    private final String ENGINE_NAME = this.getClass().getSimpleName();
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

        Object preferServer = ruleAttribute.get(RuleAttributeKeys.SSL_PREFER_SERVER_CIPHERS);
        Object ciphers = ruleAttribute.get(RuleAttributeKeys.SSL_CIPHERS);
        Object bufferSize = ruleAttribute.get(RuleAttributeKeys.SSL_BUFFER_SIZE);
        Object protocol = ruleAttribute.get(RuleAttributeKeys.SSL_PROTOCOL);
        ValidateUtils.notNullAndEmpty(preferServer, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.SSL_PREFER_SERVER_CIPHERS + " shall not be null");
        ValidateUtils.notNullAndEmpty(ciphers, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.SSL_CIPHERS + " shall not be null");
        ValidateUtils.notNullAndEmpty(bufferSize, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.SSL_BUFFER_SIZE + " shall not be null");
        ValidateUtils.notNullAndEmpty(protocol, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.SSL_PROTOCOL + " shall not be null");
        ValidateUtils.isBooleanValue(preferServer, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.SSL_PREFER_SERVER_CIPHERS + " shall be int value");
        String buffer = bufferSize.toString();
        ValidateUtils.isIntValue(buffer, "[[RuleEngine=" + ENGINE_NAME + "]]Rule " + RuleAttributeKeys.SSL_BUFFER_SIZE + " is invalidate");

        String[] protocols = protocol.toString().split(" ");
        String[] allowProtocols = configHandler.getStringValue("ssl.protocol.supported.list", "TLSv1 TLSv1.1 TLSv1.2 TLSv1.3").split(" ");
        List<String> allow = Arrays.asList(allowProtocols);
        for (String p : protocols) {
            if (p == null || p.isEmpty()) {
                continue;
            }
            if (!allow.contains(p)) {
                throw new ValidationException("Protocol Config Invalidate. " + p + " Is Not Allowed.");
            }
        }

    }

    @Override
    public String getType() {
        return RuleType.SSL_CONFIG.name();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=" + ENGINE_NAME + "]]" + ENGINE_NAME + " Rule Can't Use Multi Rules.");
        }

        Rule rule = rules.get(0);
        String attributes = rule.getAttributes();
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        if (ruleAttribute == null) {
            throw new ValidationException("Cant Find Rule Attribute.");
        }
        Boolean preServer = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.SSL_PREFER_SERVER_CIPHERS));
        String cipher = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.SSL_CIPHERS));
        String ecdh = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.SSL_ECDH_CURVE));
        String buffer = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.SSL_BUFFER_SIZE));
        String protocol = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.SSL_PROTOCOL));
        ConfWriter confWriter = new ConfWriter();
        if (preServer != null && preServer) {
            confWriter.writeCommand("ssl_prefer_server_ciphers", "on");
        } else {
            confWriter.writeCommand("ssl_prefer_server_ciphers", "off");
        }

        if (!Strings.isNullOrEmpty(cipher)) {
            confWriter.writeCommand("ssl_ciphers", cipher);
        }
        if (!Strings.isNullOrEmpty(protocol)) {
            confWriter.writeCommand("ssl_protocols", protocol);
        }
        if (!Strings.isNullOrEmpty(ecdh)) {
            confWriter.writeCommand("ssl_ecdh_curve", ecdh);
        }
        if (!Strings.isNullOrEmpty(buffer)) {
            confWriter.writeCommand("ssl_buffer_size", buffer + "k");
        }
        return confWriter.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }
}
