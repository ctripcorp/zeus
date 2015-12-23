package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("vsDomainRelMaintainer")
public class VsDomainRelMaintainer extends AbstractMultiRelMaintainer<RelVsDomainDo, Domain, VirtualServer> {
    @Resource
    private RVsDomainDao rVsDomainDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    @Override
    protected List<RelVsDomainDo> getAll(Long id) throws Exception {
        return rVsDomainDao.findAllDomainsByVs(id, RVsDomainEntity.READSET_FULL);
    }

    @Override
    protected int getTargetVersion(RelVsDomainDo target) throws Exception {
        return target.getVsVersion();
    }

    @Override
    protected int getOnlineVersion(Long id) throws Exception {
        RelVsStatusDo check = rVsStatusDao.findByVs(id, RVsStatusEntity.READSET_FULL);
        return check.getOnlineVersion();
    }

    @Override
    protected int getOfflineVersion(Long id) throws Exception {
        RelVsStatusDo check = rVsStatusDao.findByVs(id, RVsStatusEntity.READSET_FULL);
        return check.getOfflineVersion();
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
    protected Long getTargetId(VirtualServer object) throws Exception {
        return object.getId();
    }

    @Override
    protected RelVsDomainDo getDo(VirtualServer object, Domain value) throws Exception {
        return new RelVsDomainDo().setVsId(object.getId())
                .setDomain(value.getName())
                .setVsVersion(object.getVersion());
    }

    @Override
    protected void reassign(VirtualServer object, RelVsDomainDo output, Domain input) throws Exception {
        output.setDomain(input.getName()).setVsVersion(object.getVersion());
    }

    @Override
    public void relDelete(Long objectId) throws Exception {
        rVsDomainDao.deleteAllByVs(new RelVsDomainDo().setVsId(objectId));
    }

    @Override
    public void relBatchDelete(Long[] objectIds) throws Exception {
        RelVsDomainDo[] dos = new RelVsDomainDo[objectIds.length];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelVsDomainDo().setVsId(objectIds[i]);
        }
        rVsDomainDao.deleteAllByVs(dos);
    }
}
