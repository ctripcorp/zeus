package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.RSlbSlbServerDao;
import com.ctrip.zeus.dal.core.RSlbSlbServerEntity;
import com.ctrip.zeus.dal.core.RelSlbSlbServerDo;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
}
