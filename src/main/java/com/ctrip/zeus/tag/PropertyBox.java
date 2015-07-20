package com.ctrip.zeus.tag;

import com.ctrip.zeus.tag.entity.Property;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/16.
 */
public interface PropertyBox {
    
    List<Property> getAllProperties() throws Exception;

    void addProperty(String pname, String pvalue) throws Exception;

    void removeProperty(String pname) throws Exception;

    void renameProperty(String oldName, String newName) throws Exception;

    void renameProperty(String oldName, String newName, String oldValue, String newValue) throws Exception;

    void add(String pname, String pvalue, String type, Long itemId) throws Exception;

    void delete(String pname, String pvalue, String type, Long itemId) throws Exception;
}
