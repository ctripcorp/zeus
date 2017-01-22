package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.*;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.support.GenericSerializer;
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
    private UpstreamsConf upstreamsConf;
    @Resource
    private ConfVersionService confVersionService;
    @Resource
    private NginxConfBuilder nginxConfigBuilder;
    @Resource
    private NginxConfSlbDao nginxConfSlbDao;
    @Resource
    private NginxConfDao nginxConfDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Long build(Slb nxOnlineSlb,
                      Map<Long, VirtualServer> nxOnlineVses,
                      Set<Long> buildingVsIds,
                      Set<Long> clearingVsIds,
                      Map<Long, List<TrafficPolicy>> policiesByVsId,
                      Map<Long, List<Group>> groupsByVsId,
                      Set<String> serversToBeMarkedDown,
                      Set<String> groupMembersToBeMarkedUp) throws Exception {
        int version = buildInfoService.getTicket(nxOnlineSlb.getId());
        Long currentVersion = confVersionService.getSlbCurrentVersion(nxOnlineSlb.getId());

        String conf = nginxConfigBuilder.generateNginxConf(nxOnlineSlb);

        nginxConfDao.insert(new NginxConfDo().setSlbId(nxOnlineSlb.getId()).setContent(conf).setVersion(version));
        logger.info("Nginx Conf build success! slbId: " + nxOnlineSlb.getId() + ", version: " + version);

        NginxConfSlbDo d = nginxConfSlbDao.findBySlbAndVersion(nxOnlineSlb.getId(), currentVersion, NginxConfSlbEntity.READSET_FULL);
        // init current conf entry in case of generating conf file for entirely new cluster
        NginxConfEntry currentConfEntry = new NginxConfEntry().setUpstreams(new Upstreams()).setVhosts(new Vhosts());
        if (d != null) {
            currentConfEntry = DefaultJsonParser.parse(NginxConfEntry.class, CompressUtils.decompress(d.getContent()));
        }

        NginxConfEntry nextConfEntry = new NginxConfEntry().setUpstreams(new Upstreams()).setVhosts(new Vhosts());
        Set<String> fileTrack = new HashSet<>();
        for (Long vsId : buildingVsIds) {
            if (clearingVsIds.contains(vsId)) {
                continue;
            }
            VirtualServer virtualServer = nxOnlineVses.get(vsId);
            List<Group> groups = groupsByVsId.get(vsId);
            if (groups == null) {
                groups = new ArrayList<>();
            }

            String serverConf = nginxConfigBuilder.generateServerConf(nxOnlineSlb, virtualServer, policiesByVsId.get(vsId), groups);
            nextConfEntry.getVhosts().addConfFile(new ConfFile().setName("" + virtualServer.getId()).setContent(serverConf));

            List<ConfFile> list = nginxConfigBuilder.generateUpstreamsConf(nxOnlineVses.keySet(), virtualServer, groups, serversToBeMarkedDown, groupMembersToBeMarkedUp, fileTrack);
            for (ConfFile cf : list) {
                nextConfEntry.getUpstreams().addConfFile(cf);
            }
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
        nginxConfSlbDao.insert(new NginxConfSlbDo().setSlbId(nxOnlineSlb.getId()).setVersion(version)
                .setContent(CompressUtils.compress(GenericSerializer.writeJson(nextConfEntry, false))));
        return (long) version;
    }

    public DyUpstreamOpsData buildUpstream(Long slbId,
                                           VirtualServer virtualServer,
                                           Set<String> allDownServers,
                                           Set<String> allUpGroupServers,
                                           Group group) throws Exception {
        ConfWriter confWriter = new ConfWriter();
        upstreamsConf.writeUpstream(confWriter, slbId, virtualServer, group, allDownServers, allUpGroupServers);
        String upstreamBody = confWriter.getValue();
        return new DyUpstreamOpsData().setUpstreamCommands(upstreamBody).setUpstreamName(UpstreamsConf.getUpstreamName(group.getId()));
    }

    @Override
    public void rollBackConfig(Long slbId, int version) throws Exception {
        nginxConfDao.deleteBySlbIdFromVersion(new NginxConfDo().setSlbId(slbId).setVersion(version));
        nginxConfSlbDao.deleteBySlbIdFromVersion(new NginxConfSlbDo().setSlbId(slbId).setVersion(version));
    }
}
