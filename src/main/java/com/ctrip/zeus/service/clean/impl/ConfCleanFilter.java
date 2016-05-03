package com.ctrip.zeus.service.clean.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.service.clean.CleanFilter;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
    private NginxConfSlbDao nginxConfSlbDao;
    @Resource
    private NginxServerDao nginxServerDao;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private EntityFactory entityFactory;

    private static DynamicIntProperty confSaveCounts = DynamicPropertyFactory.getInstance().getIntProperty("config.save.count", 50);

    @Override
    public void runFilter() throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        Map<Long, Slb> mapping = entityFactory.getSlbsByIds(slbIds.toArray(new Long[slbIds.size()])).getOnlineMapping();
        Map<Long, Set<String>> servers = new HashMap<>();
        for (Map.Entry<Long, Slb> e : mapping.entrySet()) {
            Set<String> ips = new HashSet<>();
            for (SlbServer s : e.getValue().getSlbServers()) {
                ips.add(s.getIp());
            }
            servers.put(e.getKey(), ips);
        }

        for (Long id : slbIds) {
            Set<String> ips = servers.get(id);
            List<NginxServerDo> list = nginxServerDao.findAllBySlbId(id, NginxServerEntity.READSET_FULL);
            long min = Long.MAX_VALUE;
            for (NginxServerDo d : list) {
                if (ips.contains(d.getIp())) {
                    if (min > d.getVersion()) min = d.getVersion();
                }
            }

            int retainedMinVersion = (int) min - confSaveCounts.get();
            nginxConfDao.deleteBySlbIdLessThanVersion(new NginxConfDo().setVersion(retainedMinVersion).setSlbId(id));
            nginxConfSlbDao.deleteBySlbIdLessThanVersion(new NginxConfSlbDo().setSlbId(id).setVersion(retainedMinVersion));
        }

        for (Long slbId : slbIds) {
            //remove nginx_conf_server
            NginxConfServerDo nginxConfServerDo = nginxConfServerDao.findBySlbIdAndMaxVersion(slbId, NginxConfServerEntity.READSET_FULL);
            nginxConfServerDao.deleteBySlbIdLessThanVersion(new NginxConfServerDo().setSlbId(slbId).setVersion(nginxConfServerDo.getVersion() - confSaveCounts.get()));

            //remove nginx_conf_upstreams
            NginxConfUpstreamDo nginxConfUpstreamDo = nginxConfUpstreamDao.findBySlbIdAndMaxVersion(slbId, NginxConfUpstreamEntity.READSET_FULL);
            nginxConfUpstreamDao.deleteBySlbIdLessThanVersion(new NginxConfUpstreamDo().setSlbId(slbId).setVersion(nginxConfUpstreamDo.getVersion() - confSaveCounts.get()));
        }
    }

    @Override
    public int interval() {
        return 24;
    }
}
