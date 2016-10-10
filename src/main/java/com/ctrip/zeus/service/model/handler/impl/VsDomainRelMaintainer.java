package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.IdVersion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("vsDomainRelMaintainer")
public class VsDomainRelMaintainer extends AbstractMultiRelMaintainer<RelVsDomainDo, Domain, VirtualServer> {
    @Resource
    private RVsDomainDao rVsDomainDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    public VsDomainRelMaintainer() {
        super(RelVsDomainDo.class, VirtualServer.class);
    }

    @Override
    protected void updateByPrimaryKey(RelVsDomainDo[] values) throws Exception {
        rVsDomainDao.update(values, RVsDomainEntity.UPDATESET_FULL);
    }

    @Override
    protected void insert(RelVsDomainDo[] values) throws Exception {
        rVsDomainDao.insert(values);
    }

    @Override
    protected void deleteByPrimaryKey(RelVsDomainDo[] values) throws Exception {
        rVsDomainDao.delete(values);
    }

    @Override
    protected IdVersion getIdxKey(RelVsDomainDo rel) throws Exception {
        return new IdVersion(rel.getVsId(), rel.getVsVersion());
    }

    @Override
    protected void setDo(VirtualServer object, Domain value, RelVsDomainDo target) {
        target.setVsId(object.getId()).setVsVersion(object.getVersion()).setDomain(value.getName());
    }

    @Override
    protected List<RelVsDomainDo> getRelsByObjectId(VirtualServer object) throws Exception {
        return rVsDomainDao.findAllByVs(object.getId(), RVsDomainEntity.READSET_FULL);
    }

    @Override
    protected List<RelVsDomainDo> getRelsByObjectId(Long[] objectIds) throws Exception {
        return rVsDomainDao.findAllByVses(objectIds, RVsDomainEntity.READSET_FULL);
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
    public void clear(Long objectId) throws Exception {
        rVsDomainDao.deleteAllByVs(new RelVsDomainDo().setVsId(objectId));
    }

    @Override
    public List<Domain> get(VirtualServer object) throws Exception {
        return object.getDomains();
    }
}
