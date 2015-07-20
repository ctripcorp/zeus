package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.tag.TagBox;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    public List<String> getAllTags() throws Exception {
        List<String> result = new ArrayList<>();
        for (TagDo tagDo : tagDao.findAll(TagEntity.READSET_FULL)) {
            result.add(tagDo.getName());
        }
        return result;
    }

    @Override
    public void addTag(String name) throws Exception {
        tagDao.insert(new TagDo().setName(name));
    }

    @Override
    public void removeTag(String name) throws Exception {
        TagDo d = tagDao.findByName(name, TagEntity.READSET_FULL);
        if (d == null)
            return;
        tagDao.delete(d);
        tagItemDao.delete(new TagItemDo().setTagId(d.getId()));
    }

    @Override
    public void renameTag(String oldName, String newName) throws Exception {
        TagDo d = tagDao.findByName(oldName, TagEntity.READSET_FULL);
        if (d == null)
            return;
        tagDao.update(d.setName(newName), TagEntity.UPDATESET_FULL);
    }

    @Override
    public void tagging(String tagName, String type, Long itemId) throws Exception {
        TagDo d = tagDao.findByName(tagName, TagEntity.READSET_FULL);
        if (d == null) {
            throw new ValidationException("Tag named " + tagName + "is not found.");
        }
        tagItemDao.insert(new TagItemDo().setTagId(d.getId()).setType(type).setItemId(itemId));
    }

    @Override
    public void untagging(String tagName, String type, Long itemId) throws Exception {
        TagDo d = tagDao.findByName(tagName, TagEntity.READSET_FULL);
        if (d == null) {
            throw new ValidationException("Tag named " + tagName + "is not found.");
        }
        tagItemDao.delete(new TagItemDo().setTagId(d.getId()).setType(type).setItemId(itemId));
    }
}
