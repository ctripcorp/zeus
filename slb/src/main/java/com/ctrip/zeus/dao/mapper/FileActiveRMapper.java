package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.FileActiveR;
import com.ctrip.zeus.dao.entity.FileActiveRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileActiveRMapper {
    long countByExample(FileActiveRExample example);

    int deleteByExample(FileActiveRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FileActiveR record);

    int insertSelective(FileActiveR record);

    FileActiveR selectOneByExample(FileActiveRExample example);

    FileActiveR selectOneByExampleSelective(@Param("example") FileActiveRExample example, @Param("selective") FileActiveR.Column ... selective);

    List<FileActiveR> selectByExampleSelective(@Param("example") FileActiveRExample example, @Param("selective") FileActiveR.Column ... selective);

    List<FileActiveR> selectByExample(FileActiveRExample example);

    FileActiveR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") FileActiveR.Column ... selective);

    FileActiveR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FileActiveR record, @Param("example") FileActiveRExample example);

    int updateByExample(@Param("record") FileActiveR record, @Param("example") FileActiveRExample example);

    int updateByPrimaryKeySelective(FileActiveR record);

    int updateByPrimaryKey(FileActiveR record);

    int upsert(FileActiveR record);

    int upsertSelective(FileActiveR record);
}