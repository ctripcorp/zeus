package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbSlb;
import com.ctrip.zeus.dao.entity.SlbSlbExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbSlbMapper {
    long countByExample(SlbSlbExample example);

    int deleteByExample(SlbSlbExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbSlb record);

    int insertSelective(SlbSlb record);

    SlbSlb selectOneByExample(SlbSlbExample example);

    SlbSlb selectOneByExampleSelective(@Param("example") SlbSlbExample example, @Param("selective") SlbSlb.Column ... selective);

    SlbSlb selectOneByExampleWithBLOBs(SlbSlbExample example);

    List<SlbSlb> selectByExampleSelective(@Param("example") SlbSlbExample example, @Param("selective") SlbSlb.Column ... selective);

    List<SlbSlb> selectByExampleWithBLOBs(SlbSlbExample example);

    List<SlbSlb> selectByExample(SlbSlbExample example);

    SlbSlb selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbSlb.Column ... selective);

    SlbSlb selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbSlb record, @Param("example") SlbSlbExample example);

    int updateByExampleWithBLOBs(@Param("record") SlbSlb record, @Param("example") SlbSlbExample example);

    int updateByExample(@Param("record") SlbSlb record, @Param("example") SlbSlbExample example);

    int updateByPrimaryKeySelective(SlbSlb record);

    int updateByPrimaryKeyWithBLOBs(SlbSlb record);

    int updateByPrimaryKey(SlbSlb record);

    int upsert(SlbSlb record);

    int upsertSelective(SlbSlb record);

    int upsertWithBLOBs(SlbSlb record);


    /*Self Defined*/
    int batchInsertIncludeId(List<SlbSlb> records);

    int insertIncludeId(SlbSlb record);
    /*Self Defined*/
}