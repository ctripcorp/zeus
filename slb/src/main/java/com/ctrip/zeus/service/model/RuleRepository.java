package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.model.common.RuleTargetType;

import java.util.List;

/**
 * Created by zhoumy on 2017/1/9.
 */
public interface RuleRepository {

    /*
    * Save new rule.
    * Lock for target required outside
    * */
    Rule add(Rule rule) throws Exception;

    /*
    * Save new rules.
    * Lock for target required outside
    * */
    List<Rule> add(List<Rule> rules) throws Exception;

    /*
    * Update existing rule
    * */
    Rule update(Rule rule) throws Exception;


    /*
   * Update existing rules
   * */
    List<Rule> update(List<Rule> rules) throws Exception;


    /*
    * Set rules.
    * Lock for target required outside
    * */
    List<Rule> set(List<Rule> rules) throws Exception;


    /*
    * Get rules
    * */
    List<Rule> list(List<Long> ids) throws Exception;


    /*
    * Get rule by target type and target id
    * */
    List<Rule> getRulesByTarget(String targetId, String targetType) throws Exception;


    /*
    * Get default rules
    * */
    List<Rule> getDefaultRules() throws Exception;


    List<Rule> removeRuleByIds(List<Long> ids) throws Exception;

    /*
    * Remove target related rules
    * Lock for target required outside
    * */
    List<Rule> removeRulesByTarget(RuleTargetType target, String targetId) throws Exception;
}