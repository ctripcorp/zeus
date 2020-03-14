package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.SlbGroup;
import com.ctrip.zeus.dao.mapper.SlbGroupMapper;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2016/3/25.
 */
@Component("groupQuery")
public class GroupQueryImpl implements GroupQuery {
    @Resource
    private SlbGroupMapper slbGroupMapper;

    @Override
    public String getAppId(Long groupId) throws Exception {
        SlbGroup slbGroup = slbGroupMapper.selectByPrimaryKey(groupId);
        return slbGroup == null ? "" : slbGroup.getAppId();
    }

    @Override
    public String getGroupName(Long groupId) throws Exception {
        SlbGroup slbGroup = slbGroupMapper.selectByPrimaryKey(groupId);
        return slbGroup == null ? "" : slbGroup.getName();
    }
}
