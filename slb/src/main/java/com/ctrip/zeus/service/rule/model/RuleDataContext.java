package com.ctrip.zeus.service.rule.model;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.VirtualServer;

import java.util.*;

public class RuleDataContext {
    private List<Rule>[] rules = null;
    private List<Rule> defaultRules = null;

    private Map<String, List<List<Rule>>> rulesMap = new HashMap<>();
    private Map<String, List<Rule>> defaultRulesMap = new HashMap<>();

    private Group group = null;
    private VirtualServer vs = null;
    private Slb slb = null;

    public List<List<Rule>> getRules(boolean withDefault, String ruleType) {
        if (ruleType != null) {
            List<List<Rule>> res = new ArrayList<>();
            List<List<Rule>> org = rulesMap.get(ruleType);
            if (org != null) {
                res = new ArrayList<>(org);
            }
            if (withDefault && defaultRulesMap.get(ruleType) != null) {
                res.add(defaultRulesMap.get(ruleType));
            }
            return res;
        }

        if (withDefault && defaultRules != null) {
            List<List<Rule>> res = new ArrayList<>();
            if (rules != null) {
                Collections.addAll(res, rules);
            }
            if (defaultRules != null) {
                res.add(defaultRules);
            }
            return res;
        }
        if (rules == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(rules);
        }
    }

    public RuleDataContext setDefaultRules(List<Rule> ruleList) {
        defaultRules = ruleList;
        if (defaultRules != null) {
            for (Rule rule : defaultRules) {
                defaultRulesMap.putIfAbsent(rule.getRuleType(), new ArrayList<>());
                defaultRulesMap.get(rule.getRuleType()).add(rule);
            }
        }
        return this;
    }

    public RuleDataContext setRules(List<Rule>... ruleList) {
        if (ruleList != null) {
            for (List<Rule> rules : ruleList) {
                if (rules != null && rules.size() > 0) {
                    this.rules = ruleList;
                    break;
                }
            }
        }
        if (this.rules == null) return this;

        int index = 0;
        for (List<Rule> r : this.rules) {
            if (r == null) continue;
            for (Rule rule : r) {
                rulesMap.putIfAbsent(rule.getRuleType(), getArray(rules.length));
                List<Rule> tmp = rulesMap.get(rule.getRuleType()).get(index);
                if (tmp == null) {
                    tmp = new ArrayList<>();
                    rulesMap.get(rule.getRuleType()).set(index, tmp);
                }
                tmp.add(rule);
            }
            index++;
        }
        return this;
    }

    private List<List<Rule>> getArray(int length) {
        List<List<Rule>> tmp = new ArrayList<>(this.rules.length);
        for (int i = 0; i < length; i++) {
            tmp.add(null);
        }
        return tmp;
    }

    public boolean containsRule(RuleType ruleType) {
        if (this.rules == null) return false;

        for (List<Rule> ruleList : this.rules) {
            if (ruleList != null) {
                for (Rule rule : ruleList) {
                    if (ruleType.getName().equalsIgnoreCase(rule.getRuleType())) return true;
                }
            }
        }

        return false;
    }

    public Group getGroup() {
        return group;
    }

    public RuleDataContext setGroup(Group group) {
        this.group = group;
        return this;
    }

    public VirtualServer getVs() {
        return vs;
    }

    public RuleDataContext setVs(VirtualServer vs) {
        this.vs = vs;
        return this;
    }

    public Slb getSlb() {
        return slb;
    }

    public RuleDataContext setSlb(Slb slb) {
        this.slb = slb;
        return this;
    }
}