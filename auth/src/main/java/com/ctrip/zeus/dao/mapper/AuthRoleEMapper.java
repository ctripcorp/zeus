package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.AuthRoleE;
import com.ctrip.zeus.dao.entity.AuthRoleEExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuthRoleEMapper {
    long countByExample(AuthRoleEExample example);

    int deleteByExample(AuthRoleEExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AuthRoleE record);

    int insertSelective(AuthRoleE record);

    AuthRoleE selectOneByExample(AuthRoleEExample example);

    AuthRoleE selectOneByExampleSelective(@Param("example") AuthRoleEExample example, @Param("selective") AuthRoleE.Column ... selective);

    List<AuthRoleE> selectByExampleSelective(@Param("example") AuthRoleEExample example, @Param("selective") AuthRoleE.Column ... selective);

    List<AuthRoleE> selectByExample(AuthRoleEExample example);

    AuthRoleE selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") AuthRoleE.Column ... selective);

    AuthRoleE selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AuthRoleE record, @Param("example") AuthRoleEExample example);

    int updateByExample(@Param("record") AuthRoleE record, @Param("example") AuthRoleEExample example);

    int updateByPrimaryKeySelective(AuthRoleE record);

    int updateByPrimaryKey(AuthRoleE record);

    int upsert(AuthRoleE record);

    int upsertSelective(AuthRoleE record);
}