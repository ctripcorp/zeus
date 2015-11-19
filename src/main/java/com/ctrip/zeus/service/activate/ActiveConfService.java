package com.ctrip.zeus.service.activate;

import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/3/30.
 */
public interface ActiveConfService extends Repository {

    public Set<Long> getSlbIdsByGroupId(Long groupId)throws Exception;
    public Set<Long> getGroupIdsBySlbId(Long slbId) throws Exception;
    public Set<Long> getVsIdsBySlbId(Long slbId) throws Exception;
}
