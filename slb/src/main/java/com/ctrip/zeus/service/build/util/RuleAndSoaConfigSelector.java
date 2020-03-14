package com.ctrip.zeus.service.build.util;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.build.ConfigHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("ruleAndSoaConfigSelector")
public class RuleAndSoaConfigSelector {

    @Resource
    private ConfigHandler configHandler;

    public boolean workWithRule(Slb slb, VirtualServer vs, Group group, String ruleType) throws Exception {
        //Use Rule If Has Rules.
        if (configHandler.getEnable("enable.rule.on.models", true)) {
            if (ruleType == null) {
                return false;
            }
            if (group != null) {
                for (Rule rule : group.getRuleSet()) {
                    if (ruleType.equalsIgnoreCase(rule.getRuleType())) {
                        return true;
                    }
                }
            }
            if (vs != null) {
                for (Rule rule : vs.getRuleSet()) {
                    if (ruleType.equalsIgnoreCase(rule.getRuleType())) {
                        return true;
                    }
                }
            }
            if (slb != null) {
                for (Rule rule : slb.getRuleSet()) {
                    if (ruleType.equalsIgnoreCase(rule.getRuleType())) {
                        return true;
                    }
                }
            }

            // Use Default Rule In Case Of Enabled Default Rules
            if (configHandler.getEnable("enable.default.rules", slb == null ? null : slb.getId(), vs == null ? null : vs.getId(), group == null ? null : group.getId(), true)) {
                return true;
            }
            return false;
        }
        return false;
    }
    public boolean hasRuleWithEntity(Slb slb, VirtualServer vs, Group group, String ruleType) throws Exception {
        //Use Rule If Has Rules.
        if (configHandler.getEnable("enable.rule.on.models", true)) {
            if (ruleType == null) {
                return false;
            }
            if (group != null) {
                for (Rule rule : group.getRuleSet()) {
                    if (ruleType.equalsIgnoreCase(rule.getRuleType())) {
                        return true;
                    }
                }
            }
            if (vs != null) {
                for (Rule rule : vs.getRuleSet()) {
                    if (ruleType.equalsIgnoreCase(rule.getRuleType())) {
                        return true;
                    }
                }
            }
            if (slb != null) {
                for (Rule rule : slb.getRuleSet()) {
                    if (ruleType.equalsIgnoreCase(rule.getRuleType())) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
}
