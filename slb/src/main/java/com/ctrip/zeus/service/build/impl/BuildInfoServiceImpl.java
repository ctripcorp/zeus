package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dao.entity.SlbBuildTicket;
import com.ctrip.zeus.dao.entity.SlbBuildTicketExample;
import com.ctrip.zeus.dao.mapper.SlbBuildTicketMapper;
import com.ctrip.zeus.service.build.BuildInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component("buildInfoService")
public class BuildInfoServiceImpl implements BuildInfoService {
    @Resource
    private SlbBuildTicketMapper slbBuildTicketMapper;


    @Override
    public int getTicket(Long slbId) throws Exception {
        SlbBuildTicket slbBuildTicket = SlbBuildTicket.builder().
                slbId(slbId).
                createdTime(new Date()).
                datachangeLasttime(new Date()).
                currentTicket(1).
                pendingTicket(1).
                build();

        SlbBuildTicket existingTicket = slbBuildTicketMapper.selectOneByExample(new SlbBuildTicketExample().createCriteria().andSlbIdEqualTo(slbId).example());
        if (existingTicket != null) {
            slbBuildTicket.setId(existingTicket.getId());
            slbBuildTicket.setPendingTicket(existingTicket.getPendingTicket() + 1);
            slbBuildTicket.setCurrentTicket(existingTicket.getCurrentTicket() + 1);
            slbBuildTicketMapper.updateByExampleSelective(slbBuildTicket, new SlbBuildTicketExample().createCriteria().andSlbIdEqualTo(slbId).example());
        } else {
            slbBuildTicketMapper.insert(slbBuildTicket);
        }

        return slbBuildTicket.getCurrentTicket();
    }
}
