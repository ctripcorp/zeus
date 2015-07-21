package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.tag.TagService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/7/20.
 */
public class TagServiceImpl implements TagService {
    @Resource
    private TagDao tagDao;
    @Resource
    private TagItemDao tagItemDao;

    @Override
    public List<Long> query(String tagName, String type) throws Exception {
        TagDo d = tagDao.findByName(tagName, TagEntity.READSET_FULL);
        List<Long> result = new ArrayList<>();
        if (d == null)
            return result;
        for (TagItemDo tagItemDo : tagItemDao.findByTag(d.getId(), TagItemEntity.READSET_FULL)) {
            result.add(tagItemDo.getItemId());
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
