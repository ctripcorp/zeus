package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.UnhealthyAlertItem;
import com.ctrip.zeus.dao.entity.UnhealthyAlertItemExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UnhealthyAlertItemMapper {
    long countByExample(UnhealthyAlertItemExample example);

    int deleteByExample(UnhealthyAlertItemExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UnhealthyAlertItem record);

    int insertSelective(UnhealthyAlertItem record);

    UnhealthyAlertItem selectOneByExample(UnhealthyAlertItemExample example);

    UnhealthyAlertItem selectOneByExampleSelective(@Param("example") UnhealthyAlertItemExample example, @Param("selective") UnhealthyAlertItem.Column... selective);

    UnhealthyAlertItem selectOneByExampleWithBLOBs(UnhealthyAlertItemExample example);

    List<UnhealthyAlertItem> selectByExampleSelective(@Param("example") UnhealthyAlertItemExample example, @Param("selective") UnhealthyAlertItem.Column... selective);

    List<UnhealthyAlertItem> selectByExampleWithBLOBs(UnhealthyAlertItemExample example);

    List<UnhealthyAlertItem> selectByExample(UnhealthyAlertItemExample example);

    UnhealthyAlertItem selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") UnhealthyAlertItem.Column... selective);

    UnhealthyAlertItem selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UnhealthyAlertItem record, @Param("example") UnhealthyAlertItemExample example);

    int updateByExampleWithBLOBs(@Param("record") UnhealthyAlertItem record, @Param("example") UnhealthyAlertItemExample example);

    int updateByExample(@Param("record") UnhealthyAlertItem record, @Param("example") UnhealthyAlertItemExample example);

    int updateByPrimaryKeySelective(UnhealthyAlertItem record);

    int updateByPrimaryKeyWithBLOBs(UnhealthyAlertItem record);

    int updateByPrimaryKey(UnhealthyAlertItem record);

    int upsert(UnhealthyAlertItem record);

    int upsertSelective(UnhealthyAlertItem record);

    int upsertWithBLOBs(UnhealthyAlertItem record);

    /* manually added */
    int insertIdIncluded(UnhealthyAlertItem record);

    int batchInsertIdIncluded(List<UnhealthyAlertItem> records);

    int batchInsert(List<UnhealthyAlertItem> records);

    int batchUpdate(List<UnhealthyAlertItem> records);
    /* manually added */
}