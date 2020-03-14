package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.NginxServer;
import com.ctrip.zeus.dao.entity.NginxServerExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NginxServerMapper {
    long countByExample(NginxServerExample example);

    int deleteByExample(NginxServerExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NginxServer record);

    int insertSelective(NginxServer record);

    NginxServer selectOneByExample(NginxServerExample example);

    NginxServer selectOneByExampleSelective(@Param("example") NginxServerExample example, @Param("selective") NginxServer.Column ... selective);

    List<NginxServer> selectByExampleSelective(@Param("example") NginxServerExample example, @Param("selective") NginxServer.Column ... selective);

    List<NginxServer> selectByExample(NginxServerExample example);

    NginxServer selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") NginxServer.Column ... selective);

    NginxServer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NginxServer record, @Param("example") NginxServerExample example);

    int updateByExample(@Param("record") NginxServer record, @Param("example") NginxServerExample example);

    int updateByPrimaryKeySelective(NginxServer record);

    int updateByPrimaryKey(NginxServer record);

    int upsert(NginxServer record);

    int upsertSelective(NginxServer record);

    /*Self Defined*/
    int insertOrUpdate(NginxServer record);
}