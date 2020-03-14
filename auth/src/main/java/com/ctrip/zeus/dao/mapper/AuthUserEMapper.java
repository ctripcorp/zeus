package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.AuthUserE;
import com.ctrip.zeus.dao.entity.AuthUserEExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuthUserEMapper {
    long countByExample(AuthUserEExample example);

    int deleteByExample(AuthUserEExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AuthUserE record);

    int insertSelective(AuthUserE record);

    AuthUserE selectOneByExample(AuthUserEExample example);

    AuthUserE selectOneByExampleSelective(@Param("example") AuthUserEExample example, @Param("selective") AuthUserE.Column ... selective);

    List<AuthUserE> selectByExampleSelective(@Param("example") AuthUserEExample example, @Param("selective") AuthUserE.Column ... selective);

    List<AuthUserE> selectByExample(AuthUserEExample example);

    AuthUserE selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") AuthUserE.Column ... selective);

    AuthUserE selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AuthUserE record, @Param("example") AuthUserEExample example);

    int updateByExample(@Param("record") AuthUserE record, @Param("example") AuthUserEExample example);

    int updateByPrimaryKeySelective(AuthUserE record);

    int updateByPrimaryKey(AuthUserE record);

    int upsert(AuthUserE record);

    int upsertSelective(AuthUserE record);
}