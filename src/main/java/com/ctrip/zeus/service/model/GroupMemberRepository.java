package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.GroupServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/27.
 */
public interface GroupMemberRepository {

    List<String> listGroupServerIpsByGroup(Long groupId) throws Exception;

    List<GroupServer> listGroupServersByGroup(Long groupId) throws Exception;

    Long[] findGroupsByGroupServerIp(String groupServerIp) throws Exception;

    void addGroupServer(Long groupId, GroupServer groupServer) throws Exception;

    void updateGroupServer(Long groupId, GroupServer groupServer) throws Exception;

    void removeGroupServer(Long groupId, String ip) throws Exception;
}
