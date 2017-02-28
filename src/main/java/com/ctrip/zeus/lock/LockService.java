package com.ctrip.zeus.lock;

import com.ctrip.zeus.lock.entity.LockStatus;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * Created by zhoumy on 2015/4/16.
 */
public interface LockService {

    List<LockStatus> getLockStatus() throws DalException;

    void forceUnlockByServer(String ip) throws DalException;

    void forceUnlock(String key) throws DalException;
}
