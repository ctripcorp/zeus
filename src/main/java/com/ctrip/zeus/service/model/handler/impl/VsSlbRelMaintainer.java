package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.IdVersion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2016/9/20.
 */
@Component("vsSlbRelMaintainer")
public class VsSlbRelMaintainer extends AbstractMultiRelMaintainer<RelVsSlbDo, Long, VirtualServer> {
    @Resource
    private RVsSlbDao rVsSlbDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    public VsSlbRelMaintainer() {
        this(RelVsSlbDo.class, VirtualServer.class);
    }

    protected VsSlbRelMaintainer(Class domainClazz, Class viewClazz) {
        super(domainClazz, viewClazz);
    }

    @Override
    public void clear(Long objectId) throws Exception {
        rVsSlbDao.deleteByVs(new RelVsSlbDo().setVsId(objectId));
    }

    @Override
    public List<Long> get(VirtualServer object) throws Exception {
        return object.getSlbIds();
    }

    @Override
    protected IdVersion getIdxKey(RelVsSlbDo rel) throws Exception {
        return new IdVersion(rel.getVsId(), rel.getVsVersion());
    }

    @Override
    protected void setDo(VirtualServer object, Long value, RelVsSlbDo target) {
        target.setVsId(object.getId()).setVsVersion(object.getVersion()).setSlbId(value);
    }

    @Override
    protected List<RelVsSlbDo> getRelsByObjectId(VirtualServer object) throws Exception {
        return rVsSlbDao.findByVs(object.getId(), RVsSlbEntity.READSET_FULL);
    }

    @Override
    protected List<RelVsSlbDo> getRelsByObjectId(Long[] objectIds) throws Exception {
        return rVsSlbDao.findByVses(objectIds, RVsSlbEntity.READSET_FULL);
    }

    @Override
    protected Integer[] getStatusByObjectId(VirtualServer object) throws Exception {
        RelVsStatusDo d = rVsStatusDao.findByVs(object.getId(), RVsStatusEntity.READSET_FULL);
        return new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()};
    }

    @Override
    protected Map<Long, Integer[]> getStatusByObjectId(Long[] objectIds) throws Exception {
        Map<Long, Integer[]> result = new HashMap<>();
        for (RelVsStatusDo d : rVsStatusDao.findByVses(objectIds, RVsStatusEntity.READSET_FULL)) {
            result.put(d.getVsId(), new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()});
        }
        return result;
    }

    @Override
    protected void insert(RelVsSlbDo[] values) throws Exception {
        rVsSlbDao.insert(values);
    }

    @Override
    protected void updateByPrimaryKey(RelVsSlbDo[] values) throws Exception {
        rVsSlbDao.update(values, RVsSlbEntity.UPDATESET_FULL);
    }

    @Override
    protected void deleteByPrimaryKey(RelVsSlbDo[] values) throws Exception {
        rVsSlbDao.delete(values);
    }
}
