package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.entity.Tag;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/7/16.
 */
@Component("tagBox")
public class DefaultTagBox implements TagBox {
    @Resource
    private TagDao tagDao;
    @Resource
    private TagItemDao tagItemDao;

    @Override
    public List<Tag> getAllTags() throws Exception {
        List<Tag> result = new ArrayList<>();
        for (TagDo tagDo : tagDao.findAll(TagEntity.READSET_FULL)) {
            result.add(toTag(tagDo));
        }
        return result;
    }

    @Override
    public Tag getTag(Long tagId) throws Exception {
        TagDo d = tagDao.findById(tagId, TagEntity.READSET_FULL);
        return toTag(d);
    }

    @Override
    public Tag getTag(String name, String type) throws Exception {
        TagDo d = tagDao.findByNameAndType(name, type, TagEntity.READSET_FULL);
        return toTag(d);
    }

    @Override
    public List<Long> getAllItems(Long tagId) throws Exception {
        List<Long> result = new ArrayList<>();
        for (TagItemDo tagItemDo : tagItemDao.findByTag(tagId, TagItemEntity.READSET_FULL)) {
            result.add(tagItemDo.getItemId());
        }
        return result;
    }

    @Override
    public Map<Long, List<Long>> getAllItems(Long[] tagIds) throws Exception {
        Map<Long, List<Long>> result = new HashMap<>();
        for (TagItemDo tagItemDo : tagItemDao.findByTags(tagIds, TagItemEntity.READSET_FULL)) {
            if (result.containsKey(tagItemDo.getTagId())) {
                result.get(tagItemDo.getTagId()).add(tagItemDo.getItemId());
            } else {
                List<Long> value = new ArrayList<>();
                value.add(tagItemDo.getItemId());
                result.put(tagItemDo.getTagId(), value);
            }
        }
        return result;
    }

    @Override
    public void addTag(String name, String type) throws Exception {
        tagDao.insert(new TagDo().setName(name).setType(type));
    }

    @Override
    public void addTag(String name, String type, String extendedValue) throws Exception {
        tagDao.insert(new TagDo().setName(name).setType(type).setValue(extendedValue));
    }

    @Override
    public void removeTag(Long tagId) throws Exception {
        tagDao.delete(new TagDo().setId(tagId));
    }

    @Override
    public void tagging(Long tagId, Long itemId) throws Exception {
        tagItemDao.insert(new TagItemDo().setTagId(tagId).setId(itemId));
    }

    @Override
    public void untagging(Long tagId, Long itemId) throws Exception {
        tagItemDao.deleteByTagAndItem(new TagItemDo().setTagId(tagId).setItemId(itemId));
    }

    private static Tag toTag(TagDo d) {
        if (d == null)
            return null;
        return new Tag().setId(d.getId()).setName(d.getName()).setType(d.getType()).setValue(d.getValue());
    }
}
