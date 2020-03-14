package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbTrafficPolicyVsR;
import com.ctrip.zeus.dao.entity.SlbTrafficPolicyVsRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbTrafficPolicyVsRMapper {
    long countByExample(SlbTrafficPolicyVsRExample example);

    int deleteByExample(SlbTrafficPolicyVsRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbTrafficPolicyVsR record);

    int insertSelective(SlbTrafficPolicyVsR record);

    SlbTrafficPolicyVsR selectOneByExample(SlbTrafficPolicyVsRExample example);

    SlbTrafficPolicyVsR selectOneByExampleSelective(@Param("example") SlbTrafficPolicyVsRExample example, @Param("selective") SlbTrafficPolicyVsR.Column ... selective);

    List<SlbTrafficPolicyVsR> selectByExampleSelective(@Param("example") SlbTrafficPolicyVsRExample example, @Param("selective") SlbTrafficPolicyVsR.Column ... selective);

    List<SlbTrafficPolicyVsR> selectByExample(SlbTrafficPolicyVsRExample example);

    SlbTrafficPolicyVsR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbTrafficPolicyVsR.Column ... selective);

    SlbTrafficPolicyVsR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbTrafficPolicyVsR record, @Param("example") SlbTrafficPolicyVsRExample example);

    int updateByExample(@Param("record") SlbTrafficPolicyVsR record, @Param("example") SlbTrafficPolicyVsRExample example);

    int updateByPrimaryKeySelective(SlbTrafficPolicyVsR record);

    int updateByPrimaryKey(SlbTrafficPolicyVsR record);

    int upsert(SlbTrafficPolicyVsR record);

    int upsertSelective(SlbTrafficPolicyVsR record);

    /*Self defined*/
    List<SlbTrafficPolicyVsR> findByVsesAndPolicyVersion(@Param("ids") List<Long> ids);

    int batchInsert(List<SlbTrafficPolicyVsR> slbTrafficPolicyVsRS);

    int batchInsertIncludeId(List<SlbTrafficPolicyVsR> slbTrafficPolicyVsRS);

    List<SlbTrafficPolicyVsR> concatSelect(@Param("concats") String[] concats);
    /*Self defined*/
}