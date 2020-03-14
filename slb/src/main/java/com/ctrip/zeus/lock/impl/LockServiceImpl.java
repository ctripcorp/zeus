package com.ctrip.zeus.lock.impl;

import com.ctrip.zeus.dao.entity.DistLock;
import com.ctrip.zeus.dao.entity.DistLockExample;
import com.ctrip.zeus.dao.mapper.DistLockMapper;
import com.ctrip.zeus.lock.LockService;
import com.ctrip.zeus.model.lock.LockStatus;
import org.springframework.stereotype.Component;

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
    private DistLockMapper distLockMapper;

    @Override
    public List<LockStatus> getLockStatus() {
        List<LockStatus> list = new ArrayList<>();
        List<DistLock> locks = distLockMapper.selectByExample(new DistLockExample().createCriteria().example());
        for (DistLock lock : locks) {
            if (MysqlDistLock.isFree(lock))
                continue;
            list.add(toLockStatus(lock));
        }
        return list;
    }

    private LockStatus toLockStatus(DistLock lock) {
        return new LockStatus().setKey(lock.getLockKey())
                .setOwner(lock.getOwner()).setServer(lock.getServer())
                .setCreatedTime(new Date(lock.getCreatedTime()));
    }

    @Override
    public void forceUnlockByServer(String ip) {
        List<DistLock> locks = distLockMapper.selectByExample(new DistLockExample().createCriteria().andServerEqualTo(ip).example());
        for (DistLock d : locks) {
            d.setServer("");
            d.setOwner(0L);
            d.setCreatedTime(System.currentTimeMillis());
            distLockMapper.updateByPrimaryKey(d);
        }
    }

    @Override
    public void forceUnlock(String key) {
        DistLock d = new DistLock();
        d.setServer("");
        d.setOwner(0L);
        d.setLockKey(key);
        d.setCreatedTime(System.currentTimeMillis());
        distLockMapper.updateByExampleSelective(d, new DistLockExample().createCriteria().andLockKeyEqualTo(key).example());
    }
}
