package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import com.ctrip.zeus.service.model.AutoFiller;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.status.StatusService;
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
    private NginxConfService nginxConfService;

    @Resource
    private NginxServerDao nginxServerDao;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    ConfGroupSlbActiveDao confGroupSlbActiveDao;
    @Resource
    private NginxConfBuilder nginxConfigBuilder;
    @Resource
    private NginxConfDao nginxConfDao;
    @Resource
    private NginxConfServerDao nginxConfServerDao;
    @Resource
    private NginxConfUpstreamDao nginxConfUpstreamDao;
    @Resource
    private StatusService statusService;
    @Resource
    private AutoFiller autoFiller;
    @Resource
    private ConfGroupActiveDao confGroupActiveDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

//    @Override
//    public Map<Long, List<Group>> getInfluencedVsGroups(Long slbId,
//                                                        HashMap<Long, Group> activatingGroups,
//                                                        Map<Long, VirtualServer> buildVirtualServer,
//                                                        Set<Long> deactivateGroup) throws Exception {
//        Map<Long, List<Group>> result = new HashMap<>();
//        Map<Long, List<Group>> dbGroups = activateService.getActivatedGroupsByVses(buildVirtualServer.keySet().toArray(new Long[]{}));
//        final Map<String, Integer> vsGroupPriority = new HashMap<>();
//        for (Long vsId : buildVirtualServer.keySet()) {
//            List<Group> activatedGroups = dbGroups.get(vsId);
//            List<Group> groupList = result.get(vsId);
//            if (groupList == null) {
//                groupList = new ArrayList<>();
//                result.put(vsId, groupList);
//            }
//            if (activatedGroups == null) {
//                continue;
//            }
//            for (Group g : activatedGroups) {
//                if (deactivateGroup.contains(g.getId())) {
//                    continue;
//                }
//                if (activatingGroups.containsKey(g.getId())) {
//                    continue;
//                }
//                for (GroupVirtualServer gv : g.getGroupVirtualServers()) {
//                    if (gv.getVirtualServer().getId().equals(vsId)) {
//                        vsGroupPriority.put("VS" + vsId + "_" + g.getId(), gv.getPriority());
//                        groupList.add(g);
//                    }
//                }
//            }
//        }
//        for (Long gid : activatingGroups.keySet()) {
//            if (deactivateGroup.contains(gid)) {
//                continue;
//            }
//            Group g = activatingGroups.get(gid);
//            for (GroupVirtualServer gv : g.getGroupVirtualServers()) {
//                if (buildVirtualServer.containsKey(gv.getVirtualServer().getId())) {
//                    List<Group> list = result.get(gv.getVirtualServer().getId());
//                    if (list == null) {
//                        list = new ArrayList<>();
//                        result.put(gv.getVirtualServer().getId(), list);
//                    }
//                    list.add(g);
//                    vsGroupPriority.put("VS" + gv.getVirtualServer().getId() + "_" + g.getId(), gv.getPriority());
//                    break;
//                }
//            }
//        }
//        for (Long vsId : result.keySet()) {
//            final Long vs = vsId;
//            List<Group> groups = result.get(vs);
//            Collections.sort(groups, new Comparator<Group>() {
//                public int compare(Group group0, Group group1) {
//                    if (vsGroupPriority.get("VS" + vs + "_" + group1.getId()) == vsGroupPriority.get("VS" + vs + "_" + group0.getId())) {
//                        return (int) (group1.getId() - group0.getId());
//                    }
//                    return vsGroupPriority.get("VS" + vs + "_" + group1.getId()) - vsGroupPriority.get("VS" + vs + "_" + group0.getId());
//                }
//            });
//        }
//        return result;
//    }

    @Override
    public boolean build(Slb onlineSlb,
                      Map<Long, VirtualServer> onlineVses,
                      Set<Long> needBuildVses,
                      Set<Long> deactivateVses,
                      Map<Long, List<Group>> vsGroups,
                      Set<String> allDownServers,
                      Set<String> allUpGroupServers
    ) throws Exception {
        int version = buildInfoService.getTicket(onlineSlb.getId());
        int currentVersion = buildInfoService.getCurrentTicket(onlineSlb.getId());

        String conf = nginxConfigBuilder.generateNginxConf(onlineSlb);
        nginxConfDao.insert(new NginxConfDo().setCreatedTime(new Date())
                .setSlbId(onlineSlb.getId())
                .setContent(conf)
                .setVersion(version));
        logger.debug("Nginx Conf build sucess! slbId: " + onlineSlb.getId() + ",version: " + version);


        List<NginxConfServerDo> nginxConfServerDoList = nginxConfServerDao.findAllBySlbIdAndVersion(onlineSlb.getId(), currentVersion, NginxConfServerEntity.READSET_FULL);
        List<NginxConfUpstreamDo> nginxConfUpstreamDoList = nginxConfUpstreamDao.findAllBySlbIdAndVersion(onlineSlb.getId(), currentVersion, NginxConfUpstreamEntity.READSET_FULL);
        Map<Long, NginxConfServerDo> nginxConfServerDoMap = new HashMap<>();
        Map<Long, NginxConfUpstreamDo> nginxConfUpstreamDoMap = new HashMap<>();

        for (Long vsId : needBuildVses) {
            if (deactivateVses.contains(vsId)) {
                continue;
            }
            VirtualServer virtualServer = onlineVses.get(vsId);
            List<Group> groups = vsGroups.get(vsId);
            if (groups == null) {
                groups = new ArrayList<>();
            }

            String serverConf = nginxConfigBuilder.generateServerConf(onlineSlb, virtualServer, groups);
            String upstreamConf = nginxConfigBuilder.generateUpstreamsConf(onlineSlb, virtualServer, groups, allDownServers, allUpGroupServers);

            nginxConfServerDoMap.put(vsId, new NginxConfServerDo().setCreatedTime(new Date())
                    .setSlbId(onlineSlb.getId())
                    .setSlbVirtualServerId(vsId)
                    .setContent(serverConf)
                    .setVersion(version));

            nginxConfUpstreamDoMap.put(vsId, new NginxConfUpstreamDo().setCreatedTime(new Date())
                    .setSlbId(onlineSlb.getId())
                    .setSlbVirtualServerId(vsId)
                    .setContent(upstreamConf)
                    .setVersion(version));
        }

        for (NginxConfServerDo nginxConfServerDo : nginxConfServerDoList) {
            if (deactivateVses.contains(nginxConfServerDo.getSlbVirtualServerId())) {
                continue;
            }
            if (!nginxConfServerDoMap.containsKey(nginxConfServerDo.getSlbVirtualServerId())) {
                nginxConfServerDoMap.put(nginxConfServerDo.getSlbVirtualServerId(), nginxConfServerDo.setVersion(version));
            }
        }
        for (NginxConfUpstreamDo nginxConfUpstreamDo : nginxConfUpstreamDoList) {
            if (deactivateVses.contains(nginxConfUpstreamDo.getSlbVirtualServerId())) {
                continue;
            }
            if (!nginxConfUpstreamDoMap.containsKey(nginxConfUpstreamDo.getSlbVirtualServerId())) {
                nginxConfUpstreamDoMap.put(nginxConfUpstreamDo.getSlbVirtualServerId(), nginxConfUpstreamDo.setVersion(version));
            }
        }
        nginxConfServerDao.insert(nginxConfServerDoMap.values().toArray(new NginxConfServerDo[]{}));
        nginxConfUpstreamDao.insert(nginxConfUpstreamDoMap.values().toArray(new NginxConfUpstreamDo[]{}));
        return true;
    }


    public DyUpstreamOpsData buildUpstream(Long slbId,
                                           VirtualServer virtualServer,
                                           Set<String> allDownServers,
                                           Set<String> allUpGroupServers,
                                           Group group) throws Exception {
        String upstreambody = UpstreamsConf.buildUpstreamConfBody(null, virtualServer, group, allDownServers, allUpGroupServers);
        String upstreamName = UpstreamsConf.buildUpstreamName(null, virtualServer, group);
        return new DyUpstreamOpsData().setUpstreamCommands(upstreambody).setUpstreamName(upstreamName);
    }

    @Override
    public void rollBackConfig(Long slbId, int version) throws Exception {
        nginxConfServerDao.deleteBySlbIdFromVersion(new NginxConfServerDo().setSlbId(slbId).setVersion(version));
        nginxConfDao.deleteBySlbIdFromVersion(new NginxConfDo().setSlbId(slbId).setVersion(version));
        nginxConfUpstreamDao.deleteBySlbIdFromVersion(new NginxConfUpstreamDo().setSlbId(slbId).setVersion(version));
    }
}
