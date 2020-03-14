package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.*;
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
    private TagTagMapper tagTagMapper;
    @Resource
    private TagTagItemRMapper tagTagItemRMapper;

//    @Override
//    public Set<Long> queryByCommand(final QueryCommand command, final String type) throws Exception {
//        final TagQueryCommand tagQuery = (TagQueryCommand) command;
//        Long[] tagIds = new QueryExecuter.Builder<Long>()
//                .addFilter(new FilterSet<Long>() {
//                    @Override
//                    public boolean shouldFilter() throws Exception {
//                        return tagQuery.hasValue(tagQuery.union_tag);
//                    }
//
//                    @Override
//                    public Set<Long> filter() throws Exception {
//                        List<String> tn = new ArrayList<>();
//                        for (String s : tagQuery.getValue(tagQuery.union_tag)) {
//                            tn.add(s.trim());
//                        }
//                        return unionQuery(tn, type);
//                    }
//                })
//                .addFilter(new FilterSet<Long>() {
//                    @Override
//                    public boolean shouldFilter() throws Exception {
//                        return command.hasValue(tagQuery.join_tag);
//                    }
//
//                    @Override
//                    public Set<Long> filter() throws Exception {
//                        List<String> tn = new ArrayList<>();
//                        for (String s : tagQuery.getValue(tagQuery.join_tag)) {
//                            tn.add(s.trim());
//                        }
//                        return joinQuery(tn, type);
//                    }
//                })
//                .addFilter(new FilterSet<Long>() {
//                    @Override
//                    public boolean shouldFilter() throws Exception {
//                        return tagQuery.hasValue(tagQuery.item_type);
//                    }
//
//                    @Override
//                    public Set<Long> filter() throws Exception {
//                        String[] tmpt = tagQuery.getValue(tagQuery.item_type);
//                        if (tmpt.length > 1)
//                            throw new ValidationException("Query tags does not support multiple types.");
//                        return queryByType(tmpt[0]);
//                    }
//                }).build(Long.class).run();
//
//        if (tagIds == null) return null;
//        if (tagIds.length == 0) return new HashSet<>();
//
//        Set<Long> result = new HashSet<>();
//        for (Long i : tagIds) {
//            result.add(i);
//        }
//        return result;
//    }

    @Override
    public Set<Long> queryByType(String type) throws Exception {
        Set<Long> result = new HashSet<>();
        if (type == null) return result;
        for (TagTagItemR d : tagTagItemRMapper.selectByExample(new TagTagItemRExample().createCriteria().andTypeEqualTo(type).example())) {
            result.add(d.getTagId());
        }
        return result;
    }

    @Override
    public Set<Long> unionQuery(List<String> tagNames, String type) throws Exception {
        Set<Long> result = new HashSet<>();
        if(tagNames == null || tagNames.isEmpty() || type == null) return result;
        List<TagTag> tags = tagTagMapper.selectByExample(new TagTagExample().createCriteria().andNameIn(tagNames).example());
        if (tags == null || tags.size() == 0) return result;

        List<Long> tagIds = new ArrayList<>();
        for (TagTag tagTag : tags) {
            tagIds.add(tagTag.getId());
        }
        for (TagTagItemR d : tagTagItemRMapper.selectByExample(new TagTagItemRExample().createCriteria().andTypeEqualTo(type).andTagIdIn(tagIds).example())) {
            result.add(d.getItemId());
        }
        return result;
    }

    @Override
    public Set<Long> joinQuery(List<String> tagNames, String type) throws Exception {
        Set<Long> result = new HashSet<>();
        if (tagNames == null || tagNames.isEmpty() || type == null) return result;
        List<TagTag> tags = tagTagMapper.selectByExample(new TagTagExample().createCriteria().andNameIn(tagNames).example());
        if (tags == null || tags.size() == 0) return result;

        List<Long> tagIds = new ArrayList<>();
        for (TagTag tagTag : tags) {
            tagIds.add(tagTag.getId());
        }

        int joinedValue = tagIds.size();
        Map<Long, Counter> marker = new HashMap<>();
        for (TagTagItemR d : tagTagItemRMapper.selectByExample(new TagTagItemRExample().createCriteria().andTypeEqualTo(type).andTagIdIn(tagIds).example())) {
            Counter m = marker.get(d.getItemId());
            if (m == null) {
                marker.put(d.getItemId(), new Counter());
            } else {
                m.incr();
            }
        }
        for (Map.Entry<Long, Counter> e : marker.entrySet()) {
            if (e.getValue().get() == joinedValue) result.add(e.getKey());
        }
        return result;
    }

    @Override
    public List<Long> query(String tagName, String type) {
        List<Long> result = new ArrayList<>();
        if (tagName == null || type == null) return result;
        TagTag d = tagTagMapper.selectOneByExample(new TagTagExample().createCriteria().andNameEqualTo(tagName).example());
        if (d == null)
            return result;
        for (TagTagItemR ti : tagTagItemRMapper.selectByExample(new TagTagItemRExample().createCriteria().andTagIdEqualTo(d.getId()).example())) {
            if (type.equals(ti.getType())) {
                result.add(ti.getItemId());
            }
        }
        return result;
    }

    @Override
    public List<String> getAllTags() throws Exception {
        List<String> result = new ArrayList<>();
        for (TagTag tagDo : tagTagMapper.selectByExample(new TagTagExample())) {
            result.add(tagDo.getName());
        }
        return result;
    }

    @Override
    public List<String> getTags(Long[] tagIds) throws Exception {
        List<String> result = new ArrayList<>();
        if(tagIds == null || tagIds.length == 0) return result;
        for (TagTag d : tagTagMapper.selectByExample(new TagTagExample().createCriteria().andIdIn(Arrays.asList(tagIds)).example())) {
            result.add(d.getName());
        }
        return result;
    }

    @Override
    public List<String> getTags(String type, Long itemId) {
        List<String> result = new ArrayList<>();
        if (type == null || itemId == null) return result;
        List<TagTagItemR> list = tagTagItemRMapper.selectByExample(new TagTagItemRExample().createCriteria().andItemIdEqualTo(itemId).andTypeEqualTo(type).example());
        if (list == null || list.isEmpty()) return result;
        List<Long> tagIds = new ArrayList<>();
        for (TagTagItemR tagTagItemR : list) {
            tagIds.add(tagTagItemR.getTagId());
        }
        for (TagTag tagDo : tagTagMapper.selectByExample(new TagTagExample().createCriteria().andIdIn(tagIds).example())) {
            result.add(tagDo.getName());
        }
        return result;
    }

    @Override
    public Map<Long, List<String>> getTags(String type, Long[] itemIds) throws Exception {
        Map<Long, List<String>> result = new HashMap<>();
        if (itemIds == null || itemIds.length == 0 || type == null) return result;
        Map<Long, List<Long>> rItemTag = new HashMap<>();
        Set<Long> tagIds = new HashSet<>();

        for (TagTagItemR d : tagTagItemRMapper.selectByExample(new TagTagItemRExample().createCriteria().andTypeEqualTo(type).andItemIdIn(Arrays.asList(itemIds)).example())) {
            tagIds.add(d.getTagId());
            List<Long> l = rItemTag.get(d.getItemId());
            if (l == null) {
                l = new ArrayList<>();
                rItemTag.put(d.getItemId(), l);
            }
            l.add(d.getTagId());
        }

        if (tagIds.isEmpty()) return new HashMap<>();

        Map<Long, String> rIdName = new HashMap<>();
        for (TagTag d : tagTagMapper.selectByExample(new TagTagExample().createCriteria().andIdIn(new ArrayList<>(tagIds)).example())) {
            rIdName.put(d.getId(), d.getName());
        }

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
