package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbServerStatus;
import com.ctrip.zeus.dao.entity.SlbServerStatusExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbServerStatusMapper {
    long countByExample(SlbServerStatusExample example);

    int deleteByExample(SlbServerStatusExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbServerStatus record);

    int insertSelective(SlbServerStatus record);

    SlbServerStatus selectOneByExample(SlbServerStatusExample example);

    SlbServerStatus selectOneByExampleSelective(@Param("example") SlbServerStatusExample example, @Param("selective") SlbServerStatus.Column ... selective);

    List<SlbServerStatus> selectByExampleSelective(@Param("example") SlbServerStatusExample example, @Param("selective") SlbServerStatus.Column ... selective);

    List<SlbServerStatus> selectByExample(SlbServerStatusExample example);

    SlbServerStatus selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbServerStatus.Column ... selective);

    SlbServerStatus selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbServerStatus record, @Param("example") SlbServerStatusExample example);

    int updateByExample(@Param("record") SlbServerStatus record, @Param("example") SlbServerStatusExample example);

    int updateByPrimaryKeySelective(SlbServerStatus record);

    int updateByPrimaryKey(SlbServerStatus record);

    int upsert(SlbServerStatus record);

    int upsertSelective(SlbServerStatus record);

    /*Self Defined*/
    int insertUpdate(SlbServerStatus record);

    int batchInsertIncludeId(List<SlbServerStatus> records);
    /*Self Defined*/
}