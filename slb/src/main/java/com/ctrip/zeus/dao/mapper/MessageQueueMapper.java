package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.MessageQueue;
import com.ctrip.zeus.dao.entity.MessageQueueExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageQueueMapper {
    long countByExample(MessageQueueExample example);

    int deleteByExample(MessageQueueExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MessageQueue record);

    int insertSelective(MessageQueue record);

    MessageQueue selectOneByExample(MessageQueueExample example);

    MessageQueue selectOneByExampleSelective(@Param("example") MessageQueueExample example, @Param("selective") MessageQueue.Column ... selective);

    List<MessageQueue> selectByExampleSelective(@Param("example") MessageQueueExample example, @Param("selective") MessageQueue.Column ... selective);

    List<MessageQueue> selectByExample(MessageQueueExample example);

    MessageQueue selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") MessageQueue.Column ... selective);

    MessageQueue selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MessageQueue record, @Param("example") MessageQueueExample example);

    int updateByExample(@Param("record") MessageQueue record, @Param("example") MessageQueueExample example);

    int updateByPrimaryKeySelective(MessageQueue record);

    int updateByPrimaryKey(MessageQueue record);

    int upsert(MessageQueue record);

    int upsertSelective(MessageQueue record);

    /*Self defined*/
    int updateById(List<MessageQueue> list);
    /*Self defined*/
}