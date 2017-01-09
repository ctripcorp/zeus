package com.ctrip.zeus.service.model.common;

import com.ctrip.zeus.model.entity.Rule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2017/1/9.
 */
public class RuleSet<T> {
    private T ref;
    private List<Rule> rules;

    public RuleSet() {
        rules = new ArrayList<>();
    }

    public T getRef() {
        return ref;
    }

    public RuleSet<T> setRef(T ref) {
        this.ref = ref;
        return this;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public RuleSet<T> addRule(Rule rule) {
        rules.add(rule);
        return this;
    }
}
