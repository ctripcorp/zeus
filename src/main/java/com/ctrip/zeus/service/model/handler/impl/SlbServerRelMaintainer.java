package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("slbServerRelMaintainer")
public class SlbServerRelMaintainer extends MultiRelMaintainerEx<RelSlbSlbServerDo, SlbServer, Slb> {
    @Resource
    private RSlbSlbServerDao rSlbSlbServerDao;
    @Resource
    private RSlbStatusDao rSlbStatusDao;

    @Override
    protected List<RelSlbSlbServerDo> getAll(Long id) throws Exception {
        return rSlbSlbServerDao.findAllBySlb(id, RSlbSlbServerEntity.READSET_FULL);
    }

    @Override
    protected Long getTargetId(Slb object) throws Exception {
        return object.getId();
    }

    @Override
    protected int getTargetVersion(RelSlbSlbServerDo target) throws Exception {
        return target.getSlbVersion();
    }

    @Override
    protected int getOnlineVersion(Long id) throws Exception {
        RelSlbStatusDo check = rSlbStatusDao.findBySlb(id, RSlbStatusEntity.READSET_FULL);
        return check.getOnlineVersion();
    }

    @Override
    protected RelSlbSlbServerDo getDo(Slb object, SlbServer value) throws Exception {
        return new RelSlbSlbServerDo().setSlbId(object.getId())
                .setIp(value.getIp())
                .setSlbVersion(object.getVersion());
    }

    @Override
    protected void updateByPrimaryKey(RelSlbSlbServerDo[] values) throws Exception {
        rSlbSlbServerDao.update(values, RSlbSlbServerEntity.UPDATESET_FULL);
    }

    @Override
    protected void insert(RelSlbSlbServerDo[] values) throws Exception {
        rSlbSlbServerDao.insert(values);
    }

    @Override
    protected void deleteByPrimaryKey(RelSlbSlbServerDo[] values) throws Exception {
        rSlbSlbServerDao.delete(values);
    }

    @Override
    protected void reassign(Slb object, RelSlbSlbServerDo output, SlbServer input) throws Exception {
        output.setIp(input.getIp()).setSlbVersion(object.getVersion());
    }

    @Override
    public void deleteRel(Long objectId) throws Exception {
        rSlbSlbServerDao.deleteAllBySlb(new RelSlbSlbServerDo().setSlbId(objectId));
    }

    @Override
    public void batchDeleteRel(Long[] objectIds) throws Exception {
        throw new NotImplementedException();
    }
}
