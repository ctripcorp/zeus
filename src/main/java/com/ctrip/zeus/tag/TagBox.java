package com.ctrip.zeus.tag;

import com.ctrip.zeus.tag.entity.Tag;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/7/16.
 */
public interface TagBox {

    List<Tag> getAllTags() throws Exception;

    Tag getTag(Long tagId) throws Exception;

    Tag getTag(String name, String type) throws Exception;

    List<Long> getAllItems(Long tagId) throws Exception;

    Map<Long, List<Long>> getAllItems(Long[] tagIds) throws Exception;

    void addTag(String name, String type) throws Exception;

    void addTag(String name, String type, String extendedValue) throws Exception;

    void removeTag(Long tagId) throws Exception;

    void tagging(Long tagId, Long itemId) throws Exception;

    void untagging(Long tagId, Long itemId) throws Exception;
}