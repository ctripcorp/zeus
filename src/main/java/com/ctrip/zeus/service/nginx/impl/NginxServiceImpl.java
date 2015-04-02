package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.dal.core.NginxServerDao;
import com.ctrip.zeus.dal.core.NginxServerDo;
import com.ctrip.zeus.dal.core.NginxServerEntity;
import com.ctrip.zeus.model.entity.NginxConfServerData;
import com.ctrip.zeus.model.entity.NginxConfUpstreamData;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.nginx.NginxOperator;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Service("nginxService")
public class NginxServiceImpl implements NginxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NginxServiceImpl.class);
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    @Resource
    private SlbRepository slbRepository;

    @Resource
    private NginxConfService nginxConfService;

    @Resource
    private NginxServerDao nginxServerDao;

    @Override
    public NginxResponse load() throws Exception {
        String ip = S.getIp();
        Slb slb = slbRepository.getBySlbServer(ip);
        String slbName = slb.getName();
        int version = nginxConfService.getCurrentVersion(slbName);

        NginxOperator nginxOperator = new NginxOperator(slb.getNginxConf(), slb.getNginxBin());
        writeConfToDisk(slbName, version, nginxOperator);
        // reload configuration
        NginxResponse response = reloadConf(nginxOperator);

        // update the used version in the db
        NginxServerDo nginxServerDo =nginxServerDao.findByIp(ip, NginxServerEntity.READSET_FULL);
        nginxServerDao.updateByPK(nginxServerDo.setVersion(version), NginxServerEntity.UPDATESET_FULL);
        return response;
    }

    private NginxResponse reloadConf(NginxOperator nginxOperator) throws IOException {
        LOGGER.info("Start reloading nginx configuration");
        return nginxOperator.reloadConf();
    }

    private void writeConfToDisk(String slbName, int version, NginxOperator nginxOperator) throws Exception {
        LOGGER.info("Start writing nginx configuration.");
        // write nginx conf
        writeNginxConf(slbName, version, nginxOperator);
        // write server conf
        writeServerConf(slbName, version, nginxOperator);
        // write upstream conf
        writeUpstreamConf(slbName, version, nginxOperator);
    }

    @Override
    public NginxServerStatus getStatus() throws Exception {
        String ip = S.getIp();
        Slb slb = slbRepository.getBySlbServer(ip);
        NginxOperator nginxOperator = new NginxOperator(slb.getNginxConf(), slb.getNginxBin());
        return nginxOperator.getRuntimeStatus();
    }

    @Override
    public List<NginxResponse> loadAll(String slbName) throws Exception {
        List<NginxResponse> result = new ArrayList<>();
        String ip = S.getIp();
        Slb slb = slbRepository.get(slbName);

        List<SlbServer> slbServers = slb.getSlbServers();
        for (SlbServer slbServer : slbServers) {
            if (ip.equals(slbServer.getIp())) {
                result.add(load());
                continue;
            }
            NginxClient nginxClient = new NginxClient(buildRemoteUrl(slbServer.getIp()));
            NginxResponse response = nginxClient.load();
            result.add(response);
        }
        return result;

    }

    @Override
    public List<NginxServerStatus> getStatusAll(String slbName) throws Exception {
        List<NginxServerStatus> result = new ArrayList<>();
        String ip = S.getIp();

        Slb slb = slbRepository.get(slbName);
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (ip.equals(slbServer.getIp())) {
                result.add(getStatus());
                continue;
            }
            NginxClient nginxClient = new NginxClient(buildRemoteUrl(slbServer.getIp()));
            NginxServerStatus response = nginxClient.getNginxServerStatus();
            result.add(response);
        }
        return result;
    }

    private String buildRemoteUrl(String ip) {
        return "http://" + ip + ":" + adminServerPort.get();
    }

    private void writeNginxConf(String slbName, int version, NginxOperator nginxOperator) throws Exception {
        String nginxConf = nginxConfService.getNginxConf(slbName, version);
        if (nginxConf == null || nginxConf.isEmpty()){
            throw new IllegalStateException("the nginx conf must not be empty!");
        }
        nginxOperator.writeNginxConf(nginxConf);
    }

    private void writeServerConf(String slbName, int version, NginxOperator nginxOperator) throws Exception {
        List<NginxConfServerData> nginxConfServerDataList = nginxConfService.getNginxConfServer(slbName, version);
         for (NginxConfServerData d : nginxConfServerDataList) {
            nginxOperator.writeServerConf(d.getName(), d.getContent());
        }
    }

    private void writeUpstreamConf(String slbName, int version, NginxOperator nginxOperator) throws Exception {
        List<NginxConfUpstreamData> nginxConfUpstreamList = nginxConfService.getNginxConfUpstream(slbName, version);
        for (NginxConfUpstreamData d : nginxConfUpstreamList) {
            nginxOperator.writeUpstreamsConf(d.getName(), d.getContent());
        }
    }
}
