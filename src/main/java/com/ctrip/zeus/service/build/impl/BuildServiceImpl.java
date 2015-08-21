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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean build(Long slbId , int ticket) throws Exception {
        int paddingTicket = buildInfoService.getPaddingTicket( slbId );
        ticket = paddingTicket>ticket?paddingTicket:ticket;
        if (!buildInfoService.updateTicket(slbId, ticket))
        {
            return false;
        }
        nginxConfService.build(slbId, ticket);
        return  true;
    }

    @Override
    public List<VirtualServer> getNeedBuildVirtualServers(Long slbId,HashMap<Long , Group> activatingGroups , Set<Long>groupList)throws Exception{
        Set<Long> buildVirtualServer = new HashSet<>();
        List<Group> groups = new ArrayList<>();
        List<String> l = activeConfService.getConfGroupActiveContentByGroupIds(groupList.toArray(new Long[]{}));
        for (String content :  l ){
            Group tmpGroup = DefaultSaxParser.parseEntity(Group.class, content);
            if (tmpGroup!=null&&!activatingGroups.containsKey(tmpGroup.getId())) {
                groups.add(tmpGroup);
            }
        }
        groups.addAll(activatingGroups.values());
        for (Group group : groups) {
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getSlbId().equals(slbId))
                {
                    buildVirtualServer.add(gvs.getVirtualServer().getId());
                }
            }
        }
        Slb slb = activateService.getActivatedSlb(slbId);

        List<VirtualServer> result = new ArrayList<>();
        List<VirtualServer> vses = slb.getVirtualServers();
        for (VirtualServer vs : vses){
            if (buildVirtualServer.contains(vs.getId())){
                result.add(vs);
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<Group>> getInfluencedVsGroups(Long slbId,HashMap<Long,Group>activatingGroups,List<VirtualServer>buildVirtualServer,Set<Long> deactivateGroup)throws Exception{
        Map<Long, Map<Long,Integer>> groupMap = new HashMap<>();
        List<ConfGroupSlbActiveDo> groupSlbActiveList = confGroupSlbActiveDao.findBySlbId(slbId , ConfGroupSlbActiveEntity.READSET_FULL);
        if (groupSlbActiveList==null){
            groupSlbActiveList=new ArrayList<>();
        }
        for (ConfGroupSlbActiveDo groupSlb : groupSlbActiveList)
        {
            if (activatingGroups.containsKey(groupSlb.getGroupId())){
                continue;
            }
            if (deactivateGroup.contains(groupSlb.getGroupId())){
                continue;
            }
            long vs = groupSlb.getSlbVirtualServerId();
            Map<Long,Integer> groups = groupMap.get(vs);
            if (groups==null)
            {
                groups = new HashMap<>();
                groupMap.put(vs,groups);
            }

            groups.put(groupSlb.getGroupId(),groupSlb.getPriority());
        }
        for (Long id : activatingGroups.keySet()){
            List<GroupVirtualServer> groupVirtualServers=activatingGroups.get(id).getGroupVirtualServers();
            for (GroupVirtualServer gvs : groupVirtualServers){
                if (!gvs.getVirtualServer().getSlbId().equals(slbId)){
                    continue;
                }
                Long  vsid = gvs.getVirtualServer().getId();
                Map<Long,Integer> groups = groupMap.get(vsid);
                if (groups==null)
                {
                    groups = new HashMap<>();
                    groupMap.put(vsid,groups);
                }
                groups.put(id,gvs.getPriority());
            }
        }

        Map<Long, List<Group>> groupsMap = new HashMap<>();
        for (VirtualServer vs : buildVirtualServer){
            final Map<Long,Integer> groupPriorityMap = groupMap.get(vs.getId());

            List<Group> groupList = new ArrayList<>();
            List<Long> groupInDb = new ArrayList<>();
            if (groupPriorityMap==null){
                groupsMap.put(vs.getId(), groupList);
                continue;
            }
            Set<Long> groupIds =groupPriorityMap.keySet();
            for (Long gid : groupIds){
                Group group = activatingGroups.get(gid);
                if (group!=null){
                    groupList.add(group);
                }else {
                    groupInDb.add(gid);
                }
            }
            List<String> l = activeConfService.getConfGroupActiveContentByGroupIds(groupInDb.toArray(new Long[]{}));
            for (String content :  l ){
                Group tmpGroup = DefaultSaxParser.parseEntity(Group.class, content);
                groupList.add(tmpGroup);
            }
            Collections.sort(groupList,new Comparator<Group>(){
                public int compare(Group group0, Group group1) {
                    if (groupPriorityMap.get(group1.getId())==groupPriorityMap.get(group0.getId()))
                    {
                        return (int)(group1.getId()-group0.getId());
                    }
                    return groupPriorityMap.get(group1.getId())-groupPriorityMap.get(group0.getId());
                }
            });
            groupsMap.put(vs.getId(), groupList);
        }
        return groupsMap;
    }

    @Override
    public void build(Long slbId,
                      Slb activatedSlb,
                      List<VirtualServer>buildVirtualServer,
                      Map<Long,List<Group>>groupsMap,
                      Set<String>allDownServers,
                      Set<String>allUpGroupServers
                      )throws Exception{
        int version = buildInfoService.getTicket(slbId);
        int currentVersion = buildInfoService.getCurrentTicket(slbId);
        Slb slb = null;
        if (activatedSlb != null){
            slb = activatedSlb;
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

        for (VirtualServer vs : buildVirtualServer) {
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
        List<Long> slbVirtualServers = new ArrayList<>();
        for (VirtualServer virtualServer: slb.getVirtualServers()){
            slbVirtualServers.add(virtualServer.getId());
        }

        for (NginxConfServerDo nginxConfServerDo : nginxConfServerDoList){
            if (!slbVirtualServers.contains(nginxConfServerDo.getSlbVirtualServerId())){
                continue;
            }
            if (!nginxConfServerDoMap.containsKey(nginxConfServerDo.getSlbVirtualServerId())){
                nginxConfServerDoMap.put(nginxConfServerDo.getSlbVirtualServerId(),nginxConfServerDo.setVersion(version));
            }
        }
        for (NginxConfUpstreamDo nginxConfUpstreamDo : nginxConfUpstreamDoList){
            if (!slbVirtualServers.contains(nginxConfUpstreamDo.getSlbVirtualServerId())){
                continue;
            }
            if (!nginxConfUpstreamDoMap.containsKey(nginxConfUpstreamDo.getSlbVirtualServerId())){
                nginxConfUpstreamDoMap.put(nginxConfUpstreamDo.getSlbVirtualServerId(),nginxConfUpstreamDo.setVersion(version));
            }
        }
        nginxConfServerDao.insert(nginxConfServerDoMap.values().toArray(new NginxConfServerDo[]{}));
        nginxConfUpstreamDao.insert(nginxConfUpstreamDoMap.values().toArray(new NginxConfUpstreamDo[]{}));
    }


    public List<DyUpstreamOpsData> buildUpstream(Long slbId, Set<String>allDownServers ,Set<String> allUpGroupServers,Group group ) throws Exception {
        Slb slb = activateService.getActivatedSlb(slbId);
        AssertUtils.assertNotNull(slb, "Not found slb content by slbId!");
        HashMap<Long,VirtualServer> tmpVirtualServers = new HashMap<>();
        for (VirtualServer virtualServer : slb.getVirtualServers()){
            tmpVirtualServers.put(virtualServer.getId(),virtualServer);
        }
        List<DyUpstreamOpsData> result = new ArrayList<>();

        List<GroupVirtualServer> groupSlbList = group.getGroupVirtualServers();
        VirtualServer vs = null;
        for (GroupVirtualServer groupSlb : groupSlbList )
        {
            if (!tmpVirtualServers.containsKey(groupSlb.getVirtualServer().getId())){
                 continue;
            }
            vs = tmpVirtualServers.get(groupSlb.getVirtualServer().getId());
            String upstreambody = UpstreamsConf.buildUpstreamConfBody(slb, vs, group, allDownServers, allUpGroupServers);
            String upstreamName = UpstreamsConf.buildUpstreamName(slb,vs,group);
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
