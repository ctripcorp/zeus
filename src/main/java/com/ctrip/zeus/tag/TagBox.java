package com.ctrip.zeus.tag;

import com.ctrip.zeus.tag.entity.Tag;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/7/16.
 */
public interface TagBox {

    List<Tag> getAllTags();

    Tag getTag(Long tagId);

    Tag getTag(String name, String type);

    List<Long> getAllItems(Long tagId);

    Map<Long, Long> getAllItems(Long[] tagIds);

    void addTag(String name, String type);

    void addTag(String name, String type, String extendedValue);

    void removeTag(Long tagId);

    void tagging(Long tagId, Long itemId);

    void untagging(Long tagId, Long itemId);
}