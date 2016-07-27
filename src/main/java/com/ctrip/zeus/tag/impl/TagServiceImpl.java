package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.TagQueryCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
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
    public Set<Long> queryByCommand(final QueryCommand command, final String type) throws Exception {
        final TagQueryCommand tagQuery = (TagQueryCommand) command;
        Long[] tagIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tagQuery.hasValue(tagQuery.union_tag);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        List<String> tn = new ArrayList<>();
                        for (String s : tagQuery.getValue(tagQuery.union_tag)) {
                            tn.add(s.trim());
                        }
                        return unionQuery(tn, type);
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return command.hasValue(tagQuery.join_tag);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        List<String> tn = new ArrayList<>();
                        for (String s : tagQuery.getValue(tagQuery.join_tag)) {
                            tn.add(s.trim());
                        }
                        return joinQuery(tn, type);
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tagQuery.hasValue(tagQuery.item_type);
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        String[] tmpt = tagQuery.getValue(tagQuery.item_type);
                        if (tmpt.length > 1)
                            throw new ValidationException("Query tags does not support multiple types.");
                        return queryByType(tmpt[0]);
                    }
                }).build(Long.class).run();

        if (tagIds == null) return null;
        if (tagIds.length == 0) return new HashSet<>();

        Set<Long> result = new HashSet<>();
        for (Long i : tagIds) {
            result.add(i);
        }
        return result;
    }

    @Override
    public Set<Long> queryByType(String type) throws Exception {
        Set<Long> result = new HashSet<>();
        for (TagItemDo d : tagItemDao.findAllByType(type, TagItemEntity.READSET_FULL)) {
            result.add(d.getTagId());
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
    public List<String> getAllTags() throws Exception {
        List<String> result = new ArrayList<>();
        for (TagDo tagDo : tagDao.findAll(TagEntity.READSET_FULL)) {
            result.add(tagDo.getName());
        }
        return result;
    }

    @Override
    public List<String> getTags(Long[] tagIds) throws Exception {
        List<String> result = new ArrayList<>();
        for (TagDo d : tagDao.findAllByIds(tagIds, TagEntity.READSET_FULL)) {
            result.add(d.getName());
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

    @Override
    public Map<Long, List<String>> getTags(String type, Long[] itemIds) throws Exception {
        Map<Long, List<Long>> rItemTag = new HashMap<>();
        Set<Long> tagIds = new HashSet<>();

        for (TagItemDo d : tagItemDao.findAllByItemsAndType(itemIds, type, TagItemEntity.READSET_FULL)) {
            tagIds.add(d.getTagId());
            List<Long> l = rItemTag.get(d.getItemId());
            if (l == null) {
                l = new ArrayList<>();
                rItemTag.put(d.getItemId(), l);
            }
            l.add(d.getTagId());
        }

        if (tagIds.size() == 0) return new HashMap<>();

        Map<Long, String> rIdName = new HashMap<>();
        for (TagDo d : tagDao.findAllByIds(tagIds.toArray(new Long[tagIds.size()]), TagEntity.READSET_FULL)) {
            rIdName.put(d.getId(), d.getName());
        }

        Map<Long, List<String>> result = new HashMap<>();
        for (Map.Entry<Long, List<Long>> e : rItemTag.entrySet()) {
            List<String> l = new ArrayList<>();
            result.put(e.getKey(), l);
            for (Long i : e.getValue()) {
                String r = rIdName.get(i);
                if (r != null) l.add(r);
            }
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
