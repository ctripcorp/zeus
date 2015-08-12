package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
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
@Service("nginxConfService")
public class NginxConfServiceImpl implements NginxConfService {

    @Resource
    private NginxConfDao nginxConfDao;
    @Resource
    private NginxConfServerDao nginxConfServerDao;
    @Resource
    private NginxConfUpstreamDao nginxConfUpstreamDao;

    @Resource
    private NginxConfBuilder nginxConfigBuilder;

    @Resource
    private StatusService statusService;

    @Resource
    private BuildInfoService buildInfoService;
    @Resource
    private ActiveConfService activeConfService;
    @Resource
    ConfGroupSlbActiveDao confGroupSlbActiveDao;


    private Logger logger = LoggerFactory.getLogger(NginxConfServiceImpl.class);

    @Override
    public String getNginxConf(Long slbId, int _version) throws Exception {
        int version = getCurrentBuildingVersion(slbId);
        if (version <= _version)
        {
            return  nginxConfDao.findBySlbIdAndVersion(slbId,version, NginxConfEntity.READSET_FULL).getContent();
        }else
        {
            NginxConfDo confdo = null;
            while (confdo==null && _version>0)
            {
                confdo = nginxConfDao.findBySlbIdAndVersion(slbId,_version, NginxConfEntity.READSET_FULL);
                _version--;
            }

            if (confdo!=null)
                return confdo.getContent();
            else
                return null;
        }

    }


    @Override
    public  List<NginxConfServerData> getNginxConfServer(Long slbId, int _version) throws Exception {

        int version = getCurrentBuildingVersion(slbId);

        List<NginxConfServerData> r = new ArrayList<>();

        if (version <= _version)
        {
            List<NginxConfServerDo> d = nginxConfServerDao.findAllBySlbIdAndVersion(slbId, version, NginxConfServerEntity.READSET_FULL);

            for (NginxConfServerDo t : d)
            {
                r.add(new NginxConfServerData().setVsId(t.getSlbVirtualServerId()).setContent(t.getContent()));
            }

            return r;

        }else {

            List<NginxConfServerDo> d = null ;

            while (d == null&&_version>0)
            {
                d = nginxConfServerDao.findAllBySlbIdAndVersion(slbId, _version, NginxConfServerEntity.READSET_FULL);
                _version--;
            }

            if (d!=null)
            {
                for (NginxConfServerDo t : d)
                {
                    r.add(new NginxConfServerData().setVsId(t.getSlbVirtualServerId()).setContent(t.getContent()));
                }

                return r;

            }else
            {
                return null;
            }
        }

    }
    @Override
    public List<NginxConfUpstreamData> getNginxConfUpstream(Long slbId , int _version) throws Exception {
        int version = getCurrentBuildingVersion(slbId);

        List<NginxConfUpstreamData> r = new ArrayList<>();

        if (version <= _version){

            List<NginxConfUpstreamDo> d = nginxConfUpstreamDao.findAllBySlbIdAndVersion(slbId, version, NginxConfUpstreamEntity.READSET_FULL);

            for (NginxConfUpstreamDo t : d)
            {
                r.add(new NginxConfUpstreamData().setVsId(t.getSlbVirtualServerId()).setContent(t.getContent()));
            }

            return r;

        }else
        {
            List<NginxConfUpstreamDo> d = null;

            while (d == null && _version>0)
            {
                d = nginxConfUpstreamDao.findAllBySlbIdAndVersion(slbId, _version, NginxConfUpstreamEntity.READSET_FULL);
                _version--;
            }

            if (d!=null)
            {
                for (NginxConfUpstreamDo t : d)
                {
                    r.add(new NginxConfUpstreamData().setVsId(t.getSlbVirtualServerId()).setContent(t.getContent()));
                }

                return r;
            }else
            {
                return null;
            }
        }

    }

    @Override
    public int getCurrentBuildingVersion(Long slbId) throws Exception {
        return buildInfoService.getPaddingTicket(slbId);
    }

