package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbConfSlbVersion;
import com.ctrip.zeus.dao.entity.SlbConfSlbVersionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbConfSlbVersionMapper {
    long countByExample(SlbConfSlbVersionExample example);

    int deleteByExample(SlbConfSlbVersionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbConfSlbVersion record);

    int insertSelective(SlbConfSlbVersion record);

    SlbConfSlbVersion selectOneByExample(SlbConfSlbVersionExample example);

    SlbConfSlbVersion selectOneByExampleSelective(@Param("example") SlbConfSlbVersionExample example, @Param("selective") SlbConfSlbVersion.Column ... selective);

    List<SlbConfSlbVersion> selectByExampleSelective(@Param("example") SlbConfSlbVersionExample example, @Param("selective") SlbConfSlbVersion.Column ... selective);

    List<SlbConfSlbVersion> selectByExample(SlbConfSlbVersionExample example);

    SlbConfSlbVersion selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbConfSlbVersion.Column ... selective);

    SlbConfSlbVersion selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbConfSlbVersion record, @Param("example") SlbConfSlbVersionExample example);

    int updateByExample(@Param("record") SlbConfSlbVersion record, @Param("example") SlbConfSlbVersionExample example);

    int updateByPrimaryKeySelective(SlbConfSlbVersion record);

    int updateByPrimaryKey(SlbConfSlbVersion record);

    int upsert(SlbConfSlbVersion record);

    int upsertSelective(SlbConfSlbVersion record);

    /*Self Definded*/
    int batchInsertIncludeId(List<SlbConfSlbVersion> records);
    /*Self Definded*/
}