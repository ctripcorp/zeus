package com.ctrip.zeus.service.rule;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.rule.model.RuleDataContext;
import com.ctrip.zeus.service.rule.model.RuleType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ruleManager")
public class RuleManager {

    /**
     * Stage-> EngineList.
     */
    private Map<String, List<RuleEngine>> stageEngineMap = new HashMap<>();
    /**
     * Type-> Engine.
     */
    private Map<String, RuleEngine> engineMap = new HashMap<>();

    /**
     * Register Engine By Stage And Sort By Stage Order.
     *
     * @param engine
     */
    public synchronized void registerEngine(RuleEngine engine) {
        RuleEngine engineSaved = engineMap.put(engine.getType(), engine);

        // Engine with same type already existed
        if (engineSaved != null) {
            return;
        }

        Set<String> stages = engine.activeStages();
        for (final String s : stages) {
            if (!stageEngineMap.containsKey(s)) {
                stageEngineMap.put(s, new ArrayList<>());
            }
            stageEngineMap.get(s).add(engine);
            stageEngineMap.get(s).sort((o1, o2) -> {
                int tmp = o2.getOrder(s) - o1.getOrder(s);
                if (tmp != 0) {
                    return tmp;
                } else {
                    return o2.getType().compareTo(o1.getType());
                }
            });
        }
    }

    /**
     * Initialize Context For Rule Generate Config.
     *
     * @param rules All Rules Of Target Item.
     * @return Context For Rule Generate Config. Should Be the parameter for function RuleManager.write.
     */
    public RuleDataContext initializeContext(List<Rule> defaultRules, List<Rule>... rules) {
        RuleDataContext ruleDataContext = new RuleDataContext();
        ruleDataContext.setRules(rules);
        ruleDataContext.setDefaultRules(defaultRules);
        return ruleDataContext;
    }

    /***
     * Write Config To Conf Writer.
     * @param ruleDataContext Rule Data Context.
     * @param stage Stage Position for Rule to take effect.
     * @param confWriter conf writer
     */
    public void write(RuleDataContext ruleDataContext, String stage, ConfWriter confWriter) throws ValidationException {
        write(ruleDataContext, stage, confWriter, true);
    }

    public void write(RuleDataContext ruleDataContext, String stage, ConfWriter confWriter, boolean useDefaultRules) throws ValidationException {
        if (stage == null || stage.isEmpty()) throw new ValidationException("Rule stage is required");
        List<RuleEngine> ruleEngines = stageEngineMap.get(stage);
        if (ruleEngines == null) return;
        for (RuleEngine engine : ruleEngines) {
            // get rules from data context
            List<List<Rule>> originRules = ruleDataContext.getRules(useDefaultRules, engine.getType());
            if (originRules == null || originRules.isEmpty()) continue;
            List<Rule> mergedRules = engine.merge(originRules);
            if (!mergedRules.isEmpty()) {
                validate(mergedRules);
                List<Rule> uniqueRules = engine.removeDuplicates(mergedRules);
                engine.generate(uniqueRules, confWriter, stage, ruleDataContext);
            }
        }
    }

    /**
     * Validate Rules
     *
     * @param rules Rules to be validated.
     * @throws ValidationException
     */
    public void validate(List<Rule> rules) throws ValidationException {
        if (rules == null || rules.size() == 0)
            throw new ValidationException("[Rule Manager][Validation]Rules for current manager is null");

        // engine validation
        for (Rule rule : rules) {
            RuleType type = RuleType.getRuleType(rule.getRuleType());
            RuleEngine engine = engineMap.get(type.getName());
            if (engine == null) {
                throw new ValidationException("[Rule Manager][Validation]No rule engine instance to process current rule, rule type=" + type.getName());
            }
            engine.validate(rule);
        }
    }
}
