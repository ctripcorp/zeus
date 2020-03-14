package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.dao.entity.FileDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileDataMapper {
    long countByExample(FileDataExample example);

    int deleteByExample(FileDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FileData record);

    int insertSelective(FileData record);

    FileData selectOneByExample(FileDataExample example);

    FileData selectOneByExampleSelective(@Param("example") FileDataExample example, @Param("selective") FileData.Column ... selective);

    FileData selectOneByExampleWithBLOBs(FileDataExample example);

    List<FileData> selectByExampleSelective(@Param("example") FileDataExample example, @Param("selective") FileData.Column ... selective);

    List<FileData> selectByExampleWithBLOBs(FileDataExample example);

    List<FileData> selectByExample(FileDataExample example);

    FileData selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") FileData.Column ... selective);

    FileData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FileData record, @Param("example") FileDataExample example);

    int updateByExampleWithBLOBs(@Param("record") FileData record, @Param("example") FileDataExample example);

    int updateByExample(@Param("record") FileData record, @Param("example") FileDataExample example);

    int updateByPrimaryKeySelective(FileData record);

    int updateByPrimaryKeyWithBLOBs(FileData record);

    int updateByPrimaryKey(FileData record);

    int upsert(FileData record);

    int upsertSelective(FileData record);

    int upsertWithBLOBs(FileData record);
}