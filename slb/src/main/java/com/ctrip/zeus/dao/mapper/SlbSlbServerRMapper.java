package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbSlbServerR;
import com.ctrip.zeus.dao.entity.SlbSlbServerRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbSlbServerRMapper {
    long countByExample(SlbSlbServerRExample example);

    int deleteByExample(SlbSlbServerRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbSlbServerR record);

    int insertSelective(SlbSlbServerR record);

    SlbSlbServerR selectOneByExample(SlbSlbServerRExample example);

    SlbSlbServerR selectOneByExampleSelective(@Param("example") SlbSlbServerRExample example, @Param("selective") SlbSlbServerR.Column ... selective);

    List<SlbSlbServerR> selectByExampleSelective(@Param("example") SlbSlbServerRExample example, @Param("selective") SlbSlbServerR.Column ... selective);

    List<SlbSlbServerR> selectByExample(SlbSlbServerRExample example);

    SlbSlbServerR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbSlbServerR.Column ... selective);

    SlbSlbServerR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbSlbServerR record, @Param("example") SlbSlbServerRExample example);

    int updateByExample(@Param("record") SlbSlbServerR record, @Param("example") SlbSlbServerRExample example);

    int updateByPrimaryKeySelective(SlbSlbServerR record);

    int updateByPrimaryKey(SlbSlbServerR record);

    int upsert(SlbSlbServerR record);

    int upsertSelective(SlbSlbServerR record);

    /*Self defined queries*/
    List<SlbSlbServerR> findAllBySlbOfflineVersion();

    int batchInsert(List<SlbSlbServerR> slbSlbServerRS);

    int batchInsertIncludeId(List<SlbSlbServerR> slbSlbServerRS);

    int batchUpdate(List<SlbSlbServerR> slbSlbServerRS);

    int batchDelete(List<SlbSlbServerR> slbSlbServerRS);

    /*End */
}