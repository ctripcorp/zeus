package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.TagProperty;
import com.ctrip.zeus.dao.entity.TagPropertyExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagPropertyMapper {
    long countByExample(TagPropertyExample example);

    int deleteByExample(TagPropertyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagProperty record);

    int insertSelective(TagProperty record);

    TagProperty selectOneByExample(TagPropertyExample example);

    TagProperty selectOneByExampleSelective(@Param("example") TagPropertyExample example, @Param("selective") TagProperty.Column ... selective);

    List<TagProperty> selectByExampleSelective(@Param("example") TagPropertyExample example, @Param("selective") TagProperty.Column ... selective);

    List<TagProperty> selectByExample(TagPropertyExample example);

    TagProperty selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") TagProperty.Column ... selective);

    TagProperty selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagProperty record, @Param("example") TagPropertyExample example);

    int updateByExample(@Param("record") TagProperty record, @Param("example") TagPropertyExample example);

    int updateByPrimaryKeySelective(TagProperty record);

    int updateByPrimaryKey(TagProperty record);

    int upsert(TagProperty record);

    int upsertSelective(TagProperty record);
}