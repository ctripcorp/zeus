package com.ctrip.zeus.service.rule;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.rule.model.RuleDataContext;
import com.ctrip.zeus.service.rule.model.RuleType;
import com.ctrip.zeus.service.rule.util.ValidateUtils;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

@Service
public abstract class AbstractRuleEngine implements RuleEngine {
    @Resource
    private RuleManager ruleManager;

    private final String _ENGINE_NAME = this.getClass().getSimpleName();

    private Map<String, Integer> orders = new HashMap<>();
    private Set<String> effectTargetTypes = new HashSet<>();

    @PostConstruct
    public void init() {
        ruleManager.registerEngine(this);
        registerEffectTargetTypes(RuleTargetType.DEFAULT.getName());
    }

    public String getEngineName() {
        return _ENGINE_NAME;
    }

    public void registerStage(String stage, int order) {
        orders.put(stage, order);
    }

    public void registerEffectTargetTypes(String targetType) {
        effectTargetTypes.add(targetType.toUpperCase());
    }

    @Override
    public Set<String> activeStages() {
        return orders.keySet();
    }

    @Override
    public int getOrder(String stage) {
        return orders.get(stage) == null ? -1000 : orders.get(stage);
    }

    @Override
    public Map<String, Integer> getStageOrders() {
        return orders;
    }

    @Override
    public List<Rule> merge(List<List<Rule>> rules) {
        MergeStrategy mergeStrategy = getMergeStrategy();
        String ruleType = getType();
        return mergeStrategy.merge(ruleType, rules);
    }

    protected abstract MergeStrategy getMergeStrategy();

    @Override
    public void validate(List<Rule> rules) throws ValidationException {
        for (Rule rule : rules) {
            validate(rule);
        }
    }

    @Override
    public final void validate(Rule rule) throws ValidationException {
        if (rule == null) throw new ValidationException("[Rule Engine][Validate] Rule for current engine is null");

        String supportedType = getType();
        String ruleType = rule.getRuleType();

        if (Strings.isNullOrEmpty(ruleType) || !ruleType.equalsIgnoreCase(supportedType)) {
            throw new ValidationException(String.format("[Rule Engine][Validate]Rule's type passed for current engine is not: %s", supportedType));
        }

        if (Strings.isNullOrEmpty(rule.getTargetType()) || !effectTargetTypes.contains(rule.getTargetType().toUpperCase())) {
            throw new ValidationException("Invalidate Rule Target Type." + getType() + " Effect Target Types:" + Joiner.on(",").join(effectTargetTypes));
        }

        doValidate(rule);
    }

    @Override
    public void generate(List<Rule> rules, ConfWriter confWriter, String stage, RuleDataContext ruleDataContext) throws ValidationException {
        generate(rules, confWriter, stage);
    }


    /**
     * Rule specific validation
     *
     * @param rule the collection rules apply to this rule engine
     * @throws ValidationException if the rule for engine is invalid
     */
    protected abstract void doValidate(Rule rule) throws ValidationException;

    /**
     * getEffectTargetTypes
     *@return TargetTypes
     */
    protected Set<String> getEffectTargetTypes() {
        return effectTargetTypes;
    }

    /**
     * Remove duplicated rules apply to engine
     * Tips: Override if the engine allow duplicated rules
     *
     * @param rules the collection rules apply to this rule engine
     * @throws ValidationException if the rules is empty
     */
    @Override
    public List<Rule> removeDuplicates(List<Rule> rules) throws ValidationException {
        Map<String, Integer> ruleIndexedMap = new HashMap<>();
        List<Rule> cleanedRules = new ArrayList<>();

        for (Rule rule : rules) {
            RuleType ruleType = RuleType.getRuleType(rule.getRuleType());
            if (ruleType.isAllowMultiple()) {
                cleanedRules.add(rule);
                continue;
            }

            Integer index = ruleIndexedMap.get(rule.getRuleType());
            if (index == null) {
                cleanedRules.add(rule);
                ruleIndexedMap.put(rule.getRuleType(), cleanedRules.size() - 1);
            } else {
                if (cleanedRules.get(index).getId() < rule.getId()) {
                    cleanedRules.set(index, rule);
                }
            }
        }

        return cleanedRules;
    }

    protected HashMap<String, Object> getAttributes(Rule rule) {
        String attributes = rule.getAttributes();
        return ObjectJsonParser.parse(attributes, new TypeReference<HashMap<String, Object>>() {
        });
    }

    protected void validateAttribute(HashMap<String, Object> attributes, String key, String valueType) throws ValidationException {
        String className = this.getClass().getSimpleName();
        Object value = attributes.get(key);
        ValidateUtils.notNullAndEmpty(value, "[[RuleEngine=" + className + "]]Rule " + key + " shall not be null. ");
        if ("boolean".equalsIgnoreCase(valueType)) {
            ValidateUtils.isBooleanValue(value, "[[RuleEngine=" + className + "]]Rule " + key + " shall be in boolean format. ");
        } else if ("integer".equalsIgnoreCase(valueType)) {
            ValidateUtils.isIntValue(value, "[[RuleEngine=" + className + "]]Rule " + key + " shall be in int format. ");
        } else if ("long".equalsIgnoreCase(valueType)) {
            ValidateUtils.isLongValue(value, "[[RuleEngine=\" + className + \"]]Rule \" + key + \" shall be in long format. ");
        }
    }
}
