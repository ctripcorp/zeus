package com.ctrip.zeus.lock.impl;

import com.ctrip.zeus.dal.core.DistLockDao;
import com.ctrip.zeus.dal.core.DistLockDo;
import com.ctrip.zeus.dal.core.DistLockEntity;
import com.ctrip.zeus.lock.LockService;
import com.ctrip.zeus.lock.entity.LockStatus;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2015/4/17.
 */
@Component("lockService")
public class LockServiceImpl implements LockService {
    @Resource
    private DistLockDao distLockDao;

    @Override
    public List<LockStatus> getLockStatus() throws DalException {
        List<LockStatus> list = new ArrayList<>();
        for (DistLockDo d : distLockDao.findAll(DistLockEntity.READSET_FULL)) {
            if (MysqlDistLock.isFree(d))
                continue;
            list.add(toLockStatus(d));
        }
        return list;
    }

    @Override
    public void forceUnlockByServer(String ip) throws DalException {
        List<DistLockDo> check = distLockDao.getByServer(ip, DistLockEntity.READSET_FULL);
        for (DistLockDo d : check) {
            d.setServer("").setOwner(0L).setCreatedTime(System.currentTimeMillis());
        }
        distLockDao.updateByKey(check.toArray(new DistLockDo[check.size()]), DistLockEntity.UPDATESET_FULL);
    }

    @Override
    public void forceUnlock(String key) throws DalException {
        distLockDao.updateByKey(new DistLockDo().setLockKey(key).setServer("").setOwner(0L).setCreatedTime(System.currentTimeMillis()), DistLockEntity.UPDATESET_FULL);
    }

    private static LockStatus toLockStatus(DistLockDo d) {
        LockStatus ls = new LockStatus().setKey(d.getLockKey())
                .setOwner(d.getOwner()).setServer(d.getServer())
                .setCreatedTime(new Date(d.getCreatedTime()));
        return ls;
    }
}
