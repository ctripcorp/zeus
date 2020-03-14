package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbArchiveSlb;
import com.ctrip.zeus.dao.entity.SlbArchiveSlbExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbArchiveSlbMapper {
    long countByExample(SlbArchiveSlbExample example);

    int deleteByExample(SlbArchiveSlbExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbArchiveSlb record);

    int insertSelective(SlbArchiveSlb record);

    SlbArchiveSlb selectOneByExample(SlbArchiveSlbExample example);

    SlbArchiveSlb selectOneByExampleSelective(@Param("example") SlbArchiveSlbExample example, @Param("selective") SlbArchiveSlb.Column ... selective);

    SlbArchiveSlb selectOneByExampleWithBLOBs(SlbArchiveSlbExample example);

    List<SlbArchiveSlb> selectByExampleSelective(@Param("example") SlbArchiveSlbExample example, @Param("selective") SlbArchiveSlb.Column ... selective);

    List<SlbArchiveSlb> selectByExampleWithBLOBs(SlbArchiveSlbExample example);

    List<SlbArchiveSlb> selectByExample(SlbArchiveSlbExample example);

    SlbArchiveSlb selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbArchiveSlb.Column ... selective);

    SlbArchiveSlb selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbArchiveSlb record, @Param("example") SlbArchiveSlbExample example);

    int updateByExampleWithBLOBs(@Param("record") SlbArchiveSlb record, @Param("example") SlbArchiveSlbExample example);

    int updateByExample(@Param("record") SlbArchiveSlb record, @Param("example") SlbArchiveSlbExample example);

    int updateByPrimaryKeySelective(SlbArchiveSlb record);

    int updateByPrimaryKeyWithBLOBs(SlbArchiveSlb record);

    int updateByPrimaryKey(SlbArchiveSlb record);

    int upsert(SlbArchiveSlb record);

    int upsertSelective(SlbArchiveSlb record);

    int upsertWithBLOBs(SlbArchiveSlb record);

    /*Self defined query*/
    List<SlbArchiveSlb> findVersionizedByIds(List<Long> items);

    List<SlbArchiveSlb> findAllByIdVersion(@Param("hashes") List<Integer> hashes, @Param("id_version_array") List<String> id_version_array);

    int batchInsertIncludeId(List<SlbArchiveSlb> records);
    /*Self defined query*/
}