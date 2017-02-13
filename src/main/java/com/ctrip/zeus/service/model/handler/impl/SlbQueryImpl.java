package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author:zhoumy
 * @date: 11/4/2015.
 */
@Component("slbQuery")
public class SlbQueryImpl implements SlbQuery {
    @Resource
    private RSlbSlbServerDao rSlbSlbServerDao;

    @Override
    public List<String> getSlbIps(Long slbId) throws Exception {
        List<String> result = new ArrayList<>();
        for (RelSlbSlbServerDo relSlbSlbServerDo : rSlbSlbServerDao.findAllBySlb(slbId, RSlbSlbServerEntity.READSET_FULL)) {
            result.add(relSlbSlbServerDo.getIp());
        }
        return result;
    }

    @Override
    public Map<Long, List<SlbServer>> getServersBySlb() throws Exception {
        Map<Long, List<SlbServer>> result = new HashMap<>();
        for (RelSlbSlbServerDo e : rSlbSlbServerDao.findAllBySlbOfflineVersion(RSlbSlbServerEntity.READSET_FULL)) {
            List<SlbServer> v = result.get(e.getSlbId());
            if (v == null) {
                v = new ArrayList<>();
                result.put(e.getSlbId(), v);
            }
            v.add(new SlbServer().setIp(e.getIp()));
        }
        return result;
    }
}
