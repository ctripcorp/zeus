package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
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
        return null;
    }

    @Override
    public void addTag(String name) throws Exception {

    }

    @Override
    public void removeTag(String name) throws Exception {

    }

    @Override
    public void renameTag(String oldName, String newName) throws Exception {

    }

    @Override
    public void tagging(String tagName, String type, Long itemId) throws Exception {

    }

    @Override
    public void untagging(String tagName, String type, Long itemId) throws Exception {

    }
}
