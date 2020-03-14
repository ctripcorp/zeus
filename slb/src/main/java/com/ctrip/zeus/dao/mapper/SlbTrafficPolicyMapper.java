package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbTrafficPolicy;
import com.ctrip.zeus.dao.entity.SlbTrafficPolicyExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbTrafficPolicyMapper {
    long countByExample(SlbTrafficPolicyExample example);

    int deleteByExample(SlbTrafficPolicyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbTrafficPolicy record);

    int insertSelective(SlbTrafficPolicy record);

    SlbTrafficPolicy selectOneByExample(SlbTrafficPolicyExample example);

    SlbTrafficPolicy selectOneByExampleSelective(@Param("example") SlbTrafficPolicyExample example, @Param("selective") SlbTrafficPolicy.Column ... selective);

    List<SlbTrafficPolicy> selectByExampleSelective(@Param("example") SlbTrafficPolicyExample example, @Param("selective") SlbTrafficPolicy.Column ... selective);

    List<SlbTrafficPolicy> selectByExample(SlbTrafficPolicyExample example);

    SlbTrafficPolicy selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbTrafficPolicy.Column ... selective);

    SlbTrafficPolicy selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbTrafficPolicy record, @Param("example") SlbTrafficPolicyExample example);

    int updateByExample(@Param("record") SlbTrafficPolicy record, @Param("example") SlbTrafficPolicyExample example);

    int updateByPrimaryKeySelective(SlbTrafficPolicy record);

    int updateByPrimaryKey(SlbTrafficPolicy record);

    int upsert(SlbTrafficPolicy record);

    int upsertSelective(SlbTrafficPolicy record);

    /*Self defined*/
    int batchUpdate(List<SlbTrafficPolicy> records);

    int batchInsertIncludeId(List<SlbTrafficPolicy> records);

    int insertIncludeId(SlbTrafficPolicy record);
    /*Self defined*/
}