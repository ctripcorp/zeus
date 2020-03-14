package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ToolsVsMerge;
import com.ctrip.zeus.dao.entity.ToolsVsMergeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ToolsVsMergeMapper {
    long countByExample(ToolsVsMergeExample example);

    int deleteByExample(ToolsVsMergeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ToolsVsMerge record);

    int insertSelective(ToolsVsMerge record);

    ToolsVsMerge selectOneByExample(ToolsVsMergeExample example);

    ToolsVsMerge selectOneByExampleSelective(@Param("example") ToolsVsMergeExample example, @Param("selective") ToolsVsMerge.Column ... selective);

    ToolsVsMerge selectOneByExampleWithBLOBs(ToolsVsMergeExample example);

    List<ToolsVsMerge> selectByExampleSelective(@Param("example") ToolsVsMergeExample example, @Param("selective") ToolsVsMerge.Column ... selective);

    List<ToolsVsMerge> selectByExampleWithBLOBs(ToolsVsMergeExample example);

    List<ToolsVsMerge> selectByExample(ToolsVsMergeExample example);

    ToolsVsMerge selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ToolsVsMerge.Column ... selective);

    ToolsVsMerge selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ToolsVsMerge record, @Param("example") ToolsVsMergeExample example);

    int updateByExampleWithBLOBs(@Param("record") ToolsVsMerge record, @Param("example") ToolsVsMergeExample example);

    int updateByExample(@Param("record") ToolsVsMerge record, @Param("example") ToolsVsMergeExample example);

    int updateByPrimaryKeySelective(ToolsVsMerge record);

    int updateByPrimaryKeyWithBLOBs(ToolsVsMerge record);

    int updateByPrimaryKey(ToolsVsMerge record);

    int upsert(ToolsVsMerge record);

    int upsertSelective(ToolsVsMerge record);

    int upsertWithBLOBs(ToolsVsMerge record);
}