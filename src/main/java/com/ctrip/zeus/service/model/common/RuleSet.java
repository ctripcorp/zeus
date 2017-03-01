package com.ctrip.zeus.service.model.common;

import com.ctrip.zeus.model.entity.Rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2017/1/9.
 */
public class RuleSet<T> {
    private T ref;
    private Map<RulePhase, List<Rule>> rulesByPhase;
    private int size = 0;

    public RuleSet(T ref) {
        rulesByPhase = new HashMap<>();
        this.ref = ref;
    }

    public T getRef() {
        return ref;
    }

    public List<Rule> getRules() {
        List<Rule> result = new ArrayList<>(size);
        for (List<Rule> rules : rulesByPhase.values()) {
            result.addAll(rules);
        }
        return result;
    }

    public List<Rule> getRulesByPhase(RulePhase phase) {
        List<Rule> v = rulesByPhase.get(phase);
        return v == null ? new ArrayList<Rule>() : v;
    }

    public RuleSet<T> addRule(Rule rule) {
        RulePhase k = RulePhase.getRulePhase(rule.getPhaseId());
        if (k != null) {
            List<Rule> v = rulesByPhase.get(k);
            if (v == null) {
                v = new ArrayList<>();
                rulesByPhase.put(k, v);
            }
            v.add(rule);
            size++;
        }
        return this;
    }
}
