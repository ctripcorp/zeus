package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.SlbVsSlbR;
import com.ctrip.zeus.dao.entity.SlbVsSlbRExample;
import com.ctrip.zeus.dao.entity.SlbVsStatusR;
import com.ctrip.zeus.dao.entity.SlbVsStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbVsSlbRMapper;
import com.ctrip.zeus.dao.mapper.SlbVsStatusRMapper;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.SmartVsStatusRMapper;
import com.ctrip.zeus.service.model.IdVersion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2016/9/20.
 */
@Component("slbVsSlbRelMaintainer")
public class SlbVsSlbRelMaintainer extends AbstractMultiRelMaintainer<SlbVsSlbR, Long, VirtualServer> {
    @Resource
    private SlbVsSlbRMapper slbVsSlbRMapper;

    @Resource
    private SlbVsStatusRMapper slbVsStatusRMapper;

    @Resource
    private SmartVsStatusRMapper smartVsStatusRMapper;

    public SlbVsSlbRelMaintainer() {
        this(SlbVsSlbR.class, VirtualServer.class);
    }

    protected SlbVsSlbRelMaintainer(Class domainClazz, Class viewClazz) {
        super(domainClazz, viewClazz);
    }

    @Override
    public void clear(Long objectId) throws Exception {
        slbVsSlbRMapper.deleteByExample(new SlbVsSlbRExample().createCriteria().andVsIdEqualTo(objectId).example());
    }

    @Override
    public List<Long> get(VirtualServer object) throws Exception {
        return object.getSlbIds();
    }

    @Override
    protected IdVersion getIdxKey(SlbVsSlbR rel) throws Exception {
        return new IdVersion(rel.getVsId(), rel.getVsVersion());
    }

    @Override
    protected void setDo(VirtualServer object, Long value, SlbVsSlbR target) {
        target.setVsId(object.getId());
        target.setVsVersion(object.getVersion());
        target.setSlbId(value);
    }

    @Override
    protected List<SlbVsSlbR> getRelsByObjectId(VirtualServer object) throws Exception {
        return slbVsSlbRMapper.selectByExample(new SlbVsSlbRExample().createCriteria().andVsIdEqualTo(object.getId()).example());
    }

    @Override
    protected List<SlbVsSlbR> getRelsByObjectId(Long[] objectIds) throws Exception {
        if (objectIds == null || objectIds.length == 0) return Collections.EMPTY_LIST;
        return slbVsSlbRMapper.selectByExample(new SlbVsSlbRExample().createCriteria().andVsIdIn(Arrays.asList(objectIds)).example());
    }

    @Override
    protected Integer[] getStatusByObjectId(VirtualServer object) throws Exception {
        SlbVsStatusR d = slbVsStatusRMapper.selectOneByExample(new SlbVsStatusRExample().createCriteria().andVsIdEqualTo(object.getId()).example());
        return new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()};
    }

    @Override
    protected Map<Long, Integer[]> getStatusByObjectId(Long[] objectIds) throws Exception {
        Map<Long, Integer[]> result = new HashMap<>();
        if (objectIds == null || objectIds.length == 0) return result;

        for (SlbVsStatusR d : smartVsStatusRMapper.selectByExample(new SlbVsStatusRExample().createCriteria().andVsIdIn(Arrays.asList(objectIds)).example())) {
            result.put(d.getVsId(), new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()});
        }
        return result;
    }

    @Override
    protected void insert(SlbVsSlbR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbVsSlbRMapper.batchInsert(Arrays.asList(values));
    }

    @Override
    protected void updateByPrimaryKey(SlbVsSlbR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbVsSlbRMapper.batchUpdate(Arrays.asList(values));
    }

    @Override
    protected void deleteByPrimaryKey(SlbVsSlbR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbVsSlbRMapper.batchDelete(Arrays.asList(values));
    }
}
