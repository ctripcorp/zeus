package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.SlbGroupGsR;
import com.ctrip.zeus.dao.entity.SlbGroupGsRExample;
import com.ctrip.zeus.dao.entity.SlbGroupStatusR;
import com.ctrip.zeus.dao.entity.SlbGroupStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbGroupGsRMapper;
import com.ctrip.zeus.dao.mapper.SlbGroupStatusRMapper;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupServer;
import com.ctrip.zeus.service.SmartGroupStatusRMapper;
import com.ctrip.zeus.service.model.IdVersion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("slbGroupGsRelMaintainer")
public class SlbGroupGsRelMaintainer extends AbstractMultiRelMaintainer<SlbGroupGsR, GroupServer, Group> {
    @Resource
    private SlbGroupGsRMapper slbGroupGsRMapper;
    @Resource
    private SlbGroupStatusRMapper slbGroupStatusRMapper;
    @Resource
    private SmartGroupStatusRMapper smartGroupStatusRMapper;

    public SlbGroupGsRelMaintainer() {
        super(SlbGroupGsR.class, Group.class);
    }

    @Override
    public void updateByPrimaryKey(SlbGroupGsR[] values) throws Exception {
        if (values == null || values.length == 0) return;
        slbGroupGsRMapper.batchUpdate(Arrays.asList(values));
    }

    @Override
    protected IdVersion getIdxKey(SlbGroupGsR rel) throws Exception {
        return new IdVersion(rel.getGroupId(), rel.getGroupVersion());
    }

    @Override
    protected void setDo(Group object, GroupServer value, SlbGroupGsR target) {
        target.setGroupId(object.getId());
        target.setGroupVersion(object.getVersion());
        target.setIp(value.getIp());
    }

    @Override
    protected List<SlbGroupGsR> getRelsByObjectId(Group object) throws Exception {
        return slbGroupGsRMapper.selectByExample(new SlbGroupGsRExample().createCriteria().andGroupIdEqualTo(object.getId()).example());
    }

    @Override
    protected List<SlbGroupGsR> getRelsByObjectId(Long[] objectIds) throws Exception {
        if (objectIds == null || objectIds.length == 0) return Collections.EMPTY_LIST;
        return slbGroupGsRMapper.selectByExample(new SlbGroupGsRExample().createCriteria().andGroupIdIn(Arrays.asList(objectIds)).example());
    }

    @Override
    protected Integer[] getStatusByObjectId(Group object) throws Exception {
        SlbGroupStatusR d = slbGroupStatusRMapper.selectOneByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdEqualTo(object.getId()).example());
        return new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()};
    }

    @Override
    protected Map<Long, Integer[]> getStatusByObjectId(Long[] objectIds) throws Exception {
        Map<Long, Integer[]> result = new HashMap<>();
        if (objectIds == null || objectIds.length == 0) return result;
        for (SlbGroupStatusR d : smartGroupStatusRMapper.selectByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdIn(Arrays.asList(objectIds)).example())) {
            result.put(d.getGroupId(), new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()});
        }
        return result;
    }

    @Override
    public void insert(SlbGroupGsR[] values) throws Exception {
        if (values == null || values.length == 0) return;
        slbGroupGsRMapper.batchInsert(Arrays.asList(values));
    }

    @Override
    public void deleteByPrimaryKey(SlbGroupGsR[] values) throws Exception {
        if (values == null || values.length == 0) return;
        slbGroupGsRMapper.batchDelete(Arrays.asList(values));
    }

    @Override
    public void clear(Long objectId) throws Exception {
        slbGroupGsRMapper.deleteByExample(new SlbGroupGsRExample().createCriteria().andGroupIdEqualTo(objectId).example());
    }

    @Override
    public List<GroupServer> get(Group object) throws Exception {
        return object.getGroupServers();
    }
}
