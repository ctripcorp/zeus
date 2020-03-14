package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbArchiveVs;
import com.ctrip.zeus.dao.entity.SlbArchiveVsExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbArchiveVsMapper {
    long countByExample(SlbArchiveVsExample example);

    int deleteByExample(SlbArchiveVsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbArchiveVs record);

    int insertSelective(SlbArchiveVs record);

    SlbArchiveVs selectOneByExample(SlbArchiveVsExample example);

    SlbArchiveVs selectOneByExampleSelective(@Param("example") SlbArchiveVsExample example, @Param("selective") SlbArchiveVs.Column ... selective);

    SlbArchiveVs selectOneByExampleWithBLOBs(SlbArchiveVsExample example);

    List<SlbArchiveVs> selectByExampleSelective(@Param("example") SlbArchiveVsExample example, @Param("selective") SlbArchiveVs.Column ... selective);

    List<SlbArchiveVs> selectByExampleWithBLOBs(SlbArchiveVsExample example);

    List<SlbArchiveVs> selectByExample(SlbArchiveVsExample example);

    SlbArchiveVs selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbArchiveVs.Column ... selective);

    SlbArchiveVs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbArchiveVs record, @Param("example") SlbArchiveVsExample example);

    int updateByExampleWithBLOBs(@Param("record") SlbArchiveVs record, @Param("example") SlbArchiveVsExample example);

    int updateByExample(@Param("record") SlbArchiveVs record, @Param("example") SlbArchiveVsExample example);

    int updateByPrimaryKeySelective(SlbArchiveVs record);

    int updateByPrimaryKeyWithBLOBs(SlbArchiveVs record);

    int updateByPrimaryKey(SlbArchiveVs record);

    int upsert(SlbArchiveVs record);

    int upsertSelective(SlbArchiveVs record);

    int upsertWithBLOBs(SlbArchiveVs record);

    /*Self Defined*/
    int batchInsertIncludeId(List<SlbArchiveVs> slbArchiveVs);

    List<SlbArchiveVs> findAllBySlbId(@Param("slbId") Long slbId);

    List<SlbArchiveVs> findVersionizedByIds(List<Long> ids);

    List<SlbArchiveVs> findAllBySlbsAndVsOfflineVersion(@Param("ids") List<Long> ids);

    List<SlbArchiveVs> findAllByIdVersion(@Param("hashes") List<Integer> hashes, @Param("id_version_array") List<String> id_version_array);
    /*Self Defined*/
}