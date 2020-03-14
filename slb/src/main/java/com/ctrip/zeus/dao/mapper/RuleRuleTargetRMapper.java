package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.RuleRuleTargetR;
import com.ctrip.zeus.dao.entity.RuleRuleTargetRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RuleRuleTargetRMapper {
    long countByExample(RuleRuleTargetRExample example);

    int deleteByExample(RuleRuleTargetRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RuleRuleTargetR record);

    int insertSelective(RuleRuleTargetR record);

    RuleRuleTargetR selectOneByExample(RuleRuleTargetRExample example);

    RuleRuleTargetR selectOneByExampleSelective(@Param("example") RuleRuleTargetRExample example, @Param("selective") RuleRuleTargetR.Column ... selective);

    List<RuleRuleTargetR> selectByExampleSelective(@Param("example") RuleRuleTargetRExample example, @Param("selective") RuleRuleTargetR.Column ... selective);

    List<RuleRuleTargetR> selectByExample(RuleRuleTargetRExample example);

    RuleRuleTargetR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") RuleRuleTargetR.Column ... selective);

    RuleRuleTargetR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RuleRuleTargetR record, @Param("example") RuleRuleTargetRExample example);

    int updateByExample(@Param("record") RuleRuleTargetR record, @Param("example") RuleRuleTargetRExample example);

    int updateByPrimaryKeySelective(RuleRuleTargetR record);

    int updateByPrimaryKey(RuleRuleTargetR record);

    int upsert(RuleRuleTargetR record);

    int upsertSelective(RuleRuleTargetR record);

    /*Self Defined*/
    int batchInsert(List<RuleRuleTargetR> ruleRuleTargetRS);
    int batchInsertIncludeId(List<RuleRuleTargetR> ruleRuleTargetRS);
    List<RuleRuleTargetR> concatSelect(@Param("concats") String[] concats);
    /*Self Defined*/
}