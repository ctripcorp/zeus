package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.RuleDao;
import com.ctrip.zeus.dal.core.RuleDo;
import com.ctrip.zeus.dal.core.RuleEntity;
import com.ctrip.zeus.model.entity.Rule;
import com.ctrip.zeus.service.model.RuleRepository;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.RulePhase;
import com.ctrip.zeus.service.model.common.RuleSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/1/9.
 */
@Repository("ruleRepository")
public class RuleRepositoryImpl implements RuleRepository {
    @Resource
    private RuleDao ruleDao;

    @Override
    public RuleSet getRulesByReference(Long refId, MetaType refType) throws Exception {
        RuleSet<Long> ruleSet = new RuleSet<>(refId);
        for (RuleDo e : ruleDao.findAllByTargetAndType(refId, refType.getId(), RuleEntity.READSET_FULL)) {
            Rule r = new Rule();
            ruleSet.addRule(r);
            r.setName(e.getName()).setData(e.getValue()).setPhase(RulePhase.getRulePhase(e.getPhase()).toString()).setType(e.getType()).setVersion(e.getVersion());
        }
        return ruleSet;
    }

    @Override
    public RuleSet[] batchGetRulesByReference(Long[] refId, MetaType refType) throws Exception {
        RuleSet[] ruleSetArray = new RuleSet[refId.length];
        Map<Long, Integer> idxRef = new HashMap<>();
        for (int i = 0; i < refId.length; i++) {
            ruleSetArray[i] = new RuleSet<>(refId[i]);
            idxRef.put(refId[i], i);
        }

        List<RuleDo> result = ruleDao.findAllByTargetsAndType(refId, refType.getId(), RuleEntity.READSET_FULL);
        for (int i = 0; i < result.size(); i++) {
            RuleDo e = result.get(i);
            Rule r = new Rule();
            ruleSetArray[idxRef.get(e.getId())].addRule(r);
            r.setName(e.getName()).setData(e.getValue()).setPhase(RulePhase.getRulePhase(e.getPhase()).toString()).setType(e.getType()).setVersion(e.getVersion());
        }
        return ruleSetArray;
    }

    @Override
    public void setRulesToReference(Long refId, MetaType refType, List<Rule> rules) throws Exception {
        Map<String, RuleDo> doMap = new HashMap<>();
        for (RuleDo e : ruleDao.findAllByTargetAndType(refId, refType.getId(), RuleEntity.READSET_FULL)) {
            doMap.put(e.getName(), e);
        }

        List<RuleDo> insert = new ArrayList<>();
        for (Rule r : rules) {
            RuleDo d = doMap.remove(r.getName());
            if (d != null) {
                if (!equalsIgnoreTarget(r, d)) {
                    castRuleToRuleDo(r, d, refId, refType.getId());
                    d.setVersion(d.getVersion() + 1).setDataChangeLastTime(null);
                    insert.add(d);
                }
            } else {
                insert.add(new RuleDo().setName(r.getName()).setValue(r.getData()).setPhase(RulePhase.valueOf(r.getPhase()).getId())
                        .setType(r.getType()).setTargetId(refId).setTargetType(refType.getId()));
            }
        }

        ruleDao.insert(insert.toArray(new RuleDo[insert.size()]));
        ruleDao.delete(doMap.values().toArray(new RuleDo[doMap.size()]));
    }

    @Override
    public void partialUpdateReferenceRules(Long refId, MetaType refType, List<Rule> rules) throws Exception {
        Map<String, Rule> ruleMap = new HashMap<>();
        for (Rule r : rules) {
            ruleMap.put(r.getName(), r);
        }
        List<RuleDo> insert = new ArrayList<>();
        for (RuleDo d : ruleDao.findAllByTargetAndType(refId, refType.getId(), RuleEntity.READSET_FULL)) {
            Rule r = ruleMap.get(d.getName());
            if (r != null) {
                if (!equalsIgnoreTarget(r, d)) {
                    castRuleToRuleDo(r, d, refId, refType.getId());
                    d.setVersion(d.getVersion() + 1).setDataChangeLastTime(null);
                    insert.add(d);
                }
            }
        }
        ruleDao.insert(insert.toArray(new RuleDo[insert.size()]));
    }

    @Override
    public void removeRuleByReference(Long[] refId, MetaType refType, String ruleName) throws Exception {
        Set<Long> refs = new HashSet<>();
        for (Long rId : refId) {
            refs.add(rId);
        }

        List<RuleDo> result = ruleDao.findAllByNameAndType(ruleName, refType.getId(), RuleEntity.READSET_FULL);
        Iterator<RuleDo> iter = result.iterator();
        while (iter.hasNext()) {
            RuleDo e = iter.next();
            if (!refs.contains(e.getTargetId())) {
                iter.remove();
            }
        }
        ruleDao.delete(result.toArray(new RuleDo[result.size()]));
    }

    @Override
    public void clearRuleByReference(Long refId, MetaType refType) throws Exception {
        ruleDao.deleteByTargetAndType(new RuleDo().setTargetId(refId).setTargetType(refType.getId()));
    }

    private static boolean equalsIgnoreTarget(Rule r, RuleDo d) {
        boolean result = true;
        result &= (r.getName().equals(d.getName()));
        result &= (r.getType().equals(d.getType()));
        result &= (r.getVersion().equals(d.getVersion()));
        result &= (r.getData().equals(d.getValue()));
        result &= (r.getPhase().equals(RulePhase.getRulePhase(d.getPhase())));
        return result;
    }

    private static void castRuleToRuleDo(Rule r, RuleDo d, Long refId, int refType) {
        d.setName(r.getName()).setValue(r.getData()).setPhase(RulePhase.valueOf(r.getPhase()).getId())
                .setType(r.getType()).setTargetId(refId).setTargetType(refType).setVersion(r.getVersion());
    }
}
