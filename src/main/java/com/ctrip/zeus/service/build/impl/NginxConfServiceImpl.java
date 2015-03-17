package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.nginx.NginxConfBuilder;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.status.StatusService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Service("nginxConfService")
public class NginxConfServiceImpl implements NginxConfService {
    @Resource
    private ConfAppActiveDao confAppActiveDao;
    @Resource
    private ConfSlbActiveDao confSlbActiveDao;

    @Resource
    private NginxConfDao nginxConfDao;
    @Resource
    private NginxConfServerDao nginxConfServerDao;
    @Resource
    private NginxConfUpstreamDao nginxConfUpstreamDao;

    @Resource
    private AppSlbDao appSlbDao;

    @Resource
    private NginxConfBuilder nginxConfBuilder;

    @Resource
    private StatusService statusService;

    @Override
    public void build(String slbName, int version) throws DalException, IOException, SAXException {

        Map<String, Set<String>> appNamesMap = new HashMap<>();
        List<AppSlbDo> list = appSlbDao.findAllBySlb(slbName, AppSlbEntity.READSET_FULL);
        for (AppSlbDo d : list) {
            String vs = d.getSlbVirtualServerName();

            Set<String> apps = appNamesMap.get(vs);
            if (apps == null) {
                apps = new HashSet<>();
                appNamesMap.put(vs, apps);
            }
            apps.add(d.getAppName());
        }

        Map<String, List<App>> appsMap = new HashMap<>();
        for (String vs : appNamesMap.keySet()) {
            List<ConfAppActiveDo> l = confAppActiveDao.findAllByNames(appNamesMap.get(vs).toArray(new String[]{}), ConfAppActiveEntity.READSET_FULL);
            appsMap.put(vs, Lists.transform(l, new Function<ConfAppActiveDo, App>() {
                @Nullable
                @Override
                public App apply(@Nullable ConfAppActiveDo confAppActiveDo) {
                    try {
                        return DefaultSaxParser.parseEntity(App.class, confAppActiveDo.getContent());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }));
        }


        ConfSlbActiveDo d = confSlbActiveDao.findByName(slbName, ConfSlbActiveEntity.READSET_FULL);
        Slb slb = DefaultSaxParser.parseEntity(Slb.class, d.getContent());

        String conf = nginxConfBuilder.generateNginxConf(slb);
        nginxConfDao.insert(new NginxConfDo().setCreatedTime(new Date())
                .setName(slb.getName())
                .setContent(conf)
                .setVersion(version));


        Set<String> allDownServers = statusService.findAllDownServers();
        Set<String> allDownAppServers = statusService.findAllDownAppServers(slbName);
        for (VirtualServer vs : slb.getVirtualServers()) {
            List<App> apps = appsMap.get(vs.getName());
            if (apps == null) {
                apps = new ArrayList<>();
            }

            String serverConf = nginxConfBuilder.generateServerConf(slb, vs, apps);
            String upstreamConf = nginxConfBuilder.generateUpstreamsConf(slb, vs, apps, allDownServers, allDownAppServers);

            nginxConfServerDao.insert(new NginxConfServerDo().setCreatedTime(new Date())
                    .setSlbName(slb.getName())
                    .setName(vs.getName())
                    .setContent(serverConf)
                    .setVersion(version));

            nginxConfUpstreamDao.insert(new NginxConfUpstreamDo().setCreatedTime(new Date())
                    .setSlbName(slb.getName())
                    .setName(vs.getName())
                    .setContent(upstreamConf)
                    .setVersion(version));
        }

    }
}
