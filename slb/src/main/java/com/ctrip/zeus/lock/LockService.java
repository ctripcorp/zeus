package com.ctrip.zeus.lock;

import com.ctrip.zeus.model.lock.LockStatus;

import java.util.List;

/**
 * Created by zhoumy on 2015/4/16.
 */
public interface LockService {

    List<LockStatus> getLockStatus();

    void forceUnlockByServer(String ip);

    void forceUnlock(String key);
}
