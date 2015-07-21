package com.ctrip.zeus.tag;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/21.
 */
public interface PropertyService {

    List<Long> query(String pname, String pvalue, String type) throws Exception;

    List<Long> query(String pname, String type) throws Exception;

    List<String> getProperties(String type, Long itemId) throws Exception;
}
