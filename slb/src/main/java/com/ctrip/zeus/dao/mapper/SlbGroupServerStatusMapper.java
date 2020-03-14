package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbGroupServerStatus;
import com.ctrip.zeus.dao.entity.SlbGroupServerStatusExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SlbGroupServerStatusMapper {
    long countByExample(SlbGroupServerStatusExample example);

    int deleteByExample(SlbGroupServerStatusExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbGroupServerStatus record);

    int insertSelective(SlbGroupServerStatus record);

    SlbGroupServerStatus selectOneByExample(SlbGroupServerStatusExample example);

    SlbGroupServerStatus selectOneByExampleSelective(@Param("example") SlbGroupServerStatusExample example, @Param("selective") SlbGroupServerStatus.Column ... selective);

    List<SlbGroupServerStatus> selectByExampleSelective(@Param("example") SlbGroupServerStatusExample example, @Param("selective") SlbGroupServerStatus.Column ... selective);

    List<SlbGroupServerStatus> selectByExample(SlbGroupServerStatusExample example);

    SlbGroupServerStatus selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbGroupServerStatus.Column ... selective);

    SlbGroupServerStatus selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbGroupServerStatus record, @Param("example") SlbGroupServerStatusExample example);

    int updateByExample(@Param("record") SlbGroupServerStatus record, @Param("example") SlbGroupServerStatusExample example);

    int updateByPrimaryKeySelective(SlbGroupServerStatus record);

    int updateByPrimaryKey(SlbGroupServerStatus record);

    int upsert(SlbGroupServerStatus record);

    int upsertSelective(SlbGroupServerStatus record);

    /*Self Defined*/
    int updateStatus(@Param("record") SlbGroupServerStatus data, @Param("reset") int reset);

    int batchUpdateStatus(List<Map<String, Object>> records);

    int insertUpdate(SlbGroupServerStatus record);

    int batchInsertIncludeId(List<SlbGroupServerStatus> records);

    List<SlbGroupServerStatus> concatSelect(@Param("concats") String[] concats);
    /*Self Defined*/
}