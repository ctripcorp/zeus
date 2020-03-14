package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.SlbSlbServerR;
import com.ctrip.zeus.dao.entity.SlbSlbServerRExample;
import com.ctrip.zeus.dao.entity.SlbSlbStatusR;
import com.ctrip.zeus.dao.entity.SlbSlbStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbSlbServerRMapper;
import com.ctrip.zeus.dao.mapper.SlbSlbStatusRMapper;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.service.model.IdVersion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by ygshen on 2019/06/17.
 */
@Component("slbSlbServerRMaintainer")
public class SlbSlbServerRMaintainer extends AbstractMultiRelMaintainer<SlbSlbServerR, SlbServer, Slb> {
    @Resource
    private SlbSlbServerRMapper slbSlbServerRMapper;
    @Resource
    private SlbSlbStatusRMapper slbSlbStatusRMapper;

    public SlbSlbServerRMaintainer() {
        super(SlbSlbServerR.class, Slb.class);
    }

    @Override
    protected IdVersion getIdxKey(SlbSlbServerR rel) throws Exception {
        return new IdVersion(rel.getSlbId(), rel.getSlbVersion());
    }

    @Override
    protected void setDo(Slb object, SlbServer value, SlbSlbServerR target) {
        target.setSlbId(object.getId());
        target.setIp(value.getIp());
        target.setSlbVersion(object.getVersion());
    }

    @Override
    protected List<SlbSlbServerR> getRelsByObjectId(Slb object) throws Exception {
        return slbSlbServerRMapper.selectByExample(new SlbSlbServerRExample().createCriteria().andSlbIdEqualTo(object.getId()).example());
    }

    @Override
    protected List<SlbSlbServerR> getRelsByObjectId(Long[] objectIds) throws Exception {
        if (objectIds == null || objectIds.length == 0) return Collections.EMPTY_LIST;
        return slbSlbServerRMapper.selectByExample(new SlbSlbServerRExample().createCriteria().andSlbIdIn(Arrays.asList(objectIds)).example());
    }

    @Override
    protected Integer[] getStatusByObjectId(Slb object) throws Exception {
        SlbSlbStatusR d = slbSlbStatusRMapper.selectOneByExample(new SlbSlbStatusRExample().
                createCriteria().
                andSlbIdEqualTo(object.getId()).
                example());
        return new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()};
    }

    @Override
    protected Map<Long, Integer[]> getStatusByObjectId(Long[] objectIds) throws Exception {
        Map<Long, Integer[]> result = new HashMap<>();
        if(objectIds==null || objectIds.length==0) return result;

        for (SlbSlbStatusR d : slbSlbStatusRMapper.selectByExample(new SlbSlbStatusRExample().createCriteria().andSlbIdIn(Arrays.asList(objectIds)).example())) {
            result.put(d.getSlbId(), new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()});
        }
        return result;
    }

    @Override
    protected void updateByPrimaryKey(SlbSlbServerR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbSlbServerRMapper.batchUpdate(Arrays.asList(values));
    }

    @Override
    protected void insert(SlbSlbServerR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbSlbServerRMapper.batchInsert(Arrays.asList(values));
    }

    @Override
    protected void deleteByPrimaryKey(SlbSlbServerR[] values) throws Exception {
        if (values == null || values.length == 0) return;

        slbSlbServerRMapper.batchDelete(Arrays.asList(values));

    }

    @Override
    public void clear(Long objectId) throws Exception {
        slbSlbServerRMapper.deleteByExample(new SlbSlbServerRExample().createCriteria().andSlbIdEqualTo(objectId).example());
    }

    @Override
    public List<SlbServer> get(Slb object) throws Exception {
        return object.getSlbServers();
    }
}
