package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.SlbGroupStatusR;
import com.ctrip.zeus.dao.entity.SlbGroupStatusRExample;
import com.ctrip.zeus.dao.entity.SlbGroupVsR;
import com.ctrip.zeus.dao.entity.SlbGroupVsRExample;
import com.ctrip.zeus.dao.mapper.SlbGroupStatusRMapper;
import com.ctrip.zeus.dao.mapper.SlbGroupVsRMapper;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.SmartGroupStatusRMapper;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.handler.model.GroupVirtualServerContent;
import com.ctrip.zeus.support.DefaultObjectJsonParser;
import com.ctrip.zeus.support.DefaultObjectJsonWriter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("slbGroupVsRelMaintainer")
public class SlbGroupVsRelMaintainer extends AbstractMultiRelMaintainer<SlbGroupVsR, GroupVirtualServer, Group> {

    @Resource
    private SlbGroupVsRMapper slbGroupVsRMapper;
    @Resource
    private SmartGroupStatusRMapper smartGroupStatusRMapper;

    @Resource
    private SlbGroupStatusRMapper slbGroupStatusRMapper;

    public SlbGroupVsRelMaintainer() {
        super(SlbGroupVsR.class, Group.class);
    }

    @Override
    public void updateByPrimaryKey(SlbGroupVsR[] values) throws Exception {
        if (values == null || values.length == 0) return;
        slbGroupVsRMapper.batchUpdate(Arrays.asList(values));
    }

    @Override
    protected IdVersion getIdxKey(SlbGroupVsR rel) throws Exception {
        return new IdVersion(rel.getGroupId(), rel.getGroupVersion());
    }

    @Override
    protected void setDo(Group object, GroupVirtualServer value, SlbGroupVsR target) throws Exception {
        GroupVirtualServerContent gvsContent =
                DefaultObjectJsonParser.parse(DefaultObjectJsonWriter.write(value), GroupVirtualServerContent.class);
        gvsContent.setGroupId(object.getId())
                .setGroupVersion(object.getVersion())
                .setVirtualServer(new VirtualServer().setId(value.getVirtualServer().getId()));
        target.setGroupId(object.getId());
        target.setVsId(value.getVirtualServer().getId());
        target.setGroupVersion(object.getVersion());
        target.setPriority(0);

        target.setContent(DefaultObjectJsonWriter.write(gvsContent).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected List<SlbGroupVsR> getRelsByObjectId(Group object) throws Exception {
        return slbGroupVsRMapper.selectByExampleWithBLOBs(new SlbGroupVsRExample().createCriteria().andGroupIdEqualTo(object.getId()).example());
    }

    @Override
    protected List<SlbGroupVsR> getRelsByObjectId(Long[] objectIds) throws Exception {
        if (objectIds == null || objectIds.length == 0) return Collections.EMPTY_LIST;
        return slbGroupVsRMapper.selectByExampleWithBLOBs(new SlbGroupVsRExample().createCriteria().andGroupIdIn(Arrays.asList(objectIds)).example());
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
    public void insert(SlbGroupVsR[] values) throws Exception {
        if (values == null || values.length == 0) return;
        slbGroupVsRMapper.batchInsert(Arrays.asList(values));
    }

    @Override
    public void deleteByPrimaryKey(SlbGroupVsR[] values) throws Exception {
        if (values == null || values.length == 0) return;
        slbGroupVsRMapper.batchDelete(Arrays.asList(values));
    }

    @Override
    public void clear(Long objectId) throws Exception {
        slbGroupVsRMapper.deleteByExample(new SlbGroupVsRExample().createCriteria().andGroupIdEqualTo(objectId).example());
    }

    @Override
    public List<GroupVirtualServer> get(Group object) throws Exception {
        return object.getGroupVirtualServers();
    }
}
