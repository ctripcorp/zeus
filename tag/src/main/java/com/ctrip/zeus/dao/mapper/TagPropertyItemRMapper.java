package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.TagPropertyItemR;
import com.ctrip.zeus.dao.entity.TagPropertyItemRExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagPropertyItemRMapper {

    /** Start **/
    List<TagPropertyItemR> selectByIds(List list);

    int deleteByProperty(List<TagPropertyItemR> tagPropertyItemRExamples);

    int batchInsert(List<TagPropertyItemR> tagPropertyItemRExamples);

    int batchInsertIncludeId(List<TagPropertyItemR> tagPropertyItemRExamples);

    List<TagPropertyItemR> concatSelect(@Param("concats") String[] concats);
    /** End **/

    long countByExample(TagPropertyItemRExample example);

    int deleteByExample(TagPropertyItemRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagPropertyItemR record);

    int insertSelective(TagPropertyItemR record);

    TagPropertyItemR selectOneByExample(TagPropertyItemRExample example);

    TagPropertyItemR selectOneByExampleSelective(@Param("example") TagPropertyItemRExample example, @Param("selective") TagPropertyItemR.Column ... selective);

    List<TagPropertyItemR> selectByExampleSelective(@Param("example") TagPropertyItemRExample example, @Param("selective") TagPropertyItemR.Column ... selective);

    List<TagPropertyItemR> selectByExample(TagPropertyItemRExample example);

    TagPropertyItemR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") TagPropertyItemR.Column ... selective);

    TagPropertyItemR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagPropertyItemR record, @Param("example") TagPropertyItemRExample example);

    int updateByExample(@Param("record") TagPropertyItemR record, @Param("example") TagPropertyItemRExample example);

    int updateByPrimaryKeySelective(TagPropertyItemR record);

    int updateByPrimaryKey(TagPropertyItemR record);

    int upsert(TagPropertyItemR record);

    int upsertSelective(TagPropertyItemR record);
}