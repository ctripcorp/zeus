package com.ctrip.zeus.service.rule.merges;

import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.rule.MergeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("collectedMergeStrategy")
public class CollectedMergeStrategy implements MergeStrategy {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<Rule> merge(String ruleType, List<List<Rule>> rules) {
        if (rules == null) return Collections.emptyList();

        List<Rule> results = new ArrayList<>();

        for (List<Rule> ruleList : rules) {
            if (ruleList != null && ruleList.size() > 0) {
                for (Rule rule : ruleList) {
                    logger.info("[Rule][Inherited Merge Strategy] Current Merging rule name:"
                            + rule.getName()
                            + ", type:" + rule.getRuleType()
                            + ", target id:" + rule.getTargetId()
                            + ", target type:" + rule.getTargetType()
                    );
                    if (ruleType.equalsIgnoreCase(rule.getRuleType())) {
                        results.add(rule);
                    }
                }
            }
        }
        if (results.size() > 0) return results;
        return Collections.emptyList();
    }
}
