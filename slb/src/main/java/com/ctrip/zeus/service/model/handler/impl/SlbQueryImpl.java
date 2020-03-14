package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.SlbSlbServerR;
import com.ctrip.zeus.dao.entity.SlbSlbServerRExample;
import com.ctrip.zeus.dao.mapper.SlbSlbServerRMapper;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.service.build.ConfigHandler;
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
    private SlbSlbServerRMapper slbSlbServerRMapper;
    @Resource
    private ConfigHandler configHandler;
    public void setConfigHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }
    @Override
    public List<String> getSlbIps(Long slbId) throws Exception {
        List<String> result = new ArrayList<>();
        for (SlbSlbServerR slbSlbServerR : slbSlbServerRMapper.selectByExampleSelective(new SlbSlbServerRExample().createCriteria().andSlbIdEqualTo(slbId).example(), SlbSlbServerR.Column.ip)) {
            result.add(slbSlbServerR.getIp());
        }
        return result;
    }

    @Override
    public Map<Long, List<SlbServer>> getServersBySlb() throws Exception {
        Map<Long, List<SlbServer>> result = new HashMap<>();
        for (SlbSlbServerR e : slbSlbServerRMapper.findAllBySlbOfflineVersion()) {
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
