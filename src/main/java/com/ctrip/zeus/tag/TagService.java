package com.ctrip.zeus.tag;

import com.ctrip.zeus.service.query.command.QueryCommand;

import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/7/20.
 */
public interface TagService {

    Set<Long> queryByCommand(QueryCommand command, String type) throws Exception;

    Set<Long> unionQuery(List<String> tagNames, String type) throws Exception;

    Set<Long> joinQuery(List<String> tagNames, String type) throws Exception;

    List<Long> query(String tagName, String type) throws Exception;

    List<String> getTags(String type, Long itemId) throws Exception;
}
