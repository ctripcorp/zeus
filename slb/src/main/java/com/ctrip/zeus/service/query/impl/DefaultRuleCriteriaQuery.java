package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dao.entity.RuleRule;
import com.ctrip.zeus.dao.entity.RuleRuleExample;
import com.ctrip.zeus.dao.mapper.RuleRuleMapper;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.query.RuleCriteriaQuery;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.RuleQueryCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component("ruleCriteriaQuery")
public class DefaultRuleCriteriaQuery implements RuleCriteriaQuery {
    @Resource
    private RuleRuleMapper ruleRuleMapper;

    @Override
    public Set<Long> queryByTarget(String targetId, String targetType){
        Set<Long> results = new HashSet<>();
        // compatible: old target type is integer
        RuleTargetType ruleTargetType = RuleTargetType.getTargetType(targetType);
        Integer ruleTargetTypeId = ruleTargetType.getId();

        List<RuleRule> slbRules = new ArrayList<>();
        if (targetId != null && targetType != null) {
            slbRules = ruleRuleMapper.findRulesByTargetIdAndTargetType(targetId, targetType);
            List<RuleRule> compatibleRuleDos = ruleRuleMapper.findRulesByTargetIdAndTargetType(targetId, ruleTargetTypeId.toString());
            if (compatibleRuleDos != null && compatibleRuleDos.size() > 0) {
                slbRules.addAll(compatibleRuleDos);
            }
        } else if (targetId == null) {
            slbRules = ruleRuleMapper.findRulesByTargetType(targetType);
            List<RuleRule> compatibleRuleDos = ruleRuleMapper.findRulesByTargetType(ruleTargetTypeId.toString());
            if (compatibleRuleDos != null && compatibleRuleDos.size() > 0) {
                slbRules.addAll(compatibleRuleDos);
            }
        }
        for (RuleRule rule : slbRules) {
            results.add(rule.getId());
        }


        return results;
    }

    @Override
    public Long queryByName(String name) throws Exception {
        return null;
    }

    @Override
    public Set<Long> fuzzyQueryByName(String name) throws Exception {
        Set<Long> result = new HashSet<>();

        name = String.format("%%%s%%", name);
        for (RuleRule rule : ruleRuleMapper.selectByExample(new RuleRuleExample().createCriteria().andNameLike(name).example())) {
            result.add(rule.getId());
        }
        return result;
    }

    @Override
    public IdVersion[] queryByCommand(final QueryCommand query, final SelectionMode mode) throws Exception {
        final RuleQueryCommand queryCommand = (RuleQueryCommand) query;
        final Long[] filteredIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return queryCommand.hasValue(queryCommand.id);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : queryCommand.getValue(queryCommand.id)) {
                            result.add(Long.parseLong(s));
                        }
                        return result;
                    }
                }).addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return queryCommand.hasValue(queryCommand.fuzzy_name);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<Long>();
                        for (String s : queryCommand.getValue(queryCommand.fuzzy_name)) {
                            result.addAll(fuzzyQueryByName(s));
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return queryCommand.hasValue(queryCommand.target_id) && queryCommand.hasValue(queryCommand.target_type);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        String[] targetType = queryCommand.getValue(queryCommand.target_type);

                        for (String s : queryCommand.getValue(queryCommand.target_id)) {
                            result.addAll(queryByTarget(s, targetType[0]));
                        }
                        return result;
                    }
                }).addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return !queryCommand.hasValue(queryCommand.target_id) && queryCommand.hasValue(queryCommand.target_type);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        String[] targetType = queryCommand.getValue(queryCommand.target_type);
                        result.addAll(queryByTarget(null, targetType[0]));
                        return result;
                    }
                }).build(Long.class).run();

        IdVersion[] result = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return filteredIds != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return queryByIdsAndMode(filteredIds, mode);
                    }
                }).build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return null;
                        if (result.size() == 0) return new IdVersion[0];
                        if (filteredIds == null) {
                            Long[] arr = new Long[result.size()];
                            int i = 0;
                            for (IdVersion e : result) {
                                arr[i] = e.getId();
                                i++;
                            }
                            result.retainAll(queryByIdsAndMode(arr, mode));
                        }
                        return result.toArray(new IdVersion[result.size()]);
                    }
                });

        return result;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> results = new HashSet<>();
        for (RuleRule rule : ruleRuleMapper.selectByExample(new RuleRuleExample())) {
            results.add(rule.getId());
        }
        return results;
    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> results = new HashSet<>();
        for (RuleRule rule : ruleRuleMapper.selectByExample(new RuleRuleExample())) {
            results.add(new IdVersion(rule.getId(), 0));
        }

        return results;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        if (ids == null || ids.length == 0) return result;
        for (RuleRule r : ruleRuleMapper.selectByExample(new RuleRuleExample().createCriteria().andIdIn(Arrays.asList(ids)).example())) {
            result.add(new IdVersion(r.getId(), 0));
        }

        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception {
        return new IdVersion[0];
    }
}
