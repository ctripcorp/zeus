package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbArchiveTrafficPolicy;
import com.ctrip.zeus.dao.entity.SlbArchiveTrafficPolicyExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbArchiveTrafficPolicyMapper {
    long countByExample(SlbArchiveTrafficPolicyExample example);

    int deleteByExample(SlbArchiveTrafficPolicyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbArchiveTrafficPolicy record);

    int insertSelective(SlbArchiveTrafficPolicy record);

    SlbArchiveTrafficPolicy selectOneByExample(SlbArchiveTrafficPolicyExample example);

    SlbArchiveTrafficPolicy selectOneByExampleSelective(@Param("example") SlbArchiveTrafficPolicyExample example, @Param("selective") SlbArchiveTrafficPolicy.Column ... selective);

    List<SlbArchiveTrafficPolicy> selectByExampleSelective(@Param("example") SlbArchiveTrafficPolicyExample example, @Param("selective") SlbArchiveTrafficPolicy.Column ... selective);

    List<SlbArchiveTrafficPolicy> selectByExample(SlbArchiveTrafficPolicyExample example);

    SlbArchiveTrafficPolicy selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbArchiveTrafficPolicy.Column ... selective);

    SlbArchiveTrafficPolicy selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbArchiveTrafficPolicy record, @Param("example") SlbArchiveTrafficPolicyExample example);

    int updateByExample(@Param("record") SlbArchiveTrafficPolicy record, @Param("example") SlbArchiveTrafficPolicyExample example);

    int updateByPrimaryKeySelective(SlbArchiveTrafficPolicy record);

    int updateByPrimaryKey(SlbArchiveTrafficPolicy record);

    int upsert(SlbArchiveTrafficPolicy record);

    int upsertSelective(SlbArchiveTrafficPolicy record);

    /*Self Defined*/
    int batchInsertIncludeId(List<SlbArchiveTrafficPolicy> records);

    List<SlbArchiveTrafficPolicy> concatSelect(@Param("concats") String[] concats);
    /*Self Defined*/
}