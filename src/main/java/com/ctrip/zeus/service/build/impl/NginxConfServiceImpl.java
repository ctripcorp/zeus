package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.Activate.ActiveConfService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.NginxConfService;
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
    public void build(String slbName, int version) throws Exception {

        Map<String, Set<String>> appNamesMap = new HashMap<>();

        List<AppSlb> appSlbList = slbClusterRepository.listAppSlbsBySlb(slbName);

        if (appSlbList==null)
        {
            appSlbList = new ArrayList<>();
        }

        for (AppSlb appslb : appSlbList)
        {
            VirtualServer vs = appslb.getVirtualServer();
            String vsstr = vs.getName();

            Set<String> apps = appNamesMap.get(vsstr);
            if (apps==null)
            {
                apps = new HashSet<>();
                appNamesMap.put(vsstr,apps);
            }

            apps.add(appslb.getAppName());
        }


        Map<String, List<App>> appsMap = new HashMap<>();
        for (String vs : appNamesMap.keySet()) {
            List<String> l = activeConfService.getConfAppActiveContentByAppNames(appNamesMap.get(vs).toArray(new String[]{}));
            appsMap.put(vs, Lists.transform(l, new Function<String, App>() {
                @Nullable
                @Override
                public App apply(@Nullable String content) {
                    try {
                        return DefaultSaxParser.parseEntity(App.class, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("DefaultSaxParser fail! Class: App ,Content: ["+content+']',e);
                    }
                }
            }));
        }

        String slbContent =activeConfService.getConfSlbActiveContentBySlbNames(slbName);
        AssertUtils.isNull(slbContent,"Slb: ["+slbName+"] has not submit or submit failed!");

        Slb slb = DefaultSaxParser.parseEntity(Slb.class, slbContent);

        String conf = nginxConfigBuilder.generateNginxConf(slb);
        nginxConfDao.insert(new NginxConfDo().setCreatedTime(new Date())
                .setName(slb.getName())
                .setContent(conf)
                .setVersion(version));
        logger.debug("Nginx Conf build sucess! slbName: "+slb+",version: "+version);


        Set<String> allDownServers = statusService.findAllDownServers();
        Set<String> allDownAppServers = statusService.findAllDownAppServersBySlbName(slbName);
        for (VirtualServer vs : slb.getVirtualServers()) {
            List<App> apps = appsMap.get(vs.getName());
            if (apps == null) {
                apps = new ArrayList<>();
            }

            String serverConf = nginxConfigBuilder.generateServerConf(slb, vs, apps);
            String upstreamConf = nginxConfigBuilder.generateUpstreamsConf(slb, vs, apps, allDownServers, allDownAppServers);

            nginxConfServerDao.insert(new NginxConfServerDo().setCreatedTime(new Date())
                    .setSlbName(slb.getName())
                    .setName(vs.getName())
                    .setContent(serverConf)
                    .setVersion(version));

            logger.debug("Nginx Server Conf build sucess! slbName: "+slb+",virtualserver: "+vs.getName()+",version: "+version);

            nginxConfUpstreamDao.insert(new NginxConfUpstreamDo().setCreatedTime(new Date())
                    .setSlbName(slb.getName())
                    .setName(vs.getName())
                    .setContent(upstreamConf)
                    .setVersion(version));
            logger.debug("Nginx Upstream Conf build sucess! slbName: "+slb+",virtualserver: "+vs.getName()+",version: "+version);
        }

    }
}
