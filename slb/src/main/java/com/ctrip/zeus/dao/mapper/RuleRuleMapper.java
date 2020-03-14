package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.RuleRule;
import com.ctrip.zeus.dao.entity.RuleRuleExample;
import com.ctrip.zeus.dao.entity.RuleRuleWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RuleRuleMapper {
    long countByExample(RuleRuleExample example);

    int deleteByExample(RuleRuleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RuleRuleWithBLOBs record);

    int insertSelective(RuleRuleWithBLOBs record);

    RuleRule selectOneByExample(RuleRuleExample example);

    RuleRuleWithBLOBs selectOneByExampleSelective(@Param("example") RuleRuleExample example, @Param("selective") RuleRuleWithBLOBs.Column... selective);

    RuleRuleWithBLOBs selectOneByExampleWithBLOBs(RuleRuleExample example);

    List<RuleRuleWithBLOBs> selectByExampleSelective(@Param("example") RuleRuleExample example, @Param("selective") RuleRuleWithBLOBs.Column... selective);

    List<RuleRuleWithBLOBs> selectByExampleWithBLOBs(RuleRuleExample example);

    List<RuleRule> selectByExample(RuleRuleExample example);

    RuleRuleWithBLOBs selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") RuleRuleWithBLOBs.Column... selective);

    RuleRuleWithBLOBs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RuleRuleWithBLOBs record, @Param("example") RuleRuleExample example);

    int updateByExampleWithBLOBs(@Param("record") RuleRuleWithBLOBs record, @Param("example") RuleRuleExample example);

    int updateByExample(@Param("record") RuleRule record, @Param("example") RuleRuleExample example);

    int updateByPrimaryKeySelective(RuleRuleWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(RuleRuleWithBLOBs record);

    int updateByPrimaryKey(RuleRule record);

    int upsert(RuleRule record);

    int upsertSelective(RuleRuleWithBLOBs record);

    int upsertWithBLOBs(RuleRuleWithBLOBs record);

    /*Self Defined*/
    int batchInsertIncludeId(List<RuleRuleWithBLOBs> record);

    int insertIncludeId(RuleRuleWithBLOBs record);

    List<RuleRule> findRulesByTargetIdAndTargetType(@Param("ruleTargetId") String ruleTargetId, @Param("ruleTargetType") String ruleTargetType);

    List<RuleRule> findRulesByTargetType(@Param("targetType") String targetType);
    /*Self Defined*/
}