package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbTrafficPolicyGroupR;
import com.ctrip.zeus.dao.entity.SlbTrafficPolicyGroupRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbTrafficPolicyGroupRMapper {
    long countByExample(SlbTrafficPolicyGroupRExample example);

    int deleteByExample(SlbTrafficPolicyGroupRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbTrafficPolicyGroupR record);

    int insertSelective(SlbTrafficPolicyGroupR record);

    SlbTrafficPolicyGroupR selectOneByExample(SlbTrafficPolicyGroupRExample example);

    SlbTrafficPolicyGroupR selectOneByExampleSelective(@Param("example") SlbTrafficPolicyGroupRExample example, @Param("selective") SlbTrafficPolicyGroupR.Column ... selective);

    List<SlbTrafficPolicyGroupR> selectByExampleSelective(@Param("example") SlbTrafficPolicyGroupRExample example, @Param("selective") SlbTrafficPolicyGroupR.Column ... selective);

    List<SlbTrafficPolicyGroupR> selectByExample(SlbTrafficPolicyGroupRExample example);

    SlbTrafficPolicyGroupR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbTrafficPolicyGroupR.Column ... selective);

    SlbTrafficPolicyGroupR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbTrafficPolicyGroupR record, @Param("example") SlbTrafficPolicyGroupRExample example);

    int updateByExample(@Param("record") SlbTrafficPolicyGroupR record, @Param("example") SlbTrafficPolicyGroupRExample example);

    int updateByPrimaryKeySelective(SlbTrafficPolicyGroupR record);

    int updateByPrimaryKey(SlbTrafficPolicyGroupR record);

    int upsert(SlbTrafficPolicyGroupR record);

    int upsertSelective(SlbTrafficPolicyGroupR record);

    /*Self defined */
    int batchInsert(List<SlbTrafficPolicyGroupR> records);

    List<SlbTrafficPolicyGroupR> concatSelect(@Param("concats") String[] concats);

    int batchInsertIncludeId(List<SlbTrafficPolicyGroupR> records);

    List<SlbTrafficPolicyGroupR> findByGroupsAndPolicyVersion(@Param("ids") List<Long> ids);

    List<SlbTrafficPolicyGroupR> findByPolicyAndPolicyVersion(@Param("ids") List<Long> ids);
    /*Self defined */
}