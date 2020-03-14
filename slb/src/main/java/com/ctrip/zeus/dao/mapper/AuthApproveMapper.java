package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.AuthApprove;
import com.ctrip.zeus.dao.entity.AuthApproveExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthApproveMapper {
    long countByExample(AuthApproveExample example);

    int deleteByExample(AuthApproveExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AuthApprove record);

    int insertSelective(AuthApprove record);

    AuthApprove selectOneByExample(AuthApproveExample example);

    AuthApprove selectOneByExampleSelective(@Param("example") AuthApproveExample example, @Param("selective") AuthApprove.Column ... selective);

    AuthApprove selectOneByExampleWithBLOBs(AuthApproveExample example);

    List<AuthApprove> selectByExampleSelective(@Param("example") AuthApproveExample example, @Param("selective") AuthApprove.Column ... selective);

    List<AuthApprove> selectByExampleWithBLOBs(AuthApproveExample example);

    List<AuthApprove> selectByExample(AuthApproveExample example);

    AuthApprove selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") AuthApprove.Column ... selective);

    AuthApprove selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AuthApprove record, @Param("example") AuthApproveExample example);

    int updateByExampleWithBLOBs(@Param("record") AuthApprove record, @Param("example") AuthApproveExample example);

    int updateByExample(@Param("record") AuthApprove record, @Param("example") AuthApproveExample example);

    int updateByPrimaryKeySelective(AuthApprove record);

    int updateByPrimaryKeyWithBLOBs(AuthApprove record);

    int updateByPrimaryKey(AuthApprove record);

    int upsert(AuthApprove record);

    int upsertSelective(AuthApprove record);

    int upsertWithBLOBs(AuthApprove record);
}