    @Override
    public int getCurrentVersion(Long slbId) throws Exception {
        return buildInfoService.getCurrentTicket(slbId);
    }

    @Override
    public void build( Long slbId, int version) throws Exception {

        Map<Long, Map<Long,Integer>> groupNamesMap = new HashMap<>();


        List<ConfGroupSlbActiveDo> groupSlbActiveList = confGroupSlbActiveDao.findBySlbId(slbId ,ConfGroupSlbActiveEntity.READSET_FULL);
        if (groupSlbActiveList==null){
            groupSlbActiveList=new ArrayList<>();
        }

        for (ConfGroupSlbActiveDo groupSlb : groupSlbActiveList)
        {
            long vs = groupSlb.getSlbVirtualServerId();
            Map<Long,Integer> groups = groupNamesMap.get(vs);
            if (groups==null)
            {
                groups = new HashMap<>();
                groupNamesMap.put(vs,groups);
            }

            groups.put(groupSlb.getGroupId(),groupSlb.getPriority());
        }


        Map<Long, List<Group>> groupsMap = new HashMap<>();
        for (Long vs : groupNamesMap.keySet()) {
            final Map<Long,Integer> groupPriorityMap = groupNamesMap.get(vs);

            List<String> l = activeConfService.getConfGroupActiveContentByGroupIds(groupPriorityMap.keySet().toArray(new Long[]{}));
            List<Group> groupList = new ArrayList<>();
            for (String content :  l ){
                groupList.add(DefaultSaxParser.parseEntity(Group.class, content));
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
            groupsMap.put(vs, groupList);
        }

        String slbContent =activeConfService.getConfSlbActiveContentBySlbId(slbId);
        AssertUtils.assertNotNull(slbContent, "Not found slb content by slbId!");

        Slb slb = DefaultSaxParser.parseEntity(Slb.class, slbContent);

        String conf = nginxConfigBuilder.generateNginxConf(slb);
        nginxConfDao.insert(new NginxConfDo().setCreatedTime(new Date())
                .setSlbId(slb.getId())
                .setContent(conf)
                .setVersion(version));
        logger.debug("Nginx Conf build sucess! slbName: "+slb+",version: "+version);


        Set<String> allDownServers = statusService.findAllDownServers();
        Set<String> allUpGroupServers = statusService.findAllUpGroupServersBySlbId(slbId);

        int length = slb.getVirtualServers().size();
        NginxConfServerDo[] nginxConfServerDos = new NginxConfServerDo[length];
        NginxConfUpstreamDo[] nginxConfUpstreamDos = new NginxConfUpstreamDo[length];
        int index = 0 ;

        for (VirtualServer vs : slb.getVirtualServers()) {
            List<Group> groups = groupsMap.get(vs.getId());
            if (groups == null) {
                groups = new ArrayList<>();
            }

            String serverConf = nginxConfigBuilder.generateServerConf(slb, vs, groups);
            String upstreamConf = nginxConfigBuilder.generateUpstreamsConf(slb, vs, groups, allDownServers, allUpGroupServers);

            nginxConfServerDos[index] = new NginxConfServerDo().setCreatedTime(new Date())
                    .setSlbId(slb.getId())
                    .setSlbVirtualServerId(vs.getId())
                    .setContent(serverConf)
                    .setVersion(version);

            nginxConfUpstreamDos[index] = new NginxConfUpstreamDo().setCreatedTime(new Date())
                    .setSlbId(slb.getId())
                    .setSlbVirtualServerId(vs.getId())
                    .setContent(upstreamConf)
                    .setVersion(version);
            index++;
            logger.debug("Nginx Server Conf build sucess! slbName: "+slb+",virtualserver: "+vs.getId()+",version: "+version);
            logger.debug("Nginx Upstream Conf build sucess! slbName: "+slb+",virtualserver: "+vs.getId()+",version: "+version);
        }
        nginxConfServerDao.insert(nginxConfServerDos);
        nginxConfUpstreamDao.insert(nginxConfUpstreamDos);
    }
}
