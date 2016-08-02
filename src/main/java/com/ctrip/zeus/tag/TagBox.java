package com.ctrip.zeus.tag;

/**
 * Created by zhoumy on 2015/7/16.
 */
public interface TagBox {

    void removeTag(String name, boolean force) throws Exception;

    void renameTag(String oldName, String newName) throws Exception;

    void tagging(String tagName, String type, Long[] itemIds) throws Exception;

    void untagging(String tagName, String type, Long[] itemIds) throws Exception;

    void clear(String type, Long itemId) throws Exception;
}