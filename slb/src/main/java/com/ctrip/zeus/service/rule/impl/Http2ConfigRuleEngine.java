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

@Service("http2ConfigRuleEngine")
public class Http2ConfigRuleEngine extends AbstractRuleEngine {
    private final String ENGINE_NAME = this.getClass().getSimpleName();

    @Resource
    private MergeStrategy inheritedMergeStrategy;

    public Http2ConfigRuleEngine() {
        registerStage(RuleStages.STAGE_SERVER_HTTP2_CONFIG, 1);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    public String getType() {
        return RuleType.SERVER_HTTP2_CONFIG_RULE.getName();
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

        if (ruleAttribute == null) {
            throw new ValidationException("[HTTP2 Config Rule Engine][Validate]Rule attributes shall not be null");
        }

        Object enableSetting = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        if (enableSetting == null) {
            throw new ValidationException("[HTTP2 Config Rule Engine][Validate] Rule enable attribute is required");
        } else {
            ValidateUtils.isBooleanValue(enableSetting, "[HTTP2 Config Rule Engine][Validate] Rule enable attribute shall be boolean value required");
        }

        validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_BODY_PREREAD_SIZE, "[HTTP2 Config Rule Engine][Validate] Rule Rule PreReadSize value shall be int");

        validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_CHUNK_SIZE, "[HTTP2 Config Rule Engine][Validate] Rule Chunk Size value shall be int");

        validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_MAX_FIELD_SIZE, "[HTTP2 Config Rule Engine][Validate] Rule Max Field Size value shall be int");

        validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_MAX_HEADER_SIZE, "[HTTP2 Config Rule Engine][Validate] Rule Max Header Size value shall be int");

        //validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_RECV_BUFFER_SIZE, "[HTTP2 Config Rule Engine][Validate] Rule Receive Buffer Size value shall be int");

        validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_IDLE_TIMEOUT, "[HTTP2 Config Rule Engine][Validate] Rule Idle timeout value shall be int");

        validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_RECV_TIMEOUT, "[HTTP2 Config Rule Engine][Validate] Rule Receive timeout value shall be int");

        validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_MAX_CONCURRENT_STREAMS, "[HTTP2 Config Rule Engine][Validate] Rule Max Concurrent Streams value shall be int");

        validateHttp2Size(ruleAttribute, RuleAttributeKeys.HTTP2_MAX_REQUESTS, "[HTTP2 Config Rule Engine][Validate] Rule Max Requests value shall be int");
    }

    @Override
    public String generate(List<Rule> rules, String stage) {
        if (rules == null || rules.size() == 0) {
            return "";
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=" + ENGINE_NAME + "]]" + ENGINE_NAME + " Rule Can't Use Multi Rules.");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Rule rule : rules) {
            String attributes = rule.getAttributes();
            HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
            });

            String enabled = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY).toString();
            if (!Boolean.parseBoolean(enabled)) {
                continue;
            }

            StringBuilder chunkSizeSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_CHUNK_SIZE)!=null){
                chunkSizeSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_CHUNK_SIZE).toString());
                chunkSizeSb.append("k");
            }


            StringBuilder bodyPreReadSizeSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_BODY_PREREAD_SIZE)!=null){
                bodyPreReadSizeSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_BODY_PREREAD_SIZE).toString());
                bodyPreReadSizeSb.append("k");
            }


            StringBuilder maxFieldSizeSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_MAX_FIELD_SIZE)!=null){
                maxFieldSizeSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_MAX_FIELD_SIZE).toString());
                maxFieldSizeSb.append("k");
            }

            StringBuilder maxHeaderSizeSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_MAX_HEADER_SIZE)!=null){
                maxHeaderSizeSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_MAX_HEADER_SIZE).toString());
                maxHeaderSizeSb.append("k");
            }


            StringBuilder receiveBufferSizeSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_RECV_BUFFER_SIZE)!=null){
                receiveBufferSizeSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_RECV_BUFFER_SIZE).toString());
                receiveBufferSizeSb.append("k");
            }

            StringBuilder idleTimeOutSizeSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_IDLE_TIMEOUT)!=null){
                idleTimeOutSizeSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_IDLE_TIMEOUT).toString());
                idleTimeOutSizeSb.append("m");
            }


            StringBuilder receiveTimeoutSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_RECV_TIMEOUT)!=null){
                receiveTimeoutSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_RECV_TIMEOUT).toString());
                receiveTimeoutSb.append("s");
            }


            StringBuilder maxConcurrentStreamsSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_MAX_CONCURRENT_STREAMS)!=null){
                maxConcurrentStreamsSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_MAX_CONCURRENT_STREAMS).toString());
            }


            StringBuilder maxRequestsSb = new StringBuilder();
            if(ruleAttribute.get(RuleAttributeKeys.HTTP2_MAX_REQUESTS)!=null){
                maxRequestsSb.append(ruleAttribute.get(RuleAttributeKeys.HTTP2_MAX_REQUESTS).toString());
            }

            writeCommand(stringBuilder, "http2_chunk_size", chunkSizeSb);
            writeCommand(stringBuilder, "http2_body_preread_size", bodyPreReadSizeSb);
            writeCommand(stringBuilder, "http2_max_field_size", maxFieldSizeSb);
            writeCommand(stringBuilder, "http2_max_header_size", maxHeaderSizeSb);

            // Not applied in server config
            //writeCommand(stringBuilder, "http2_recv_buffer_size", receiveBufferSizeSb);

            writeCommand(stringBuilder, "http2_idle_timeout", idleTimeOutSizeSb);
            writeCommand(stringBuilder, "http2_recv_timeout", receiveTimeoutSb);
            writeCommand(stringBuilder, "http2_max_concurrent_streams", maxConcurrentStreamsSb);
            writeCommand(stringBuilder, "http2_max_requests", maxRequestsSb);
        }

        return stringBuilder.toString();
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage) {
        String line = generate(rules, stage);
        confWriter.writeLine(line);
    }


    private void validateHttp2Size(HashMap<String, Object> ruleAttribute, String sizeKey, String errorMsg) throws ValidationException {
        Object size = ruleAttribute.get(sizeKey);
        if (size != null) {
            ValidateUtils.isIntValue(size, errorMsg);
        }
    }


    private void writeCommand(StringBuilder sb, String key, StringBuilder value) {
        if (key != null && value != null && value.length() > 0) {
            sb.append(key);
            sb.append(" ");
            sb.append(value.toString());
            sb.append(";");
            sb.append("\n");
        }
    }
}
