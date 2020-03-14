package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.NginxConf;
import com.ctrip.zeus.dao.entity.NginxConfExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NginxConfMapper {
    long countByExample(NginxConfExample example);

    int deleteByExample(NginxConfExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NginxConf record);

    int insertSelective(NginxConf record);

    NginxConf selectOneByExample(NginxConfExample example);

    NginxConf selectOneByExampleSelective(@Param("example") NginxConfExample example, @Param("selective") NginxConf.Column ... selective);

    NginxConf selectOneByExampleWithBLOBs(NginxConfExample example);

    List<NginxConf> selectByExampleSelective(@Param("example") NginxConfExample example, @Param("selective") NginxConf.Column ... selective);

    List<NginxConf> selectByExampleWithBLOBs(NginxConfExample example);

    List<NginxConf> selectByExample(NginxConfExample example);

    NginxConf selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") NginxConf.Column ... selective);

    NginxConf selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NginxConf record, @Param("example") NginxConfExample example);

    int updateByExampleWithBLOBs(@Param("record") NginxConf record, @Param("example") NginxConfExample example);

    int updateByExample(@Param("record") NginxConf record, @Param("example") NginxConfExample example);

    int updateByPrimaryKeySelective(NginxConf record);

    int updateByPrimaryKeyWithBLOBs(NginxConf record);

    int updateByPrimaryKey(NginxConf record);

    int upsert(NginxConf record);

    int upsertSelective(NginxConf record);

    int upsertWithBLOBs(NginxConf record);
}