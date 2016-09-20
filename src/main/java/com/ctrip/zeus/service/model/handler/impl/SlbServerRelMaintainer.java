package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.VersionUtils;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("slbServerRelMaintainer")
public class SlbServerRelMaintainer extends AbstractMultiRelMaintainer<RelSlbSlbServerDo, SlbServer, Slb> {
    @Resource
    private RSlbSlbServerDao rSlbSlbServerDao;
    @Resource
    private RSlbStatusDao rSlbStatusDao;

    public SlbServerRelMaintainer() {
        super(RelSlbSlbServerDo.class, Slb.class);
    }

    @Override
    protected IdVersion getIdxKey(RelSlbSlbServerDo rel) throws Exception {
        return new IdVersion(rel.getSlbId(), rel.getSlbVersion());
    }

    @Override
    protected void setDo(Slb object, SlbServer value, RelSlbSlbServerDo target) {
        target.setSlbId(object.getId()).setIp(value.getIp())
                .setSlbVersion(object.getVersion());
    }

    @Override
    protected List<RelSlbSlbServerDo> getRelsByObjectId(Slb object) throws Exception {
        return rSlbSlbServerDao.findAllBySlb(object.getId(), RSlbSlbServerEntity.READSET_FULL);
    }

    @Override
    protected List<RelSlbSlbServerDo> getRelsByObjectId(Long[] objectIds) throws Exception {
        return rSlbSlbServerDao.findAllBySlbs(objectIds, RSlbSlbServerEntity.READSET_FULL);
    }

    @Override
    protected Integer[] getStatusByObjectId(Slb object) throws Exception {
        RelSlbStatusDo d = rSlbStatusDao.findBySlb(object.getId(), RSlbStatusEntity.READSET_FULL);
        return new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()};
    }

    @Override
    protected Map<Long, Integer[]> getStatusByObjectId(Long[] objectIds) throws Exception {
        Map<Long, Integer[]> result = new HashMap<>();
        for (RelSlbStatusDo d : rSlbStatusDao.findBySlbs(objectIds, RSlbStatusEntity.READSET_FULL)) {
            result.put(d.getSlbId(), new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()});
        }
        return result;
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
    public void clear(Long objectId) throws Exception {
        rSlbSlbServerDao.deleteAllBySlb(new RelSlbSlbServerDo().setSlbId(objectId));
    }

    @Override
    public List<SlbServer> get(Slb object) throws Exception {
        return object.getSlbServers();
    }
}
