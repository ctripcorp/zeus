package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.tag.TagService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/7/20.
 */
@Component("tagService")
public class TagServiceImpl implements TagService {
    @Resource
    private TagDao tagDao;
    @Resource
    private TagItemDao tagItemDao;

    @Override
    public List<Long> query(List<String> tagNames, String type) throws Exception {
        List<TagDo> tags = tagDao.findAllByNames(tagNames.toArray(new String[tagNames.size()]), TagEntity.READSET_FULL);
        if (tags.size() == 0) return new ArrayList<>();

        Long[] tagIds = new Long[tags.size()];
        for (int i = 0; i < tagIds.length; i++) {
            tagIds[i] = tags.get(i).getId();
        }

        List<Long> result = new ArrayList<>();
        for (TagItemDo d : tagItemDao.findByTagsAndType(tagIds, type, TagItemEntity.READSET_FULL)) {
            result.add(d.getItemId());
        }
        return result;
    }

    @Override
    public List<Long> query(String tagName, String type) throws Exception {
        TagDo d = tagDao.findByName(tagName, TagEntity.READSET_FULL);
        List<Long> result = new ArrayList<>();
        if (d == null)
            return result;
        for (TagItemDo ti : tagItemDao.findByTag(d.getId(), TagItemEntity.READSET_FULL)) {
            if (type.equals(ti.getType())) {
                result.add(ti.getItemId());
            }
        }
        return result;
    }

    @Override
    public List<String> getTags(String type, Long itemId) throws Exception {
        List<TagItemDo> list = tagItemDao.findByItemAndType(itemId, type, TagItemEntity.READSET_FULL);
        List<String> result = new ArrayList<>();
        if (list.size() == 0)
            return result;
        Long[] tagIds = new Long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            tagIds[i] = list.get(i).getTagId();
        }
        for (TagDo tagDo : tagDao.findAllByIds(tagIds, TagEntity.READSET_FULL)) {
            result.add(tagDo.getName());
        }
        return result;
    }
}
