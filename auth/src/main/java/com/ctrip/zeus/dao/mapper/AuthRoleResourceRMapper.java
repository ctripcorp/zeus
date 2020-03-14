package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.AuthRoleResourceR;
import com.ctrip.zeus.dao.entity.AuthRoleResourceRExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuthRoleResourceRMapper {


    /** Non-automatic generation Code Start **/
    int batchInsert(List<AuthRoleResourceR> records);
    /** Non-automatic generation Code END **/

    long countByExample(AuthRoleResourceRExample example);

    int deleteByExample(AuthRoleResourceRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AuthRoleResourceR record);

    int insertSelective(AuthRoleResourceR record);

    AuthRoleResourceR selectOneByExample(AuthRoleResourceRExample example);

    AuthRoleResourceR selectOneByExampleSelective(@Param("example") AuthRoleResourceRExample example, @Param("selective") AuthRoleResourceR.Column ... selective);

    List<AuthRoleResourceR> selectByExampleSelective(@Param("example") AuthRoleResourceRExample example, @Param("selective") AuthRoleResourceR.Column ... selective);

    List<AuthRoleResourceR> selectByExample(AuthRoleResourceRExample example);

    AuthRoleResourceR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") AuthRoleResourceR.Column ... selective);

    AuthRoleResourceR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AuthRoleResourceR record, @Param("example") AuthRoleResourceRExample example);

    int updateByExample(@Param("record") AuthRoleResourceR record, @Param("example") AuthRoleResourceRExample example);

    int updateByPrimaryKeySelective(AuthRoleResourceR record);

    int updateByPrimaryKey(AuthRoleResourceR record);

    int upsert(AuthRoleResourceR record);

    int upsertSelective(AuthRoleResourceR record);
}