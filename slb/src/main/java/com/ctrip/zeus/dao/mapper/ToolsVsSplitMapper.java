package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ToolsVsSplit;
import com.ctrip.zeus.dao.entity.ToolsVsSplitExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ToolsVsSplitMapper {
    long countByExample(ToolsVsSplitExample example);

    int deleteByExample(ToolsVsSplitExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ToolsVsSplit record);

    int insertSelective(ToolsVsSplit record);

    ToolsVsSplit selectOneByExample(ToolsVsSplitExample example);

    ToolsVsSplit selectOneByExampleSelective(@Param("example") ToolsVsSplitExample example, @Param("selective") ToolsVsSplit.Column ... selective);

    ToolsVsSplit selectOneByExampleWithBLOBs(ToolsVsSplitExample example);

    List<ToolsVsSplit> selectByExampleSelective(@Param("example") ToolsVsSplitExample example, @Param("selective") ToolsVsSplit.Column ... selective);

    List<ToolsVsSplit> selectByExampleWithBLOBs(ToolsVsSplitExample example);

    List<ToolsVsSplit> selectByExample(ToolsVsSplitExample example);

    ToolsVsSplit selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ToolsVsSplit.Column ... selective);

    ToolsVsSplit selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ToolsVsSplit record, @Param("example") ToolsVsSplitExample example);

    int updateByExampleWithBLOBs(@Param("record") ToolsVsSplit record, @Param("example") ToolsVsSplitExample example);

    int updateByExample(@Param("record") ToolsVsSplit record, @Param("example") ToolsVsSplitExample example);

    int updateByPrimaryKeySelective(ToolsVsSplit record);

    int updateByPrimaryKeyWithBLOBs(ToolsVsSplit record);

    int updateByPrimaryKey(ToolsVsSplit record);

    int upsert(ToolsVsSplit record);

    int upsertSelective(ToolsVsSplit record);

    int upsertWithBLOBs(ToolsVsSplit record);
}