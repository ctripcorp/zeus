package com.ctrip.zeus.service.clean.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.NginxConfMapper;
import com.ctrip.zeus.dao.mapper.NginxConfSlbMapper;
import com.ctrip.zeus.dao.mapper.NginxModelSnapshotMapper;
import com.ctrip.zeus.dao.mapper.NginxServerMapper;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.service.build.util.NginxModelSnapshotType;
import com.ctrip.zeus.service.clean.AbstractCleanFilter;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2015/10/20.
 */
@Service("confCleanFilter")
public class ConfCleanFilter extends AbstractCleanFilter {
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private NginxConfMapper nginxConfMapper;
    @Resource
    private NginxConfSlbMapper nginxConfSlbMapper;
    @Resource
    private NginxServerMapper nginxServerMapper;
    @Resource
    private NginxModelSnapshotMapper nginxModelSnapshotMapper;


    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static DynamicIntProperty confSaveCounts = DynamicPropertyFactory.getInstance().getIntProperty("config.save.count", 50);
    private static DynamicIntProperty maxDeleteTimes = DynamicPropertyFactory.getInstance().getIntProperty("config.max.delete.times", 500);
    private static DynamicIntProperty deleteLimit = DynamicPropertyFactory.getInstance().getIntProperty("config.delete.limit", 200);

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

        for (Long id : mapping.keySet()) {
            try {
                Set<String> ips = servers.get(id);
                long min = Long.MAX_VALUE;
                List<NginxServer> serverList = nginxServerMapper.selectByExample(new NginxServerExample().createCriteria().andSlbIdEqualTo(id).example());
                for (NginxServer d : serverList) {
                    if (ips.contains(d.getIp())) {
                        if (min > d.getVersion()) min = d.getVersion();
                    }
                }

                int retainedMinVersion = (int) min - confSaveCounts.get();

                NginxModelSnapshot latestFullVersion = nginxModelSnapshotMapper.selectOneByExampleSelective(NginxModelSnapshotExample.newAndCreateCriteria().
                                andSlbIdEqualTo(id).
                                andSnapshotTypeNotEqualTo(NginxModelSnapshotType.INCREMENTAL).
                                example().orderBy("version desc").limit(1), NginxModelSnapshot.Column.version);
                logger.info("[[cleanjob=true]]Start Clean;Id:" + id);
                for (int i = 0; i < maxDeleteTimes.get(); i++) {
                    boolean isFinish =  true;
                    List<NginxConf> nginxConfs = nginxConfMapper.selectByExampleSelective(new NginxConfExample().createCriteria()
                                    .andSlbIdEqualTo(id).andVersionLessThan(retainedMinVersion).example().limit(deleteLimit.get())
                            , NginxConf.Column.id);
                    if (nginxConfs != null && nginxConfs.size() > 0) {
                        List<Long> ids = new ArrayList<>();
                        nginxConfs.forEach(e -> ids.add(e.getId()));
                        nginxConfMapper.deleteByExample(new NginxConfExample().createCriteria()
                                .andIdIn(ids).example());
                        isFinish = false;
                    }

                    List<NginxConfSlb> nginxConfSlbs = nginxConfSlbMapper.selectByExampleSelective(new NginxConfSlbExample()
                                    .createCriteria().andSlbIdEqualTo(id).andVersionLessThan((long) retainedMinVersion).example().limit(deleteLimit.get())
                            , NginxConfSlb.Column.id);
                    if (nginxConfSlbs != null && nginxConfSlbs.size() > 0) {
                        List<Long> ids = new ArrayList<>();
                        nginxConfSlbs.forEach(e -> ids.add(e.getId()));
                        nginxConfSlbMapper.deleteByExample(new NginxConfSlbExample().createCriteria()
                                .andIdIn(ids).example());
                        isFinish = false;
                    }

                    if (latestFullVersion == null) {
                        logger.error("No full version of snapshot exists for slb. slbId: " + id);
                        continue;
                    }
                    long latestFull = latestFullVersion.getVersion();
                    retainedMinVersion = (int)Math.min(retainedMinVersion, latestFull);
                    List<NginxModelSnapshot> NginxModelSnapshots = nginxModelSnapshotMapper.selectByExampleSelective(new NginxModelSnapshotExample().createCriteria().andSlbIdEqualTo(id)
                                    .andVersionLessThan((long) retainedMinVersion).example().limit(deleteLimit.get())
                            , NginxModelSnapshot.Column.id);
                    if (NginxModelSnapshots != null && NginxModelSnapshots.size() > 0) {
                        List<Long> ids = new ArrayList<>();
                        NginxModelSnapshots.forEach(e -> ids.add(e.getId()));
                        nginxModelSnapshotMapper.deleteByExample(new NginxModelSnapshotExample().createCriteria()
                                .andIdIn(ids).example());
                        isFinish = false;
                    }

                    if (isFinish) {
                        break;
                    }
                }
                logger.info("[[cleanjob=true]]End Clean;Id:" + id);
            } catch (Exception ex) {
                logger.error("Cleanse conf files failed of slb " + id, ex);
            }
        }
    }
}
