package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ToolsCertUpgrade;
import com.ctrip.zeus.dao.entity.ToolsCertUpgradeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ToolsCertUpgradeMapper {
    long countByExample(ToolsCertUpgradeExample example);

    int deleteByExample(ToolsCertUpgradeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ToolsCertUpgrade record);

    int insertSelective(ToolsCertUpgrade record);

    ToolsCertUpgrade selectOneByExample(ToolsCertUpgradeExample example);

    ToolsCertUpgrade selectOneByExampleSelective(@Param("example") ToolsCertUpgradeExample example, @Param("selective") ToolsCertUpgrade.Column ... selective);

    ToolsCertUpgrade selectOneByExampleWithBLOBs(ToolsCertUpgradeExample example);

    List<ToolsCertUpgrade> selectByExampleSelective(@Param("example") ToolsCertUpgradeExample example, @Param("selective") ToolsCertUpgrade.Column ... selective);

    List<ToolsCertUpgrade> selectByExampleWithBLOBs(ToolsCertUpgradeExample example);

    List<ToolsCertUpgrade> selectByExample(ToolsCertUpgradeExample example);

    ToolsCertUpgrade selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ToolsCertUpgrade.Column ... selective);

    ToolsCertUpgrade selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ToolsCertUpgrade record, @Param("example") ToolsCertUpgradeExample example);

    int updateByExampleWithBLOBs(@Param("record") ToolsCertUpgrade record, @Param("example") ToolsCertUpgradeExample example);

    int updateByExample(@Param("record") ToolsCertUpgrade record, @Param("example") ToolsCertUpgradeExample example);

    int updateByPrimaryKeySelective(ToolsCertUpgrade record);

    int updateByPrimaryKeyWithBLOBs(ToolsCertUpgrade record);

    int updateByPrimaryKey(ToolsCertUpgrade record);

    int upsert(ToolsCertUpgrade record);

    int upsertSelective(ToolsCertUpgrade record);

    int upsertWithBLOBs(ToolsCertUpgrade record);
}