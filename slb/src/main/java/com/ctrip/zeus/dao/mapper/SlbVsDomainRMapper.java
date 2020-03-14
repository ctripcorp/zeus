package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbVsDomainR;
import com.ctrip.zeus.dao.entity.SlbVsDomainRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbVsDomainRMapper {
    long countByExample(SlbVsDomainRExample example);

    int deleteByExample(SlbVsDomainRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbVsDomainR record);

    int insertSelective(SlbVsDomainR record);

    SlbVsDomainR selectOneByExample(SlbVsDomainRExample example);

    SlbVsDomainR selectOneByExampleSelective(@Param("example") SlbVsDomainRExample example, @Param("selective") SlbVsDomainR.Column ... selective);

    List<SlbVsDomainR> selectByExampleSelective(@Param("example") SlbVsDomainRExample example, @Param("selective") SlbVsDomainR.Column ... selective);

    List<SlbVsDomainR> selectByExample(SlbVsDomainRExample example);

    SlbVsDomainR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbVsDomainR.Column ... selective);

    SlbVsDomainR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbVsDomainR record, @Param("example") SlbVsDomainRExample example);

    int updateByExample(@Param("record") SlbVsDomainR record, @Param("example") SlbVsDomainRExample example);

    int updateByPrimaryKeySelective(SlbVsDomainR record);

    int updateByPrimaryKey(SlbVsDomainR record);

    int upsert(SlbVsDomainR record);

    int upsertSelective(SlbVsDomainR record);

    /*Self defined*/
    int batchUpdate(List<SlbVsDomainR> records);

    int batchInsert(List<SlbVsDomainR> records);

    int batchDelete(List<SlbVsDomainR> records);

    int batchInsertIncludeId(List<SlbVsDomainR> records);
    /*Self defined*/
}