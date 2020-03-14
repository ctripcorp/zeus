package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dao.entity.NginxConf;
import com.ctrip.zeus.dao.entity.NginxConfExample;
import com.ctrip.zeus.dao.entity.NginxConfSlb;
import com.ctrip.zeus.dao.entity.NginxConfSlbExample;
import com.ctrip.zeus.dao.mapper.NginxConfMapper;
import com.ctrip.zeus.dao.mapper.NginxConfSlbMapper;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.model.nginx.Upstreams;
import com.ctrip.zeus.model.nginx.Vhosts;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.util.CompressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Service("buildService")
public class BuildServiceImpl implements BuildService {
    @Resource
    private BuildInfoService buildInfoService;
    @Resource
    private ConfVersionService confVersionService;
    @Resource
    private NginxConfBuilder nginxConfigBuilder;
    @Resource
    private NginxConfMapper nginxConfMapper;
    @Resource
    private NginxConfSlbMapper nginxConfSlbMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public Long build(Slb nxOnlineSlb, Map<Long, VirtualServer> nxOnlineVses, Set<Long> buildingVsIds,
                      Set<Long> clearingVsIds, Map<Long, List<TrafficPolicy>> policiesByVsId, Map<Long, List<Group>> groupsByVsId,
                      Map<Long, Map<Long, Map<Long, Integer>>> drDesSlbByGvses, Map<Long, Dr> drByGroupIds,
                      Set<String> serversToBeMarkedDown, Set<String> groupMembersToBeMarkedUp, Map<Long, String> canaryIpMap, List<Rule> defaultRules) throws Exception {
        int version = buildInfoService.getTicket(nxOnlineSlb.getId());
        Long currentVersion = confVersionService.getSlbCurrentVersion(nxOnlineSlb.getId());
        String conf = nginxConfigBuilder.generateNginxConf(nxOnlineSlb, defaultRules, null);
        NginxConf nginxConf = new NginxConf();
        nginxConf.setSlbId(nxOnlineSlb.getId());
        nginxConf.setVersion(version);
        nginxConf.setContent(conf);
        nginxConf.setDatachangeLasttime(new Date());

        nginxConfMapper.upsertSelective(nginxConf);
        logger.info("Nginx Conf build success! slbId: " + nxOnlineSlb.getId() + ", version: " + version);


        // init current conf entry in case of generating conf file for entirely new cluster
        NginxConfEntry currentConfEntry = new NginxConfEntry().setUpstreams(new Upstreams()).setVhosts(new Vhosts());
        NginxConfSlb d = nginxConfSlbMapper.selectOneByExampleWithBLOBs(new NginxConfSlbExample().createCriteria().andSlbIdEqualTo(nxOnlineSlb.getId())
                .andVersionEqualTo(currentVersion).example());
        if (d != null) {
            currentConfEntry = ObjectJsonParser.parse(CompressUtils.decompress(d.getContent()), NginxConfEntry.class);
        }
        NginxConfEntry nextConfEntry = new NginxConfEntry().setUpstreams(new Upstreams()).setVhosts(new Vhosts());
        Set<String> fileTrack = new HashSet<>();
        logger.info("[Model Snapshot Test]Start Build Nginx Conf VsIDs:" + buildingVsIds.toString());
        for (Long vsId : buildingVsIds) {
            if (clearingVsIds.contains(vsId)) {
                continue;
            }
            VirtualServer virtualServer = nxOnlineVses.get(vsId);
            List<Group> groups = groupsByVsId.get(vsId);
            if (groups == null) {
                groups = new ArrayList<>();
            }
            logger.info("[Model Snapshot Test]Build Server Conf:" + vsId + ":Groups:" + ObjectJsonWriter.write(groups));
            String serverConf = nginxConfigBuilder.generateServerConf(nxOnlineSlb, virtualServer, policiesByVsId.get(vsId), groups, drDesSlbByGvses.get(vsId), drByGroupIds, canaryIpMap, defaultRules, null);
            nextConfEntry.getVhosts().addConfFile(new ConfFile().setName("" + virtualServer.getId()).setContent(serverConf));
            logger.info("[Model Snapshot Test]Finished Server Conf:");
            List<ConfFile> list = nginxConfigBuilder.generateUpstreamsConf(nxOnlineVses.keySet(), virtualServer, groups, serversToBeMarkedDown, groupMembersToBeMarkedUp, fileTrack, defaultRules, nxOnlineSlb);
            for (ConfFile cf : list) {
                nextConfEntry.getUpstreams().addConfFile(cf);
            }
            logger.info("[Model Snapshot Test]Finished Build Upstream Conf:");
        }
        for (ConfFile cf : currentConfEntry.getVhosts().getFiles()) {
            try {
                Long vsId = Long.parseLong(cf.getName());
                if (clearingVsIds.contains(vsId) || buildingVsIds.contains(vsId)) {
                    continue;
                } else {
                    nextConfEntry.getVhosts().addConfFile(cf);
                }
            } catch (NumberFormatException ex) {
                logger.error("Unable to extract vs id information from vhost file: " + cf.getName() + ".");
            }
        }
        for (ConfFile cf : currentConfEntry.getUpstreams().getFiles()) {
            String[] fn = cf.getName().split("_");
            boolean add = true;
            for (String relatedVsId : fn) {
                if (relatedVsId.isEmpty()) continue;

                Long vsId = 0L;
                try {
                    vsId = Long.parseLong(relatedVsId);
                } catch (NumberFormatException ex) {
                    add = false;
                    logger.warn("Unable to extract vs id information from upstream file: " + cf.getName() + ".");
                    continue;
                }
                if (clearingVsIds.contains(vsId) || buildingVsIds.contains(vsId)) {
                    if (add) add = false;
                }
            }
            if (add) {
                nextConfEntry.getUpstreams().addConfFile(cf);
            }
        }
        logger.info("[Model Snapshot Test]Start Insert Nginx Conf To DB");
        NginxConfSlb toInsert = new NginxConfSlb();
        toInsert.setContent(CompressUtils.compress(ObjectJsonWriter.write(nextConfEntry)));
        toInsert.setSlbId(nxOnlineSlb.getId());
        toInsert.setVersion((long) version);
        nginxConfSlbMapper.insert(toInsert);
        logger.info("[Model Snapshot Test]Finish Insert Nginx Conf To DB");
        return (long) version;
    }


    @Override
    public void rollBackConfig(Long slbId, int version) throws Exception {
        nginxConfMapper.deleteByExample(new NginxConfExample().createCriteria().andSlbIdEqualTo(slbId).andVersionGreaterThan(version).example());
        nginxConfSlbMapper.deleteByExample(new NginxConfSlbExample().createCriteria().andSlbIdEqualTo(slbId).andVersionGreaterThan((long) version).example());
    }
}
