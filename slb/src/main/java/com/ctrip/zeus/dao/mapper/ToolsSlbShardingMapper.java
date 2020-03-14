package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ToolsSlbSharding;
import com.ctrip.zeus.dao.entity.ToolsSlbShardingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ToolsSlbShardingMapper {
    long countByExample(ToolsSlbShardingExample example);

    int deleteByExample(ToolsSlbShardingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ToolsSlbSharding record);

    int insertSelective(ToolsSlbSharding record);

    ToolsSlbSharding selectOneByExample(ToolsSlbShardingExample example);

    ToolsSlbSharding selectOneByExampleSelective(@Param("example") ToolsSlbShardingExample example, @Param("selective") ToolsSlbSharding.Column ... selective);

    ToolsSlbSharding selectOneByExampleWithBLOBs(ToolsSlbShardingExample example);

    List<ToolsSlbSharding> selectByExampleSelective(@Param("example") ToolsSlbShardingExample example, @Param("selective") ToolsSlbSharding.Column ... selective);

    List<ToolsSlbSharding> selectByExampleWithBLOBs(ToolsSlbShardingExample example);

    List<ToolsSlbSharding> selectByExample(ToolsSlbShardingExample example);

    ToolsSlbSharding selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ToolsSlbSharding.Column ... selective);

    ToolsSlbSharding selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ToolsSlbSharding record, @Param("example") ToolsSlbShardingExample example);

    int updateByExampleWithBLOBs(@Param("record") ToolsSlbSharding record, @Param("example") ToolsSlbShardingExample example);

    int updateByExample(@Param("record") ToolsSlbSharding record, @Param("example") ToolsSlbShardingExample example);

    int updateByPrimaryKeySelective(ToolsSlbSharding record);

    int updateByPrimaryKeyWithBLOBs(ToolsSlbSharding record);

    int updateByPrimaryKey(ToolsSlbSharding record);

    int upsert(ToolsSlbSharding record);

    int upsertSelective(ToolsSlbSharding record);

    int upsertWithBLOBs(ToolsSlbSharding record);
}