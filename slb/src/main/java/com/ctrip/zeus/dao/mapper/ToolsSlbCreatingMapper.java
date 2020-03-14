package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ToolsSlbCreating;
import com.ctrip.zeus.dao.entity.ToolsSlbCreatingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ToolsSlbCreatingMapper {
    long countByExample(ToolsSlbCreatingExample example);

    int deleteByExample(ToolsSlbCreatingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ToolsSlbCreating record);

    int insertSelective(ToolsSlbCreating record);

    ToolsSlbCreating selectOneByExample(ToolsSlbCreatingExample example);

    ToolsSlbCreating selectOneByExampleSelective(@Param("example") ToolsSlbCreatingExample example, @Param("selective") ToolsSlbCreating.Column ... selective);

    ToolsSlbCreating selectOneByExampleWithBLOBs(ToolsSlbCreatingExample example);

    List<ToolsSlbCreating> selectByExampleSelective(@Param("example") ToolsSlbCreatingExample example, @Param("selective") ToolsSlbCreating.Column ... selective);

    List<ToolsSlbCreating> selectByExampleWithBLOBs(ToolsSlbCreatingExample example);

    List<ToolsSlbCreating> selectByExample(ToolsSlbCreatingExample example);

    ToolsSlbCreating selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ToolsSlbCreating.Column ... selective);

    ToolsSlbCreating selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ToolsSlbCreating record, @Param("example") ToolsSlbCreatingExample example);

    int updateByExampleWithBLOBs(@Param("record") ToolsSlbCreating record, @Param("example") ToolsSlbCreatingExample example);

    int updateByExample(@Param("record") ToolsSlbCreating record, @Param("example") ToolsSlbCreatingExample example);

    int updateByPrimaryKeySelective(ToolsSlbCreating record);

    int updateByPrimaryKeyWithBLOBs(ToolsSlbCreating record);

    int updateByPrimaryKey(ToolsSlbCreating record);

    int upsert(ToolsSlbCreating record);

    int upsertSelective(ToolsSlbCreating record);

    int upsertWithBLOBs(ToolsSlbCreating record);
}