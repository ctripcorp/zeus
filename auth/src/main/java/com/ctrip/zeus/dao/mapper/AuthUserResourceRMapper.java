package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.AuthUserResourceR;
import com.ctrip.zeus.dao.entity.AuthUserResourceRExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuthUserResourceRMapper {


    /** Non-automatic generation Code Start **/
    int batchInsert(List<AuthUserResourceR> records);
    /** Non-automatic generation Code END **/

    long countByExample(AuthUserResourceRExample example);

    int deleteByExample(AuthUserResourceRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AuthUserResourceR record);

    int insertSelective(AuthUserResourceR record);

    AuthUserResourceR selectOneByExample(AuthUserResourceRExample example);

    AuthUserResourceR selectOneByExampleSelective(@Param("example") AuthUserResourceRExample example, @Param("selective") AuthUserResourceR.Column ... selective);

    List<AuthUserResourceR> selectByExampleSelective(@Param("example") AuthUserResourceRExample example, @Param("selective") AuthUserResourceR.Column ... selective);

    List<AuthUserResourceR> selectByExample(AuthUserResourceRExample example);

    AuthUserResourceR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") AuthUserResourceR.Column ... selective);

    AuthUserResourceR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AuthUserResourceR record, @Param("example") AuthUserResourceRExample example);

    int updateByExample(@Param("record") AuthUserResourceR record, @Param("example") AuthUserResourceRExample example);

    int updateByPrimaryKeySelective(AuthUserResourceR record);

    int updateByPrimaryKey(AuthUserResourceR record);

    int upsert(AuthUserResourceR record);

    int upsertSelective(AuthUserResourceR record);
}