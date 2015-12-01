package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import com.ctrip.zeus.service.model.AutoFiller;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.util.AssertUtils;
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
    private ActiveConfService activeConfService;
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
    private ActivateService activateService;
    @Resource
    private AutoFiller autoFiller;
    @Resource
    private ConfGroupActiveDao confGroupActiveDao;


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<Long,VirtualServer> getNeedBuildVirtualServers(Long slbId,
                                                          Map<Long,VirtualServer> activatingVses,
                                                          Map<Long,VirtualServer> activatedVses,
                                                          HashMap<Long , Group> activatingGroups ,
                                                          Set<Long>groupList) throws Exception {
        Set<Long> buildVirtualServer = new HashSet<>();
        List<Group> groups = new ArrayList<>();
        List<Group> activatedGroups = activateService.getActivatedGroups(groupList.toArray(new Long[]{}),slbId);
        groups.addAll(activatedGroups);
        groups.addAll(activatingGroups.values());
        Map<Long,VirtualServer> allActivatedVses;
        if (activatedVses!=null){
            allActivatedVses = activatedVses;
        }else {
            allActivatedVses = activateService.getActivatedVirtualServerBySlb(slbId);
        }
        for (Group group : groups) {
            boolean flag = false ;
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (allActivatedVses.containsKey(gvs.getVirtualServer().getId())||activatingVses.containsKey(gvs.getVirtualServer().getId()))
                {
                    flag = true ;
                    buildVirtualServer.add(gvs.getVirtualServer().getId());
                }
            }
            if (!flag){
                throw new Exception("Not Found Related Vs for Group:"+group.getId());
            }
        }
        if (activatedVses != null){
            buildVirtualServer.addAll(activatedVses.keySet());
        }
        buildVirtualServer.addAll(activatingVses.keySet());
        Map<Long,VirtualServer> result = new HashMap<>();
        for (Long vsId : buildVirtualServer){
            if (activatingVses.containsKey(vsId)){
                result.put(vsId,activatingVses.get(vsId));
            }else if (allActivatedVses.containsKey(vsId)){
                result.put(vsId,allActivatedVses.get(vsId));
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<Group>> getInfluencedVsGroups(Long slbId,
                                                        HashMap<Long,Group>activatingGroups,
                                                        Map<Long,VirtualServer> buildVirtualServer,
                                                        Set<Long> deactivateGroup)throws Exception{
        Map<Long, List<Group>> result = new HashMap<>();
        Map<Long,List<Group>> dbGroups = activateService.getActivatedGroupsByVses(buildVirtualServer.keySet().toArray(new Long[]{}));
        final Map<String,Integer> vsGroupPriority = new HashMap<>();
        for (Long vsId : buildVirtualServer.keySet()){
            List<Group> activatedGroups = dbGroups.get(vsId);
            List<Group> groupList = result.get(vsId);
            if (groupList == null){
                groupList = new ArrayList<>();
                result.put(vsId,groupList);
            }
            if (activatedGroups == null){
                continue;
            }
            for (Group g : activatedGroups){
                if (deactivateGroup.contains(g.getId())){
                    continue;
                }
                if (activatingGroups.containsKey(g.getId())){
                    continue;
                }
                for (GroupVirtualServer gv : g.getGroupVirtualServers()){
                    if (gv.getVirtualServer().getId().equals(vsId)){
                        vsGroupPriority.put("VS"+vsId+"_"+g.getId(), gv.getPriority());
                        groupList.add(g);
                    }
                }
            }
        }
        for (Long gid : activatingGroups.keySet()){
            if (deactivateGroup.contains(gid)){
                continue;
            }
            Group g = activatingGroups.get(gid);
            for (GroupVirtualServer gv : g.getGroupVirtualServers()){
                if (buildVirtualServer.containsKey(gv.getVirtualServer().getId())){
                    List<Group> list = result.get(gv.getVirtualServer().getId());
                    if (list == null){
                        list = new ArrayList<>();
                        result.put(gv.getVirtualServer().getId(),list);
                    }
                    list.add(g);
                    vsGroupPriority.put("VS"+gv.getVirtualServer().getId()+"_"+g.getId(), gv.getPriority());
                    break;
                }
            }
        }
        for (Long vsId : result.keySet()){
            final Long vs = vsId;
            List<Group> groups = result.get(vs);
            Collections.sort(groups,new Comparator<Group>(){
                public int compare(Group group0, Group group1) {
                    if (vsGroupPriority.get("VS"+vs+"_"+group1.getId())==vsGroupPriority.get("VS"+vs+"_"+group0.getId()))
                    {
                        return (int)(group1.getId()-group0.getId());
                    }
                    return vsGroupPriority.get("VS"+vs+"_"+group1.getId())-vsGroupPriority.get("VS"+vs+"_"+group0.getId());
                }
            });
        }
        return result;
    }

    @Override
    public void build(Long slbId,
                      Slb activatingSlb,
                      Map<Long,VirtualServer>buildVirtualServer,
                      Set<Long> deactivateVses,
                      Map<Long,List<Group>>groupsMap,
                      Set<String>allDownServers,
                      Set<String>allUpGroupServers
                      )throws Exception{
        int version = buildInfoService.getTicket(slbId);
        int currentVersion = buildInfoService.getCurrentTicket(slbId);
        Slb slb = null;
        if (activatingSlb != null){
            slb = activatingSlb;
        }else{
            slb = activateService.getActivatedSlb(slbId);
        }

        String conf = nginxConfigBuilder.generateNginxConf(slb);
        nginxConfDao.insert(new NginxConfDo().setCreatedTime(new Date())
                .setSlbId(slb.getId())
                .setContent(conf)
                .setVersion(version));
        logger.debug("Nginx Conf build sucess! slbName: "+slb+",version: "+version);


        List<NginxConfServerDo> nginxConfServerDoList = nginxConfServerDao.findAllBySlbIdAndVersion(slbId,currentVersion,NginxConfServerEntity.READSET_FULL);
        List<NginxConfUpstreamDo> nginxConfUpstreamDoList = nginxConfUpstreamDao.findAllBySlbIdAndVersion(slbId,currentVersion,NginxConfUpstreamEntity.READSET_FULL);
        Map <Long , NginxConfServerDo> nginxConfServerDoMap = new HashMap<>();
        Map<Long,NginxConfUpstreamDo> nginxConfUpstreamDoMap = new HashMap<>();

        for (VirtualServer vs : buildVirtualServer.values()) {
            List<Group> groups = groupsMap.get(vs.getId());
            if (groups == null) {
                groups = new ArrayList<>();
            }

            String serverConf = nginxConfigBuilder.generateServerConf(slb, vs, groups);
            String upstreamConf = nginxConfigBuilder.generateUpstreamsConf(slb, vs, groups, allDownServers, allUpGroupServers);

            nginxConfServerDoMap.put(vs.getId(), new NginxConfServerDo().setCreatedTime(new Date())
                    .setSlbId(slb.getId())
                    .setSlbVirtualServerId(vs.getId())
                    .setContent(serverConf)
                    .setVersion(version));

            nginxConfUpstreamDoMap.put(vs.getId(),new NginxConfUpstreamDo().setCreatedTime(new Date())
                    .setSlbId(slb.getId())
                    .setSlbVirtualServerId(vs.getId())
                    .setContent(upstreamConf)
                    .setVersion(version));
        }

        for (NginxConfServerDo nginxConfServerDo : nginxConfServerDoList){
            if (deactivateVses.contains(nginxConfServerDo.getSlbVirtualServerId())){
                continue;
            }
            if (!nginxConfServerDoMap.containsKey(nginxConfServerDo.getSlbVirtualServerId())){
                nginxConfServerDoMap.put(nginxConfServerDo.getSlbVirtualServerId(),nginxConfServerDo.setVersion(version));
            }
        }
        for (NginxConfUpstreamDo nginxConfUpstreamDo : nginxConfUpstreamDoList){
            if (deactivateVses.contains(nginxConfUpstreamDo.getSlbVirtualServerId())){
                continue;
            }
            if (!nginxConfUpstreamDoMap.containsKey(nginxConfUpstreamDo.getSlbVirtualServerId())){
                nginxConfUpstreamDoMap.put(nginxConfUpstreamDo.getSlbVirtualServerId(),nginxConfUpstreamDo.setVersion(version));
            }
        }
        nginxConfServerDao.insert(nginxConfServerDoMap.values().toArray(new NginxConfServerDo[]{}));
        nginxConfUpstreamDao.insert(nginxConfUpstreamDoMap.values().toArray(new NginxConfUpstreamDo[]{}));
    }


    public List<DyUpstreamOpsData> buildUpstream(Long slbId,
                                                 Map<Long,VirtualServer> buildVirtualServer,
                                                 Set<String>allDownServers ,
                                                 Set<String> allUpGroupServers,
                                                 Group group ) throws Exception {
        List<DyUpstreamOpsData> result = new ArrayList<>();

        List<GroupVirtualServer> groupSlbList = group.getGroupVirtualServers();
        VirtualServer vs = null;
        for (GroupVirtualServer groupSlb : groupSlbList )
        {
            if (!buildVirtualServer.containsKey(groupSlb.getVirtualServer().getId())){
                 continue;
            }
            vs = buildVirtualServer.get(groupSlb.getVirtualServer().getId());
            String upstreambody = UpstreamsConf.buildUpstreamConfBody(null, vs, group, allDownServers, allUpGroupServers);
            String upstreamName = UpstreamsConf.buildUpstreamName(null,vs,group);
            result.add(new DyUpstreamOpsData().setUpstreamCommands(upstreambody).setUpstreamName(upstreamName));
        }

        return result;
    }

    @Override
    public void rollBackConfig(Long slbId, int version) throws Exception {
        nginxConfServerDao.deleteBySlbIdFromVersion(new NginxConfServerDo().setSlbId(slbId).setVersion(version));
        nginxConfDao.deleteBySlbIdFromVersion(new NginxConfDo().setSlbId(slbId).setVersion(version));
        nginxConfUpstreamDao.deleteBySlbIdFromVersion(new NginxConfUpstreamDo().setSlbId(slbId).setVersion(version));
    }
}
