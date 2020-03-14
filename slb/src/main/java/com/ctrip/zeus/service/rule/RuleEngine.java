package com.ctrip.zeus.service.rule;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.rule.model.RuleDataContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rule Engine Interface.
 */
public interface RuleEngine {

    /**
     * use for get engine type
     *
     * @RETURN Rule Type. ALL Rule Types Defined In Class RuleType
     */
    String getType();

    /**
     * Get Order By Stages
     *
     * @param stage , All Stage Defined In Class RuleStages. Stage position for where the rule to take effect.
     * @return
     */
    int getOrder(String stage);

    /**
     * Get All Stage Orders.
     *
     * @return
     */
    Map<String, Integer> getStageOrders();

    /**
     * Get Engine Active Stages. Engine Should Only Work In These Stages.
     *
     * @return Stage Set
     */
    Set<String> activeStages();

    /**
     * Engine to merge Rules
     *
     * @param rules
     * @throws ValidationException
     */
    List<Rule> merge(List<List<Rule>> rules);

    /**
     * Validate Rule
     *
     * @param rule
     * @throws ValidationException
     */
    void validate(Rule rule) throws ValidationException;

    /**
     * Validate Rules
     *
     * @param rules
     * @throws ValidationException
     */
    void validate(List<Rule> rules) throws ValidationException;

    /**
     * Generate Nginx Conf With Rules.
     *
     * @param rules input rules.
     */
    String generate(List<Rule> rules, String stage) throws ValidationException;


    /**
     * Generate Nginx Conf With Rules.
     *  @param rules      input rules.
     * @param confWriter Nginx conf writer.
     * @param stage
     */
    void generate(List<Rule> rules, ConfWriter confWriter, String stage) throws ValidationException;


    /**
     * Generate Nginx Conf With Rules.
     *  @param rules      input rules.
     * @param confWriter Nginx conf writer.
     * @param stage
     */
    void generate(List<Rule> rules, ConfWriter confWriter, String stage, RuleDataContext ruleDataContext) throws ValidationException;

    /**
     * Remove Duplicate Rule In List. In case of add same rule for target.
     * Should be used in add or update rules.
     *
     * @param rules
     * @return
     */
    List<Rule> removeDuplicates(List<Rule> rules) throws ValidationException;
}
