package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Rule;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.RuleSet;

import java.util.List;

/**
 * Created by zhoumy on 2017/1/9.
 */
public interface RuleRepository {

    RuleSet getRulesByReference(Long refId, MetaType refType) throws Exception;

    RuleSet[] batchGetRulesByReference(Long[] refId, MetaType refType) throws Exception;

    void setRulesToReference(Long refId, MetaType refType, List<Rule> rules) throws Exception;

    void partialUpdateReferenceRules(Long refId, MetaType refType, List<Rule> rules) throws Exception;

    void removeRuleByReference(Long[] refId, MetaType refType, String ruleName) throws Exception;

    void clearRuleByReference(Long refId, MetaType refType) throws Exception;
}