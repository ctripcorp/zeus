package com.ctrip.zeus.tag;

import com.ctrip.zeus.tag.entity.Property;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/21.
 */
public interface PropertyService {

    List<Long> query(String pname, String pvalue, String type) throws Exception;

    List<Long> query(String pname, String type) throws Exception;

    List<Property> getProperties(String type, Long itemId) throws Exception;
}
