package com.ctrip.zeus.tag;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/16.
 */
public interface TagBox {

    List<String> getAllTags() throws Exception;

    void removeTag(String name) throws Exception;

    void renameTag(String oldName, String newName) throws Exception;

    void tagging(String tagName, String type, Long[] itemIds) throws Exception;

    void untagging(String tagName, String type, Long[] itemIds) throws Exception;
}