package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ToolsDnsSwitch;
import com.ctrip.zeus.dao.entity.ToolsDnsSwitchExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ToolsDnsSwitchMapper {
    long countByExample(ToolsDnsSwitchExample example);

    int deleteByExample(ToolsDnsSwitchExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ToolsDnsSwitch record);

    int insertSelective(ToolsDnsSwitch record);

    ToolsDnsSwitch selectOneByExample(ToolsDnsSwitchExample example);

    ToolsDnsSwitch selectOneByExampleSelective(@Param("example") ToolsDnsSwitchExample example, @Param("selective") ToolsDnsSwitch.Column ... selective);

    ToolsDnsSwitch selectOneByExampleWithBLOBs(ToolsDnsSwitchExample example);

    List<ToolsDnsSwitch> selectByExampleSelective(@Param("example") ToolsDnsSwitchExample example, @Param("selective") ToolsDnsSwitch.Column ... selective);

    List<ToolsDnsSwitch> selectByExampleWithBLOBs(ToolsDnsSwitchExample example);

    List<ToolsDnsSwitch> selectByExample(ToolsDnsSwitchExample example);

    ToolsDnsSwitch selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ToolsDnsSwitch.Column ... selective);

    ToolsDnsSwitch selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ToolsDnsSwitch record, @Param("example") ToolsDnsSwitchExample example);

    int updateByExampleWithBLOBs(@Param("record") ToolsDnsSwitch record, @Param("example") ToolsDnsSwitchExample example);

    int updateByExample(@Param("record") ToolsDnsSwitch record, @Param("example") ToolsDnsSwitchExample example);

    int updateByPrimaryKeySelective(ToolsDnsSwitch record);

    int updateByPrimaryKeyWithBLOBs(ToolsDnsSwitch record);

    int updateByPrimaryKey(ToolsDnsSwitch record);

    int upsert(ToolsDnsSwitch record);

    int upsertSelective(ToolsDnsSwitch record);

    int upsertWithBLOBs(ToolsDnsSwitch record);
}