package com.ctrip.zeus.tag;


public interface TagBox {

    void removeTag(String name, boolean force) throws Exception;

    void renameTag(String oldName, String newName) throws Exception;

    void tagging(String tagName, String type, Long[] itemIds) throws Exception;

    void untagging(String tagName, String type, Long[] itemIds);

    void clear(String type, Long itemId) throws Exception;
}