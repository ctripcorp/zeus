package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbVirtualServer;
import com.ctrip.zeus.dao.entity.SlbVirtualServerExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbVirtualServerMapper {
    long countByExample(SlbVirtualServerExample example);

    int deleteByExample(SlbVirtualServerExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbVirtualServer record);

    int insertSelective(SlbVirtualServer record);

    SlbVirtualServer selectOneByExample(SlbVirtualServerExample example);

    SlbVirtualServer selectOneByExampleSelective(@Param("example") SlbVirtualServerExample example, @Param("selective") SlbVirtualServer.Column ... selective);

    List<SlbVirtualServer> selectByExampleSelective(@Param("example") SlbVirtualServerExample example, @Param("selective") SlbVirtualServer.Column ... selective);

    List<SlbVirtualServer> selectByExample(SlbVirtualServerExample example);

    SlbVirtualServer selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbVirtualServer.Column ... selective);

    SlbVirtualServer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbVirtualServer record, @Param("example") SlbVirtualServerExample example);

    int updateByExample(@Param("record") SlbVirtualServer record, @Param("example") SlbVirtualServerExample example);

    int updateByPrimaryKeySelective(SlbVirtualServer record);

    int updateByPrimaryKey(SlbVirtualServer record);

    int upsert(SlbVirtualServer record);

    int upsertSelective(SlbVirtualServer record);
}