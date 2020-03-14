package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.FileServerActiveR;
import com.ctrip.zeus.dao.entity.FileServerActiveRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileServerActiveRMapper {
    long countByExample(FileServerActiveRExample example);

    int deleteByExample(FileServerActiveRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FileServerActiveR record);

    int insertSelective(FileServerActiveR record);

    FileServerActiveR selectOneByExample(FileServerActiveRExample example);

    FileServerActiveR selectOneByExampleSelective(@Param("example") FileServerActiveRExample example, @Param("selective") FileServerActiveR.Column ... selective);

    List<FileServerActiveR> selectByExampleSelective(@Param("example") FileServerActiveRExample example, @Param("selective") FileServerActiveR.Column ... selective);

    List<FileServerActiveR> selectByExample(FileServerActiveRExample example);

    FileServerActiveR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") FileServerActiveR.Column ... selective);

    FileServerActiveR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FileServerActiveR record, @Param("example") FileServerActiveRExample example);

    int updateByExample(@Param("record") FileServerActiveR record, @Param("example") FileServerActiveRExample example);

    int updateByPrimaryKeySelective(FileServerActiveR record);

    int updateByPrimaryKey(FileServerActiveR record);

    int upsert(FileServerActiveR record);

    int upsertSelective(FileServerActiveR record);
}