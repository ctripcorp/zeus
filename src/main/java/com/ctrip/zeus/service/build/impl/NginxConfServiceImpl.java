package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.Activate.ActiveConfService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.util.AssertUtils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
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
    private SlbRepository slbClusterRepository;

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
    public String getNginxConf(String slbName , int _version) throws Exception {
        int version = getCurrentVersion(slbName);
        if (version <= _version)
        {
            return  nginxConfDao.findBySlbNameAndVersion(slbName,version, NginxConfEntity.READSET_FULL).getContent();
        }else
        {
            NginxConfDo confdo = null;
            while (confdo==null && _version>0)
            {
                confdo = nginxConfDao.findBySlbNameAndVersion(slbName,_version, NginxConfEntity.READSET_FULL);
                _version--;
            }

            if (confdo!=null)
                return confdo.getContent();
            else
                return null;
        }

    }


    @Override
    public  List<NginxConfServerData> getNginxConfServer(String slbName , int _version) throws Exception {

        int version = getCurrentVersion(slbName);

        List<NginxConfServerData> r = new ArrayList<>();

        if (version <= _version)
        {
            List<NginxConfServerDo> d = nginxConfServerDao.findAllBySlbNameAndVersion(slbName, version, NginxConfServerEntity.READSET_FULL);

            for (NginxConfServerDo t : d)
            {
                r.add(new NginxConfServerData().setName(t.getName()).setContent(t.getContent()));
            }

            return r;

        }else {

            List<NginxConfServerDo> d = null ;

            while (d == null&&_version>0)
            {
                d = nginxConfServerDao.findAllBySlbNameAndVersion(slbName, _version, NginxConfServerEntity.READSET_FULL);
                _version--;
            }

            if (d!=null)
            {
                for (NginxConfServerDo t : d)
                {
                    r.add(new NginxConfServerData().setName(t.getName()).setContent(t.getContent()));
                }

                return r;

            }else
            {
                return null;
            }
        }

    }
    @Override
    public List<NginxConfUpstreamData> getNginxConfUpstream(String slbName , int _version) throws Exception {
        int version = getCurrentVersion(slbName);

        List<NginxConfUpstreamData> r = new ArrayList<>();

        if (version <= _version){

            List<NginxConfUpstreamDo> d = nginxConfUpstreamDao.findAllBySlbNameAndVersion(slbName, version, NginxConfUpstreamEntity.READSET_FULL);

            for (NginxConfUpstreamDo t : d)
            {
                r.add(new NginxConfUpstreamData().setName(t.getName()).setContent(t.getContent()));
            }

            return r;

        }else
        {
            List<NginxConfUpstreamDo> d = null;

            while (d == null && _version>0)
            {
                d = nginxConfUpstreamDao.findAllBySlbNameAndVersion(slbName, _version, NginxConfUpstreamEntity.READSET_FULL);
                _version--;
            }

            if (d!=null)
            {
                for (NginxConfUpstreamDo t : d)
                {
                    r.add(new NginxConfUpstreamData().setName(t.getName()).setContent(t.getContent()));
                }

                return r;
            }else
            {
                return null;
            }
        }

    }

    @Override
    public int getCurrentVersion(String slbname) throws Exception {
        return buildInfoService.getCurrentTicket(slbname);
    }


    @Override
    public List<DyUpstreamOpsData> buildUpstream(Slb slb, String appName) throws Exception {

        List<DyUpstreamOpsData> result = new ArrayList<>();

        Set<String> allDownServers = statusService.findAllDownServers();
        Set<String> allDownAppServers = statusService.findAllDownAppServersBySlbName(slb.getName());

        List<String> appactiveconf =activeConfService.getConfAppActiveContentByAppNames(new String[]{appName});

        if (appactiveconf.size()!=1){ throw new Exception(appName+" is not activated!");}

        App app = DefaultSaxParser.parseEntity(App.class, appactiveconf.get(0));

        List<AppSlb> appslbList = app.getAppSlbs();
        VirtualServer vs = null;
        for (AppSlb appSlb : appslbList )
        {
            vs  = appSlb.getVirtualServer();

            String upstreambody = UpstreamsConf.buildUpstreamConfBody(slb,vs,app,allDownServers,allDownAppServers);
            String upstreamName = UpstreamsConf.buildUpstreamName(slb,vs,app);
            result.add(new DyUpstreamOpsData().setUpstreamCommands(upstreambody).setUpstreamName(upstreamName));
        }

        return result;
    }

    @Override
    public void build( Long slbId, int version) throws Exception {

        Map<Long, Map<Long,Integer>> appNamesMap = new HashMap<>();


        List<ConfGroupSlbActiveDo> groupSlbActiveList = confGroupSlbActiveDao.findBySlbId(slbId ,ConfGroupSlbActiveEntity.READSET_FULL);
        if (groupSlbActiveList==null){
            groupSlbActiveList=new ArrayList<>();
        }

        for (ConfGroupSlbActiveDo groupSlb : groupSlbActiveList)
        {
            long vs = groupSlb.getSlbVirtualServerId();
            Map<Long,Integer> groups = appNamesMap.get(vs);
            if (groups==null)
            {
                groups = new HashMap<>();
                appNamesMap.put(vs,groups);
            }

            groups.put(groupSlb.getGroupId(),groupSlb.getPriority());
        }


        Map<Long, List<Group>> groupsMap = new HashMap<>();
        for (Long vs : appNamesMap.keySet()) {
            final Map<Long,Integer> groupPriorityMap = appNamesMap.get(vs);

            List<String> l = activeConfService.getConfGroupActiveContentByGroupIds(groupPriorityMap.keySet().toArray(new Long[]{}));
            List<Group> groupList = new ArrayList<>();
            for (String content :  l ){
                groupList.add(DefaultSaxParser.parseEntity(Group.class, content));
            }

            Collections.sort(groupList,new Comparator<Group>(){
                public int compare(Group group0, Group group1) {
                    return groupPriorityMap.get(group0.getName())-groupPriorityMap.get(group1.getName());
                }
            });
            groupsMap.put(vs, groupList);
        }

        String slbContent =activeConfService.getConfSlbActiveContentBySlbId(slbId);
        AssertUtils.isNull(slbContent,"SlbID: ["+slbId+"] has not submit or submit failed!");

        Slb slb = DefaultSaxParser.parseEntity(Slb.class, slbContent);

        String conf = nginxConfigBuilder.generateNginxConf(slb);
        nginxConfDao.insert(new NginxConfDo().setCreatedTime(new Date())
                .setSlbId(slb.getId())
                .setContent(conf)
                .setVersion(version));
        logger.debug("Nginx Conf build sucess! slbName: "+slb+",version: "+version);


        Set<String> allDownServers = statusService.findAllDownServers();
        Set<String> allDownAppServers = statusService.findAllDownAppServersBySlbName(slbName);

        int length = slb.getVirtualServers().size();
        NginxConfServerDo[] nginxConfServerDos = new NginxConfServerDo[length];
        NginxConfUpstreamDo[] nginxConfUpstreamDos = new NginxConfUpstreamDo[length];
        int index = 0 ;

        for (VirtualServer vs : slb.getVirtualServers()) {
            List<App> apps = appsMap.get(vs.getName());
            if (apps == null) {
                apps = new ArrayList<>();
            }

            String serverConf = nginxConfigBuilder.generateServerConf(slb, vs, apps);
            String upstreamConf = nginxConfigBuilder.generateUpstreamsConf(slb, vs, apps, allDownServers, allDownAppServers);

            nginxConfServerDos[index] = new NginxConfServerDo().setCreatedTime(new Date())
                    .setSlbName(slb.getName())
                    .setName(vs.getName())
                    .setContent(serverConf)
                    .setVersion(version);

            nginxConfUpstreamDos[index] = new NginxConfUpstreamDo().setCreatedTime(new Date())
                    .setSlbName(slb.getName())
                    .setName(vs.getName())
                    .setContent(upstreamConf)
                    .setVersion(version);
            index++;
            logger.debug("Nginx Server Conf build sucess! slbName: "+slb+",virtualserver: "+vs.getName()+",version: "+version);
            logger.debug("Nginx Upstream Conf build sucess! slbName: "+slb+",virtualserver: "+vs.getName()+",version: "+version);
        }
        nginxConfServerDao.insert(nginxConfServerDos);
        nginxConfUpstreamDao.insert(nginxConfUpstreamDos);
    }
}
