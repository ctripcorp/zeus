package com.ctrip.zeus.tag;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/20.
 */
public interface TagService {

    List<Long> query(List<String> tagNames, String type) throws Exception;

    List<Long> query(String tagName, String type) throws Exception;

    List<String> getTags(String type, Long itemId) throws Exception;
}
