package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.NginxConfSlb;
import com.ctrip.zeus.dao.entity.NginxConfSlbExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NginxConfSlbMapper {
    long countByExample(NginxConfSlbExample example);

    int deleteByExample(NginxConfSlbExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NginxConfSlb record);

    int insertSelective(NginxConfSlb record);

    NginxConfSlb selectOneByExample(NginxConfSlbExample example);

    NginxConfSlb selectOneByExampleSelective(@Param("example") NginxConfSlbExample example, @Param("selective") NginxConfSlb.Column ... selective);

    NginxConfSlb selectOneByExampleWithBLOBs(NginxConfSlbExample example);

    List<NginxConfSlb> selectByExampleSelective(@Param("example") NginxConfSlbExample example, @Param("selective") NginxConfSlb.Column ... selective);

    List<NginxConfSlb> selectByExampleWithBLOBs(NginxConfSlbExample example);

    List<NginxConfSlb> selectByExample(NginxConfSlbExample example);

    NginxConfSlb selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") NginxConfSlb.Column ... selective);

    NginxConfSlb selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NginxConfSlb record, @Param("example") NginxConfSlbExample example);

    int updateByExampleWithBLOBs(@Param("record") NginxConfSlb record, @Param("example") NginxConfSlbExample example);

    int updateByExample(@Param("record") NginxConfSlb record, @Param("example") NginxConfSlbExample example);

    int updateByPrimaryKeySelective(NginxConfSlb record);

    int updateByPrimaryKeyWithBLOBs(NginxConfSlb record);

    int updateByPrimaryKey(NginxConfSlb record);

    int upsert(NginxConfSlb record);

    int upsertSelective(NginxConfSlb record);

    int upsertWithBLOBs(NginxConfSlb record);
}