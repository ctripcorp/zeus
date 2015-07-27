package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.GroupServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/27.
 */
public interface GroupMemberRepository {

    List<String> listGroupServersBySlb(Long slbId) throws Exception;

    List<String> listGroupServerIpsByGroup(Long groupId) throws Exception;

    List<GroupServer> listGroupServerByGroup(Long groupId) throws Exception;

    Long[] findGroupsByGroupServerIp(String groupServerIp) throws Exception;
}
