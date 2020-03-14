package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.NginxModelSnapshot;
import com.ctrip.zeus.dao.entity.NginxModelSnapshotExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NginxModelSnapshotMapper {
    long countByExample(NginxModelSnapshotExample example);

    int deleteByExample(NginxModelSnapshotExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NginxModelSnapshot record);

    int insertSelective(NginxModelSnapshot record);

    NginxModelSnapshot selectOneByExample(NginxModelSnapshotExample example);

    NginxModelSnapshot selectOneByExampleSelective(@Param("example") NginxModelSnapshotExample example, @Param("selective") NginxModelSnapshot.Column ... selective);

    NginxModelSnapshot selectOneByExampleWithBLOBs(NginxModelSnapshotExample example);

    List<NginxModelSnapshot> selectByExampleSelective(@Param("example") NginxModelSnapshotExample example, @Param("selective") NginxModelSnapshot.Column ... selective);

    List<NginxModelSnapshot> selectByExampleWithBLOBs(NginxModelSnapshotExample example);

    List<NginxModelSnapshot> selectByExample(NginxModelSnapshotExample example);

    NginxModelSnapshot selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") NginxModelSnapshot.Column ... selective);

    NginxModelSnapshot selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NginxModelSnapshot record, @Param("example") NginxModelSnapshotExample example);

    int updateByExampleWithBLOBs(@Param("record") NginxModelSnapshot record, @Param("example") NginxModelSnapshotExample example);

    int updateByExample(@Param("record") NginxModelSnapshot record, @Param("example") NginxModelSnapshotExample example);

    int updateByPrimaryKeySelective(NginxModelSnapshot record);

    int updateByPrimaryKeyWithBLOBs(NginxModelSnapshot record);

    int updateByPrimaryKey(NginxModelSnapshot record);

    int upsert(NginxModelSnapshot record);

    int upsertSelective(NginxModelSnapshot record);

    int upsertWithBLOBs(NginxModelSnapshot record);
}