package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.model.common.RuleTargetType;

import java.util.List;

public interface RuleValidator {
    /*
    * Validate rule's required fields
    * */
    void validateFields(Rule rule) throws ValidationException;

    /*
    * Validate rule's target
    * */
    void checkRuleTarget(RuleTargetType targetType, String targetId) throws Exception;

    /*
    * Validate rule existed and rule target is same
    * */
    void checkRuleUpdate(Rule rule) throws Exception;

    /*
    * Validate rule type for target are valid
    * */
    void checkRuleNew(Rule rule) throws Exception;

    /*
    * Validate rules type for target are valid
    * */
    void checkRulesType(List<Rule> rules) throws Exception;
}
