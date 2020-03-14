package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.*;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.rule.RuleManager;
import com.ctrip.zeus.service.rule.model.RuleType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("ruleValidator")
public class DefaultRuleValidator implements RuleValidator {

    @Resource
    private SlbGroupMapper slbGroupMapper;
    @Resource
    private SlbSlbMapper slbSlbMapper;
    @Resource
    private SlbVirtualServerMapper slbVirtualServerMapper;
    @Resource
    private SlbTrafficPolicyMapper slbTrafficPolicyMapper;
    @Resource
    private RuleManager ruleManager;
    @Resource
    private RuleRuleMapper ruleRuleMapper;
    @Resource
    private RuleRuleTargetRMapper ruleRuleTargetRMapper;

    @Override
    public void validateFields(Rule rule) throws ValidationException {
        if (rule == null) {
            throw new ValidationException("Rule target entity shall not be entity");
        }

        // attributes
        String name = rule.getName();
        String targetId = rule.getTargetId();

        RuleTargetType ruleTargetType = RuleTargetType.getTargetType(rule.getTargetType());
        RuleType ruleType = RuleType.getRuleType(rule.getRuleType());

        if (name == null || name.isEmpty()) {
            throw new ValidationException("Rule name shall not be empty");
        }
        if (ruleType == null) {
            throw new ValidationException("Rule type is either empty or not allowed");
        }

        if (ruleTargetType == null) {
            throw new ValidationException("Target type " + rule.getTargetType() + " is not supported.");
        }
        if (targetId == null) {
            throw new ValidationException("Rule target id is required");
        }

        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        ruleManager.validate(rules);
    }

    @Override
    public void checkRuleTarget(RuleTargetType targetType, String targetId) throws Exception {
        if (!targetType.isNeedTarget()) return;

        Long target;
        Object searchedObject;
        switch (targetType) {
            case GROUP:
                target = RuleTargetType.parseLongTargetId(targetId);
                searchedObject = slbGroupMapper.selectOneByExampleSelective(new SlbGroupExample().createCriteria().andIdEqualTo(target).example());
                break;
            case SLB:
                target = RuleTargetType.parseLongTargetId(targetId);
                searchedObject = slbSlbMapper.selectByPrimaryKey(target);
                break;
            case VS:
                target = RuleTargetType.parseLongTargetId(targetId);
                searchedObject = slbVirtualServerMapper.selectByPrimaryKey(target);
                break;
            case TRAFFIC_POLICY:
                target = RuleTargetType.parseLongTargetId(targetId);
                searchedObject = slbTrafficPolicyMapper.selectByPrimaryKeySelective(target);
                break;
            default:
                searchedObject = null;
                break;
        }

        if (searchedObject == null) {
            throw new NotFoundException(String.format("%s with id = %d does not existed.", targetType.toString(), targetId));
        }
    }

    @Override
    public void checkRuleUpdate(Rule rule) throws Exception {
        if (rule == null || rule.getId() == null || rule.getId() == 0L) {
            throw new ValidationException("Rule id to be updated shall be greater than 0.");
        }

        Long ruleId = rule.getId();
        // get rule target
        RuleRuleWithBLOBs ruleWithBLOBs = ruleRuleMapper.selectByPrimaryKey(ruleId);
        if (ruleWithBLOBs == null) {
            throw new NotFoundException(String.format("Rule with id = %d does not existed.", ruleId));
        }
        Rule existed = ContentReaders.readRuleContent(ruleWithBLOBs.getContent());

        if (existed == null) {
            throw new ValidationException(String.format("Rule's content is null. Rule id = %d.", ruleId));
        }

        String existedTargetId = existed.getTargetId();
        String existedTargetType = existed.getTargetType();
        String existedRuleType = existed.getRuleType();

        String currentTargetId = rule.getTargetId();
        String currentTargetType = rule.getTargetType();

        boolean sameTarget = existedTargetId.equalsIgnoreCase(currentTargetId) && existedTargetType.equalsIgnoreCase(currentTargetType);
        boolean sameRuleType = rule.getRuleType().equalsIgnoreCase(existedRuleType);

        if (!sameTarget) {
            throw new ValidationException("Not allowed to change rule target id and target type in updating rule");
        }

        if (!sameRuleType) {
            throw new ValidationException("Not allowed to change rule type in updating rule");
        }
    }

    @Override
    public void checkRuleNew(Rule rule) throws Exception {
        RuleType ruleType = RuleType.getRuleType(rule.getRuleType());
        if (ruleType.isAllowMultiple()) {
            return;
        }

        String targetType = rule.getTargetType();
        String targetId = rule.getTargetId();

        // find
        List<RuleRuleTargetR> ruleRuleTargetRExamples = ruleRuleTargetRMapper.selectByExample(new RuleRuleTargetRExample().
                createCriteria().
                andTargetIdEqualTo(targetId).
                andTargetTypeEqualTo(targetType).example());

        if (ruleRuleTargetRExamples.size() == 0) return;
        List<Long> ruleIds = new ArrayList<>();
        for (RuleRuleTargetR ruleRuleTargetR : ruleRuleTargetRExamples) {
            ruleIds.add(ruleRuleTargetR.getRuleId());
        }

        // Existing rules
        List<RuleRule> ruleRules = ruleRuleMapper.selectByExample(new RuleRuleExample().createCriteria().andIdIn(ruleIds).example());
        if (ruleRules != null && ruleRules.size() > 0) {
            for (RuleRule ruleRule : ruleRules) {
                int existedRuleTypeId = ruleRule.getRuleType();
                if (existedRuleTypeId == ruleType.getId()) {
                    throw new ValidationException(String.format("Rule with type: %s already existed on target", ruleType.getName()));
                }
            }
        }
    }

    @Override
    public void checkRulesType(List<Rule> rules) throws Exception {
        Map<String, Integer> ruleIndexedMap = new HashMap<>();

        for (Rule rule : rules) {
            RuleType ruleType = RuleType.getRuleType(rule.getRuleType());
            if (ruleType.isAllowMultiple()) {
                continue;
            }

            Integer index = ruleIndexedMap.get(rule.getRuleType());
            if (index == null) {
                ruleIndexedMap.put(rule.getRuleType(), 0);
            } else {
                throw new ValidationException("Has duplicated rule type in request");
            }
        }
    }

    public void setRuleRuleMapper(RuleRuleMapper ruleRuleMapper) {
        this.ruleRuleMapper = ruleRuleMapper;
    }

    public void setRuleRuleTargetRMapper(RuleRuleTargetRMapper ruleRuleTargetRMapper) {
        this.ruleRuleTargetRMapper = ruleRuleTargetRMapper;
    }
}
