package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbArchiveDr;
import com.ctrip.zeus.dao.entity.SlbArchiveDrExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbArchiveDrMapper {
    long countByExample(SlbArchiveDrExample example);

    int deleteByExample(SlbArchiveDrExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbArchiveDr record);

    int insertSelective(SlbArchiveDr record);

    SlbArchiveDr selectOneByExample(SlbArchiveDrExample example);

    SlbArchiveDr selectOneByExampleSelective(@Param("example") SlbArchiveDrExample example, @Param("selective") SlbArchiveDr.Column ... selective);

    SlbArchiveDr selectOneByExampleWithBLOBs(SlbArchiveDrExample example);

    List<SlbArchiveDr> selectByExampleSelective(@Param("example") SlbArchiveDrExample example, @Param("selective") SlbArchiveDr.Column ... selective);

    List<SlbArchiveDr> selectByExampleWithBLOBs(SlbArchiveDrExample example);

    List<SlbArchiveDr> selectByExample(SlbArchiveDrExample example);

    SlbArchiveDr selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbArchiveDr.Column ... selective);

    SlbArchiveDr selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbArchiveDr record, @Param("example") SlbArchiveDrExample example);

    int updateByExampleWithBLOBs(@Param("record") SlbArchiveDr record, @Param("example") SlbArchiveDrExample example);

    int updateByExample(@Param("record") SlbArchiveDr record, @Param("example") SlbArchiveDrExample example);

    int updateByPrimaryKeySelective(SlbArchiveDr record);

    int updateByPrimaryKeyWithBLOBs(SlbArchiveDr record);

    int updateByPrimaryKey(SlbArchiveDr record);

    int upsert(SlbArchiveDr record);

    int upsertSelective(SlbArchiveDr record);

    int upsertWithBLOBs(SlbArchiveDr record);

    /* Self Defined*/
    List<SlbArchiveDr> findAllByIdVersion(@Param("idVersionArray") String[] idVersionArray);

    List<SlbArchiveDr> findVersionizedByIds(@Param("ids") List<Long> ids);

    List<SlbArchiveDr> findAllByVsIds(@Param("ids") List<Long> ids);

    int batchInsertIncludeId(List<SlbArchiveDr> records);

    List<SlbArchiveDr> concatSelect(@Param("concats") String[] concats);
    /* Self Defined*/
}