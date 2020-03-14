package com.ctrip.zeus.service.rule;

import com.ctrip.zeus.model.model.Rule;

import java.util.List;

/*
* Merge rules engine
* */
public interface MergeStrategy {
    List<Rule> merge(String ruleType, List<List<Rule>> rules);
}
