package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.SlbVirtualServerDao;
import com.ctrip.zeus.dal.core.SlbVirtualServerDo;
import com.ctrip.zeus.dal.core.SlbVirtualServerEntity;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhoumy on 2015/9/11.
 */
@Component("virtualServerCriteriaQuery")
public class DefaultVirtualServerCriteriaQuery implements VirtualServerCriteriaQuery {

    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbVirtualServerDo slbVirtualServerDo : slbVirtualServerDao.findAll(SlbVirtualServerEntity.READSET_FULL)) {
            result.add(slbVirtualServerDo.getId());
        }
        return result;
    }

    @Override
    public Set<Long> queryBySlbId(Long slbId) throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbVirtualServerDo slbVirtualServerDo : slbVirtualServerDao.findAllBySlb(slbId, SlbVirtualServerEntity.READSET_FULL)) {
            result.add(slbVirtualServerDo.getId());
        }
        return result;
    }
}
