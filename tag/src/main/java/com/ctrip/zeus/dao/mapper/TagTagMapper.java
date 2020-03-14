package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.TagTag;
import com.ctrip.zeus.dao.entity.TagTagExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagTagMapper {
    long countByExample(TagTagExample example);

    int deleteByExample(TagTagExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagTag record);

    int insertSelective(TagTag record);

    TagTag selectOneByExample(TagTagExample example);

    TagTag selectOneByExampleSelective(@Param("example") TagTagExample example, @Param("selective") TagTag.Column ... selective);

    List<TagTag> selectByExampleSelective(@Param("example") TagTagExample example, @Param("selective") TagTag.Column ... selective);

    List<TagTag> selectByExample(TagTagExample example);

    TagTag selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") TagTag.Column ... selective);

    TagTag selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagTag record, @Param("example") TagTagExample example);

    int updateByExample(@Param("record") TagTag record, @Param("example") TagTagExample example);

    int updateByPrimaryKeySelective(TagTag record);

    int updateByPrimaryKey(TagTag record);

    int upsert(TagTag record);

    int upsertSelective(TagTag record);
}