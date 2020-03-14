package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.ReportSnapshot;
import com.ctrip.zeus.dao.entity.ReportSnapshotExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportSnapshotMapper {

    /** Non-automatic generation Code Start **/
    int batchInsert(List<ReportSnapshot> records);
    /** Non-automatic generation Code END **/

    long countByExample(ReportSnapshotExample example);

    int deleteByExample(ReportSnapshotExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportSnapshot record);

    int insertSelective(ReportSnapshot record);

    ReportSnapshot selectOneByExample(ReportSnapshotExample example);

    ReportSnapshot selectOneByExampleSelective(@Param("example") ReportSnapshotExample example, @Param("selective") ReportSnapshot.Column ... selective);

    List<ReportSnapshot> selectByExampleSelective(@Param("example") ReportSnapshotExample example, @Param("selective") ReportSnapshot.Column ... selective);

    List<ReportSnapshot> selectByExample(ReportSnapshotExample example);

    ReportSnapshot selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") ReportSnapshot.Column ... selective);

    ReportSnapshot selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportSnapshot record, @Param("example") ReportSnapshotExample example);

    int updateByExample(@Param("record") ReportSnapshot record, @Param("example") ReportSnapshotExample example);

    int updateByPrimaryKeySelective(ReportSnapshot record);

    int updateByPrimaryKey(ReportSnapshot record);

    int upsert(ReportSnapshot record);

    int upsertSelective(ReportSnapshot record);
}