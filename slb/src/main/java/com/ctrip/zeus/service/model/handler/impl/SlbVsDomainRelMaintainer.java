package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.SlbVsDomainR;
import com.ctrip.zeus.dao.entity.SlbVsDomainRExample;
import com.ctrip.zeus.dao.entity.SlbVsStatusR;
import com.ctrip.zeus.dao.entity.SlbVsStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbVsDomainRMapper;
import com.ctrip.zeus.dao.mapper.SlbVsStatusRMapper;
import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.SmartVsStatusRMapper;
import com.ctrip.zeus.service.model.IdVersion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("slbVsDomainRelMaintainer")
public class SlbVsDomainRelMaintainer extends AbstractMultiRelMaintainer<SlbVsDomainR, Domain, VirtualServer> {
    @Resource
    private SlbVsDomainRMapper slbVsDomainRMapper;
    @Resource
    private SlbVsStatusRMapper slbVsStatusRMapper;
    @Resource
    private SmartVsStatusRMapper smartVsStatusRMapper;

    public SlbVsDomainRelMaintainer() {
        super(SlbVsDomainR.class, VirtualServer.class);
    }

    @Override
    protected void updateByPrimaryKey(SlbVsDomainR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbVsDomainRMapper.batchUpdate(Arrays.asList(values));
    }

    @Override
    protected void insert(SlbVsDomainR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbVsDomainRMapper.batchInsert(Arrays.asList(values));
    }

    @Override
    protected void deleteByPrimaryKey(SlbVsDomainR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbVsDomainRMapper.batchDelete(Arrays.asList(values));
    }

    @Override
    protected IdVersion getIdxKey(SlbVsDomainR rel) throws Exception {
        return new IdVersion(rel.getVsId(), rel.getVsVersion());
    }

    @Override
    protected void setDo(VirtualServer object, Domain value, SlbVsDomainR target) {
        target.setVsId(object.getId());
        target.setVsVersion(object.getVersion());
        target.setDomain(value.getName());
    }

    @Override
    protected List<SlbVsDomainR> getRelsByObjectId(VirtualServer object) throws Exception {
        return slbVsDomainRMapper.selectByExample(new SlbVsDomainRExample().createCriteria().andVsIdEqualTo(object.getId()).example());
    }

    @Override
    protected List<SlbVsDomainR> getRelsByObjectId(Long[] objectIds) throws Exception {
        if (objectIds == null || objectIds.length == 0) return Collections.EMPTY_LIST;
        return slbVsDomainRMapper.selectByExample(new SlbVsDomainRExample().createCriteria().andVsIdIn(Arrays.asList(objectIds)).example());
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

        for (SlbVsStatusR d : smartVsStatusRMapper.selectByExample(new SlbVsStatusRExample().
                createCriteria().
                andVsIdIn(Arrays.asList(objectIds)).
                example())) {
            result.put(d.getVsId(), new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()});
        }
        return result;
    }

    @Override
    public void clear(Long objectId) throws Exception {
        slbVsDomainRMapper.deleteByExample(new SlbVsDomainRExample().createCriteria().andVsIdEqualTo(objectId).example());
    }

    @Override
    public List<Domain> get(VirtualServer object) throws Exception {
        return object.getDomains();
    }
}
