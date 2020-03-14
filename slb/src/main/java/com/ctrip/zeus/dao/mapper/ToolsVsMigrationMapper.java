package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ToolsVsMigration;
import com.ctrip.zeus.dao.entity.ToolsVsMigrationExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ToolsVsMigrationMapper {
    long countByExample(ToolsVsMigrationExample example);

    int deleteByExample(ToolsVsMigrationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ToolsVsMigration record);

    int insertSelective(ToolsVsMigration record);

    ToolsVsMigration selectOneByExample(ToolsVsMigrationExample example);

    ToolsVsMigration selectOneByExampleSelective(@Param("example") ToolsVsMigrationExample example, @Param("selective") ToolsVsMigration.Column ... selective);

    ToolsVsMigration selectOneByExampleWithBLOBs(ToolsVsMigrationExample example);

    List<ToolsVsMigration> selectByExampleSelective(@Param("example") ToolsVsMigrationExample example, @Param("selective") ToolsVsMigration.Column ... selective);

    List<ToolsVsMigration> selectByExampleWithBLOBs(ToolsVsMigrationExample example);

    List<ToolsVsMigration> selectByExample(ToolsVsMigrationExample example);

    ToolsVsMigration selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ToolsVsMigration.Column ... selective);

    ToolsVsMigration selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ToolsVsMigration record, @Param("example") ToolsVsMigrationExample example);

    int updateByExampleWithBLOBs(@Param("record") ToolsVsMigration record, @Param("example") ToolsVsMigrationExample example);

    int updateByExample(@Param("record") ToolsVsMigration record, @Param("example") ToolsVsMigrationExample example);

    int updateByPrimaryKeySelective(ToolsVsMigration record);

    int updateByPrimaryKeyWithBLOBs(ToolsVsMigration record);

    int updateByPrimaryKey(ToolsVsMigration record);

    int upsert(ToolsVsMigration record);

    int upsertSelective(ToolsVsMigration record);

    int upsertWithBLOBs(ToolsVsMigration record);
}