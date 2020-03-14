package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.*;
import com.ctrip.zeus.exceptions.TagValidationException;
import com.ctrip.zeus.tag.TagBox;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhoumy on 2015/7/16.
 */
@Component("tagBox")
public class DefaultTagBox implements TagBox {
    @Resource
    private TagTagMapper tagTagMapper;
    @Resource
    private TagTagItemRMapper tagTagItemRMapper;

    @Override
    public void removeTag(String name, boolean force) throws Exception {
        TagTag d = tagTagMapper.selectOneByExample(new TagTagExample().createCriteria().andNameEqualTo(name).example());
        if (d == null) return;
        if (force) {
            tagTagItemRMapper.deleteByExample(new TagTagItemRExample().createCriteria().andTagIdEqualTo(d.getId()).example());
        } else {
            List<TagTagItemR> check = tagTagItemRMapper.selectByExample(new TagTagItemRExample().createCriteria().andTagIdEqualTo(d.getId()).example());
            if (check.size() > 0)
                throw new TagValidationException("Combination exists with tag " + name + ".");
        }
        tagTagMapper.deleteByPrimaryKey(d.getId());
    }

    @Override
    public void renameTag(String oldName, String newName) throws Exception {
        TagTag d = tagTagMapper.selectOneByExample(new TagTagExample().createCriteria().andNameEqualTo(oldName).example());
        if (d == null)
            return;
        d.setName(newName);
        d.setDatachangeLasttime(null);
        tagTagMapper.updateByPrimaryKey(d);
    }

    @Override
    public void tagging(String tagName, String type, Long[] itemIds) throws Exception {
        TagTag d = tagTagMapper.selectOneByExample(new TagTagExample().createCriteria().andNameEqualTo(tagName).example());
        if (d == null) {
            d = new TagTag();
            d.setName(tagName);
            tagTagMapper.upsert(d);
        }

        List<TagTagItemR> tagTagItemRS = new ArrayList<>();
        for (int i = 0; i < itemIds.length; i++) {
            TagTagItemR itemR = tagTagItemRMapper.selectOneByExample(new TagTagItemRExample().
                    createCriteria().
                    andTagIdEqualTo(d.getId()).
                    andTypeEqualTo(type).
                    andItemIdEqualTo(itemIds[i]).
                    example());
            if (itemR != null) {
                continue;
            }
            TagTagItemR tagTagItemR = new TagTagItemR();
            tagTagItemR.setTagId(d.getId());
            tagTagItemR.setType(type);
            tagTagItemR.setItemId(itemIds[i]);
            tagTagItemRS.add(tagTagItemR);
        }
        if (tagTagItemRS.size() > 0)
            tagTagItemRMapper.batchInsert(tagTagItemRS);
    }

    @Override
    public void untagging(String tagName, String type, Long[] itemIds) {
        TagTag d = tagTagMapper.selectOneByExample(new TagTagExample().createCriteria().andNameEqualTo(tagName).example());
        if (d == null) {
            return;
        }
        if (itemIds != null) {
            tagTagItemRMapper.deleteByExample(new TagTagItemRExample().createCriteria()
                    .andTagIdEqualTo(d.getId()).andTypeEqualTo(type).andItemIdIn(Arrays.asList(itemIds)).example());
        } else {
            tagTagItemRMapper.deleteByExample(new TagTagItemRExample().createCriteria().andTagIdEqualTo(d.getId()).andTypeEqualTo(type).example());
        }
    }

    @Override
    public void clear(String type, Long itemId) throws Exception {
        List<TagTagItemR> list = tagTagItemRMapper.selectByExample(new TagTagItemRExample().createCriteria().andItemIdEqualTo(itemId).andTypeEqualTo(type).example());
        if (list.size() > 0) {
            List<Long> ids = new ArrayList<>();
            for (TagTagItemR tagTagItemR : list) {
                ids.add(tagTagItemR.getId());
            }
            tagTagItemRMapper.deleteByExample(new TagTagItemRExample().createCriteria().andIdIn(ids).example());
        }
    }
}
