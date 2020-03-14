package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.TagTagItemR;
import com.ctrip.zeus.dao.entity.TagTagItemRExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagTagItemRMapper {

    /** Start **/
    int batchInsert(List<TagTagItemR> tagTagItemRS);

    List<TagTagItemR> selectByIds(List list);

    int batchInsertIncludeId(List<TagTagItemR> tagTags);

    List<TagTagItemR> concatSelect(@Param("concats") String[] concats);

    /** End **/

    long countByExample(TagTagItemRExample example);

    int deleteByExample(TagTagItemRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagTagItemR record);

    int insertSelective(TagTagItemR record);

    TagTagItemR selectOneByExample(TagTagItemRExample example);

    TagTagItemR selectOneByExampleSelective(@Param("example") TagTagItemRExample example, @Param("selective") TagTagItemR.Column ... selective);

    List<TagTagItemR> selectByExampleSelective(@Param("example") TagTagItemRExample example, @Param("selective") TagTagItemR.Column ... selective);

    List<TagTagItemR> selectByExample(TagTagItemRExample example);

    TagTagItemR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") TagTagItemR.Column ... selective);

    TagTagItemR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagTagItemR record, @Param("example") TagTagItemRExample example);

    int updateByExample(@Param("record") TagTagItemR record, @Param("example") TagTagItemRExample example);

    int updateByPrimaryKeySelective(TagTagItemR record);

    int updateByPrimaryKey(TagTagItemR record);

    int upsert(TagTagItemR record);

    int upsertSelective(TagTagItemR record);
}