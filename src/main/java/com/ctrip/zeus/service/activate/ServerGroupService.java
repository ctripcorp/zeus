package com.ctrip.zeus.service.activate;

import com.ctrip.zeus.model.entity.Group;

import java.util.List;

/**
 * Created by fanqq on 2015/8/4.
 */
public interface ServerGroupService {

    List<Long> findAllByIp(String ip)throws Exception;
    void insertServerGroup(String ip, Long groupId)throws Exception;
    void deleteByGroupId(Long groupId)throws  Exception;
}
