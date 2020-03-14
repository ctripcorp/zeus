package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.AuthUserRoleR;
import com.ctrip.zeus.dao.entity.AuthUserRoleRExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuthUserRoleRMapper {
    long countByExample(AuthUserRoleRExample example);

    int deleteByExample(AuthUserRoleRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AuthUserRoleR record);

    int insertSelective(AuthUserRoleR record);

    AuthUserRoleR selectOneByExample(AuthUserRoleRExample example);

    AuthUserRoleR selectOneByExampleSelective(@Param("example") AuthUserRoleRExample example, @Param("selective") AuthUserRoleR.Column ... selective);

    List<AuthUserRoleR> selectByExampleSelective(@Param("example") AuthUserRoleRExample example, @Param("selective") AuthUserRoleR.Column ... selective);

    List<AuthUserRoleR> selectByExample(AuthUserRoleRExample example);

    AuthUserRoleR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") AuthUserRoleR.Column ... selective);

    AuthUserRoleR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AuthUserRoleR record, @Param("example") AuthUserRoleRExample example);

    int updateByExample(@Param("record") AuthUserRoleR record, @Param("example") AuthUserRoleRExample example);

    int updateByPrimaryKeySelective(AuthUserRoleR record);

    int updateByPrimaryKey(AuthUserRoleR record);

    int upsert(AuthUserRoleR record);

    int upsertSelective(AuthUserRoleR record);
}