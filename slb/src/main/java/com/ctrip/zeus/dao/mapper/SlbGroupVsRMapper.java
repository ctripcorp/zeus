package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbGroupVsR;
import com.ctrip.zeus.dao.entity.SlbGroupVsRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbGroupVsRMapper {
    long countByExample(SlbGroupVsRExample example);

    int deleteByExample(SlbGroupVsRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbGroupVsR record);

    int insertSelective(SlbGroupVsR record);

    SlbGroupVsR selectOneByExample(SlbGroupVsRExample example);

    SlbGroupVsR selectOneByExampleSelective(@Param("example") SlbGroupVsRExample example, @Param("selective") SlbGroupVsR.Column ... selective);

    SlbGroupVsR selectOneByExampleWithBLOBs(SlbGroupVsRExample example);

    List<SlbGroupVsR> selectByExampleSelective(@Param("example") SlbGroupVsRExample example, @Param("selective") SlbGroupVsR.Column ... selective);

    List<SlbGroupVsR> selectByExampleWithBLOBs(SlbGroupVsRExample example);

    List<SlbGroupVsR> selectByExample(SlbGroupVsRExample example);

    SlbGroupVsR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbGroupVsR.Column ... selective);

    SlbGroupVsR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbGroupVsR record, @Param("example") SlbGroupVsRExample example);

    int updateByExampleWithBLOBs(@Param("record") SlbGroupVsR record, @Param("example") SlbGroupVsRExample example);

    int updateByExample(@Param("record") SlbGroupVsR record, @Param("example") SlbGroupVsRExample example);

    int updateByPrimaryKeySelective(SlbGroupVsR record);

    int updateByPrimaryKeyWithBLOBs(SlbGroupVsR record);

    int updateByPrimaryKey(SlbGroupVsR record);

    int upsert(SlbGroupVsR record);

    int upsertSelective(SlbGroupVsR record);

    int upsertWithBLOBs(SlbGroupVsR record);

    /*Self Defined*/
    List<SlbGroupVsR> findAllByGroupOfflineVersion(List<Long> ids);

    int batchUpdate(List<SlbGroupVsR> records);

    int batchInsert(List<SlbGroupVsR> records);

    int batchInsertIncludeId(List<SlbGroupVsR> records);

    int batchDelete(List<SlbGroupVsR> records);

    List<SlbGroupVsR> concatSelect(@Param("concats") String[] concats);

    List<SlbGroupVsR> findByVsesAndGroupOfflineVersion(@Param("ids") List<Long> ids);
    /*Self Defined*/
}