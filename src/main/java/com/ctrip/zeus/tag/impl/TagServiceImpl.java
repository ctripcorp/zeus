package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.TagQueryCommand;
import com.ctrip.zeus.tag.TagService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

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
    public Set<Long> queryByCommand(QueryCommand command, String type) throws Exception {
        Set<Long> result = null;
        TagQueryCommand tagQuery = (TagQueryCommand) command;
        if (command.hasValue(tagQuery.union_tag)) {
            List<String> tn = new ArrayList<>();
            for (String s : tagQuery.getValue(tagQuery.union_tag)) {
                tn.add(s.trim());
            }
            result = unionQuery(tn, type);
        }
        if (command.hasValue(tagQuery.join_tag)) {
            List<String> tn = new ArrayList<>();
            for (String s : tagQuery.getValue(tagQuery.join_tag)) {
                tn.add(s.trim());
            }
            if (result == null) {
                result = joinQuery(tn, type);
            } else {
                result.retainAll(joinQuery(tn, type));
            }
        }
        return result;
    }

    @Override
    public Set<Long> unionQuery(List<String> tagNames, String type) throws Exception {
        List<TagDo> tags = tagDao.findAllByNames(tagNames.toArray(new String[tagNames.size()]), TagEntity.READSET_FULL);
        if (tags.size() == 0) return new HashSet<>();

        Long[] tagIds = new Long[tags.size()];
        for (int i = 0; i < tagIds.length; i++) {
            tagIds[i] = tags.get(i).getId();
        }

        Set<Long> result = new HashSet<>();
        for (TagItemDo d : tagItemDao.findByTagsAndType(tagIds, type, TagItemEntity.READSET_FULL)) {
            result.add(d.getItemId());
        }
        return result;
    }

    @Override
    public Set<Long> joinQuery(List<String> tagNames, String type) throws Exception {
        List<TagDo> tags = tagDao.findAllByNames(tagNames.toArray(new String[tagNames.size()]), TagEntity.READSET_FULL);
        if (tags.size() == 0) return new HashSet<>();

        Long[] tagIds = new Long[tags.size()];
        for (int i = 0; i < tagIds.length; i++) {
            tagIds[i] = tags.get(i).getId();
        }
        if (tagIds.length < tagNames.size()) return new HashSet<>();

        int joinedValue = tagIds.length;
        Map<Long, Counter> marker = new HashMap<>();
        for (TagItemDo d : tagItemDao.findByTagsAndType(tagIds, type, TagItemEntity.READSET_FULL)) {
            Counter m = marker.get(d.getItemId());
            if (m == null) {
                marker.put(d.getItemId(), new Counter());
            } else {
                m.incr();
            }
        }

        Set<Long> result = new HashSet<>();
        for (Map.Entry<Long, Counter> e : marker.entrySet()) {
            if (e.getValue().get() == joinedValue) result.add(e.getKey());
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

    private class Counter {
        int count = 1;

        public void incr() {
            count++;
        }

        public int get() {
            return count;
        }
    }
}
