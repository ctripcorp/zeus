package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbBuildTicket;
import com.ctrip.zeus.dao.entity.SlbBuildTicketExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbBuildTicketMapper {
    long countByExample(SlbBuildTicketExample example);

    int deleteByExample(SlbBuildTicketExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbBuildTicket record);

    int insertSelective(SlbBuildTicket record);

    SlbBuildTicket selectOneByExample(SlbBuildTicketExample example);

    SlbBuildTicket selectOneByExampleSelective(@Param("example") SlbBuildTicketExample example, @Param("selective") SlbBuildTicket.Column ... selective);

    List<SlbBuildTicket> selectByExampleSelective(@Param("example") SlbBuildTicketExample example, @Param("selective") SlbBuildTicket.Column ... selective);

    List<SlbBuildTicket> selectByExample(SlbBuildTicketExample example);

    SlbBuildTicket selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbBuildTicket.Column ... selective);

    SlbBuildTicket selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbBuildTicket record, @Param("example") SlbBuildTicketExample example);

    int updateByExample(@Param("record") SlbBuildTicket record, @Param("example") SlbBuildTicketExample example);

    int updateByPrimaryKeySelective(SlbBuildTicket record);

    int updateByPrimaryKey(SlbBuildTicket record);

    int upsert(SlbBuildTicket record);

    int upsertSelective(SlbBuildTicket record);

    /*Self Defined*/
    int insertIncludeId(SlbBuildTicket record);

    int batchInsertIncludeId(List<SlbBuildTicket> records);
    /*Self Defined*/
}