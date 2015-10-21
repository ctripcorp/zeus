package com.ctrip.zeus.service.clean.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.clean.CleanFilter;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

/**
 * Created by fanqq on 2015/10/20.
 */
@Service("confCleanFilter")
public class ConfCleanFilter implements CleanFilter {
    @Resource
    private NginxConfDao nginxConfDao;
    @Resource
    private NginxConfServerDao nginxConfServerDao;
    @Resource
    private NginxConfUpstreamDao nginxConfUpstreamDao;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    private static DynamicIntProperty confSaveCounts = DynamicPropertyFactory.getInstance().getIntProperty("config.save.count", 100000);

    @Override
    public void runFilter() throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        for (Long slbId : slbIds){
            //remove nginx_conf
            NginxConfDo nginxConfDo = nginxConfDao.findBySlbIdAndMaxVersion(slbId, NginxConfEntity.READSET_FULL);
            nginxConfDao.deleteBySlbIdLessThanVersion(new NginxConfDo().setVersion(nginxConfDo.getVersion()-confSaveCounts.get()).setSlbId(slbId));

            //remove nginx_conf_server
            NginxConfServerDo nginxConfServerDo = nginxConfServerDao.findBySlbIdAndMaxVersion(slbId,NginxConfServerEntity.READSET_FULL);
            nginxConfServerDao.deleteBySlbIdLessThanVersion(new NginxConfServerDo().setSlbId(slbId).setVersion(nginxConfServerDo.getVersion()-confSaveCounts.get()));

            //remove nginx_conf_upstreams
            NginxConfUpstreamDo nginxConfUpstreamDo = nginxConfUpstreamDao.findBySlbIdAndMaxVersion(slbId,NginxConfUpstreamEntity.READSET_FULL);
            nginxConfUpstreamDao.deleteBySlbIdLessThanVersion(new NginxConfUpstreamDo().setSlbId(slbId).setVersion(nginxConfUpstreamDo.getVersion()-confSaveCounts.get()));
        }
    }

    @Override
    public int interval() {
        return 12;
    }
}
