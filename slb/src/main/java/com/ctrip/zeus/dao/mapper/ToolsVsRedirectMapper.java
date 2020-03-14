package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ToolsVsRedirect;
import com.ctrip.zeus.dao.entity.ToolsVsRedirectExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ToolsVsRedirectMapper {
    long countByExample(ToolsVsRedirectExample example);

    int deleteByExample(ToolsVsRedirectExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ToolsVsRedirect record);

    int insertSelective(ToolsVsRedirect record);

    ToolsVsRedirect selectOneByExample(ToolsVsRedirectExample example);

    ToolsVsRedirect selectOneByExampleSelective(@Param("example") ToolsVsRedirectExample example, @Param("selective") ToolsVsRedirect.Column ... selective);

    ToolsVsRedirect selectOneByExampleWithBLOBs(ToolsVsRedirectExample example);

    List<ToolsVsRedirect> selectByExampleSelective(@Param("example") ToolsVsRedirectExample example, @Param("selective") ToolsVsRedirect.Column ... selective);

    List<ToolsVsRedirect> selectByExampleWithBLOBs(ToolsVsRedirectExample example);

    List<ToolsVsRedirect> selectByExample(ToolsVsRedirectExample example);

    ToolsVsRedirect selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ToolsVsRedirect.Column ... selective);

    ToolsVsRedirect selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ToolsVsRedirect record, @Param("example") ToolsVsRedirectExample example);

    int updateByExampleWithBLOBs(@Param("record") ToolsVsRedirect record, @Param("example") ToolsVsRedirectExample example);

    int updateByExample(@Param("record") ToolsVsRedirect record, @Param("example") ToolsVsRedirectExample example);

    int updateByPrimaryKeySelective(ToolsVsRedirect record);

    int updateByPrimaryKeyWithBLOBs(ToolsVsRedirect record);

    int updateByPrimaryKey(ToolsVsRedirect record);

    int upsert(ToolsVsRedirect record);

    int upsertSelective(ToolsVsRedirect record);

    int upsertWithBLOBs(ToolsVsRedirect record);
}