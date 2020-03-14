package com.ctrip.zeus.service.rule.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.build.conf.LogFormat;
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
import java.util.HashMap;
import java.util.List;

@Service("FaviconRuleEngine")
public class FaviconRuleEngine extends AbstractRuleEngine {
    @Resource
    private MergeStrategy inheritedMergeStrategy;

    @Resource
    private ConfigHandler configHandler;

    public FaviconRuleEngine() {
        registerStage(RuleStages.STAGE_FAVOR_ICON, -100);
        registerEffectTargetTypes(RuleTargetType.SLB.getName());
        registerEffectTargetTypes(RuleTargetType.GROUP.getName());
        registerEffectTargetTypes(RuleTargetType.VS.getName());
    }

    @Override
    protected MergeStrategy getMergeStrategy() {
        return inheritedMergeStrategy;
    }

    @Override
    protected void doValidate(Rule rule) throws ValidationException {
        String attributes = rule.getAttributes();
        ValidateUtils.notNullAndEmpty(attributes, "[[RuleEngine=FaviconRuleEngine]]Rule attributes shall not be null");
        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
        ValidateUtils.notNullAndEmpty(ruleAttribute, "[[RuleEngine=FaviconRuleEngine]]Rule attributes shall not be null");
        Object faviconEnabled = ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY);
        ValidateUtils.notNullAndEmpty(faviconEnabled, "[[RuleEngine=FaviconRuleEngine]]Rule " + RuleAttributeKeys.ENABLED_KEY + " shall not be null");
        ValidateUtils.isBooleanValue(faviconEnabled, "[[RuleEngine=FaviconRuleEngine]][Validate]Rule attribute key: " + RuleAttributeKeys.ENABLED_KEY + " shall be in Boolean format");
    }

    @Override
    public String getType() {
        return RuleType.FAVICON_RULE.getName();
    }

    @Override
    public String generate(List<Rule> rules, String stage) throws ValidationException {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        if (rules.size() != 1) {
            throw new RuntimeException("[[RuleEngine=FaviconRuleEngine]]FaviconRuleEngine Rule Can't Use Multi Rules.");
        }

        HashMap<String, Object> ruleAttribute = ObjectJsonParser.parse(rules.get(0).getAttributes(), new TypeReference<HashMap<String, Object>>() {
        });
        Boolean favivonEnabled = ParserUtils.booleanValue(ruleAttribute.get(RuleAttributeKeys.ENABLED_KEY));
        if (favivonEnabled == null || !favivonEnabled) {
            return null;
        }

        String faviconBase64Text = ParserUtils.stringValue(ruleAttribute.get(RuleAttributeKeys.FAVICON_BASE64_CODE));
        try {
            if (Strings.isNullOrEmpty(faviconBase64Text)) {
                faviconBase64Text = configHandler.getStringValue("location.vs.favicon.base64", null, null, null,
                        "AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAON3" +
                                "JXvjdyXX5Xcj9+h2IP/pdh7/6HYf/+Z3IqvkdyQqAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA43clNON3Jf/jdy" +
                                "X/5Xcjb+p2HtbydRX/soBZ//Z0Ef/vdRj/6HYg/+R3JKoAAAAAAAAAAAAAAAAAAAAA43clM+N3Jf/jdyX/5HckSud" +
                                "2Ic7YeTG9MJXhKgCe/3oHm///l4R1//F1Fv/odiD/5HckhQAAAAAAAAAAAAAAAON3Jf/jdyX/43cl3OR3JALodiDcAAA" +
                                "AAAAAAAAAAAAABZv/FwCd//+4f1P/7XUb/+V3I/8AAAAAAAAAAON3JYPjdyX/43cl/+N3JXbjdyUJ53YhfQAAAAAAAAAAAAA" +
                                "AAAAAAAAEm/+hWo20//R0E//odiD/AAAAAAAAAADjdyXj43cl/+N3Jf/jdyV3AAAAAOV3I2wAAAAAAAAAAAAAAAAAAAAACZv/" +
                                "gDCV4f/+cgn/7HYb/wAAAAAAAAAA43cl/+N3Jf/jdyX/43cl2wAAAADkdyRRAAAAAAAAAAAAAAAAAAAAAAqa/7QEm///HJj15/" +
                                "N1FA0AAAAAAAAAAON3Jf/jdyX/43cl/+N3Jf/jdyVN43clAeR3JP3pdh9LAAAAAAAAAAAFm/8BB5v/fACe/xMAAAAAAAAAAAAAAADjdyX" +
                                "/43cl/+N3Jf/jdyX/43cl/eN3JVjjdyXm5Xcj/+x1G10AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA43cl/+N3Jf/jdyX/43cl/" +
                                "+N3Jf/jdyX/43cl/+R3JP/mdiLuAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAON3JX/jdyX/43cl/+N3Jf/jdyX/43cl/+N3Jf/j" +
                                "dyX/5Hck/+Z2IVAAAAAAAAAAAAAAAAAAAAAAAAAAAON3JTrjdyVh43cl/+N3Jf/jdyX/43cl/+N3Jf/jdyX/43Yk/+N2I//jdiP/5XYi/+" +
                                "Z2IafmdiFKAAAAAAAAAADjdyXu43cl/+N3Jf/jdyX/43cl/+N3Jf/jdyX/43Yk/+N1Iv/ullP/54E0/+N2JP/jdyT/43cl/+N3JdvjdyUMAA" +
                                "AAAON3JXXjdyWl43cl7eN3Jf/jdyX/43cl/+N2JP/jdSL/6LqX/+N1If/jdiT/43cl/+N3JdHjdyVBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA43clkeN3Jf" +
                                "/jdyT/43Yj/+N1Iv/jdiP/43ck/+N3JfUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADjdyUr43cll+N3JP/jdyT/43ck/+N3JenjdyUYAAAAAAAAAAAAAAAA+AecQeADn" +
                                "EHAAZxBwcGcQYHhnEGF4ZxBheGcQYBjnEGAP5xBgD+cQYAfnEEAA5xBAACcQYABnEH4B5xB/AecQQ==");
            }
        } catch (Exception ex) {
            throw new ValidationException("[[RuleEngine=FaviconRuleEngine]] Validate Failed for getting default values, message: " + ex.getMessage());
        }

        StringBuilder sb = new StringBuilder(512).append("'\n")
                .append("local res = ngx.decode_base64(\"").append(faviconBase64Text).append("\");\n")
                .append("ngx.print(res);\n")
                .append("return ngx.exit(200);'");

        ConfWriter confWriter = new ConfWriter(16, true);
        confWriter.writeLocationStart("@favicon");
        confWriter.writeCommand("add_header", "Accept-Ranges bytes");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeCommand("content_by_lua", sb.toString());
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
