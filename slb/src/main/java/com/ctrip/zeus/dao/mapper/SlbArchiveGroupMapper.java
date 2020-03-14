package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbArchiveGroup;
import com.ctrip.zeus.dao.entity.SlbArchiveGroupExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbArchiveGroupMapper {
    long countByExample(SlbArchiveGroupExample example);

    int deleteByExample(SlbArchiveGroupExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbArchiveGroup record);

    int insertSelective(SlbArchiveGroup record);

    SlbArchiveGroup selectOneByExample(SlbArchiveGroupExample example);

    SlbArchiveGroup selectOneByExampleSelective(@Param("example") SlbArchiveGroupExample example, @Param("selective") SlbArchiveGroup.Column ... selective);

    SlbArchiveGroup selectOneByExampleWithBLOBs(SlbArchiveGroupExample example);

    List<SlbArchiveGroup> selectByExampleSelective(@Param("example") SlbArchiveGroupExample example, @Param("selective") SlbArchiveGroup.Column ... selective);

    List<SlbArchiveGroup> selectByExampleWithBLOBs(SlbArchiveGroupExample example);

    List<SlbArchiveGroup> selectByExample(SlbArchiveGroupExample example);

    SlbArchiveGroup selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbArchiveGroup.Column ... selective);

    SlbArchiveGroup selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbArchiveGroup record, @Param("example") SlbArchiveGroupExample example);

    int updateByExampleWithBLOBs(@Param("record") SlbArchiveGroup record, @Param("example") SlbArchiveGroupExample example);

    int updateByExample(@Param("record") SlbArchiveGroup record, @Param("example") SlbArchiveGroupExample example);

    int updateByPrimaryKeySelective(SlbArchiveGroup record);

    int updateByPrimaryKeyWithBLOBs(SlbArchiveGroup record);

    int updateByPrimaryKey(SlbArchiveGroup record);

    int upsert(SlbArchiveGroup record);

    int upsertSelective(SlbArchiveGroup record);

    int upsertWithBLOBs(SlbArchiveGroup record);

    /*Self Defined*/
    List<SlbArchiveGroup> findAllByVsIds(@Param("ids") List<Long> ids);

    List<SlbArchiveGroup> findAllByIdVersion(@Param("hashes") List<Integer> hashes, @Param("idVersionArray") List<String> idVersionArray);

    /**
     * find online and offline version of group archive
     * @param ids
     * @return
     */
    List<SlbArchiveGroup> findVersionizedByIds(List<Long> ids);

    int batchInsertIncludeId(List<SlbArchiveGroup> lists);
    /*Self Defined*/
}