package com.ctrip.zeus.tag;

//import com.ctrip.zeus.service.query.command.QueryCommand;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TagService {

//    Set<Long> queryByCommand(QueryCommand command, String type) throws Exception;

    Set<Long> queryByType(String type) throws Exception;

    Set<Long> unionQuery(List<String> tagNames, String type) throws Exception;

    Set<Long> joinQuery(List<String> tagNames, String type) throws Exception;

    List<Long> query(String tagName, String type);

    List<String> getTags(Long[] tagIds) throws Exception;

    List<String> getTags(String type, Long itemId);

    List<String> getAllTags() throws Exception;

    Map<Long, List<String>> getTags(String type, Long[] itemIds) throws Exception;
}
