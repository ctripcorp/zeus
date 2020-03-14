package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.LogOperationLog;
import com.ctrip.zeus.dao.entity.LogOperationLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface LogOperationLogMapper {

    /**
     * TODO Do Not Delete Extra Method. These methods are not auto generated.
     * TODO Do Not Delete Extra Method. These methods are not auto generated.
     * TODO Do Not Delete Extra Method. These methods are not auto generated.
     * @param example
     * @return
     */
    //START EXTRA
    List<Map<String, Object>> countByExampleGroupByTargetId(LogOperationLogExample example);
    //END

    long countByExample(LogOperationLogExample example);

    int deleteByExample(LogOperationLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(LogOperationLog record);

    int insertSelective(LogOperationLog record);

    LogOperationLog selectOneByExample(LogOperationLogExample example);

    LogOperationLog selectOneByExampleSelective(@Param("example") LogOperationLogExample example, @Param("selective") LogOperationLog.Column ... selective);

    List<LogOperationLog> selectByExampleSelective(@Param("example") LogOperationLogExample example, @Param("selective") LogOperationLog.Column ... selective);

    List<LogOperationLog> selectByExample(LogOperationLogExample example);

    LogOperationLog selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") LogOperationLog.Column ... selective);

    LogOperationLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") LogOperationLog record, @Param("example") LogOperationLogExample example);

    int updateByExample(@Param("record") LogOperationLog record, @Param("example") LogOperationLogExample example);

    int updateByPrimaryKeySelective(LogOperationLog record);

    int updateByPrimaryKey(LogOperationLog record);

    int upsert(LogOperationLog record);

    int upsertSelective(LogOperationLog record);
}