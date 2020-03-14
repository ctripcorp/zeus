package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.AuthUserPassword;
import com.ctrip.zeus.dao.entity.AuthUserPasswordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthUserPasswordMapper {
    long countByExample(AuthUserPasswordExample example);

    int deleteByExample(AuthUserPasswordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AuthUserPassword record);

    int insertSelective(AuthUserPassword record);

    AuthUserPassword selectOneByExample(AuthUserPasswordExample example);

    AuthUserPassword selectOneByExampleSelective(@Param("example") AuthUserPasswordExample example, @Param("selective") AuthUserPassword.Column ... selective);

    List<AuthUserPassword> selectByExampleSelective(@Param("example") AuthUserPasswordExample example, @Param("selective") AuthUserPassword.Column ... selective);

    List<AuthUserPassword> selectByExample(AuthUserPasswordExample example);

    AuthUserPassword selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") AuthUserPassword.Column ... selective);

    AuthUserPassword selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AuthUserPassword record, @Param("example") AuthUserPasswordExample example);

    int updateByExample(@Param("record") AuthUserPassword record, @Param("example") AuthUserPasswordExample example);

    int updateByPrimaryKeySelective(AuthUserPassword record);

    int updateByPrimaryKey(AuthUserPassword record);

    int upsert(AuthUserPassword record);

    int upsertSelective(AuthUserPassword record);
}