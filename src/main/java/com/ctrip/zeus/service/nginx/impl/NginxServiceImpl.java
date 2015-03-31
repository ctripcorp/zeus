package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.nginx.NginxResponse;
import com.ctrip.zeus.nginx.NginxServerStatus;
import com.ctrip.zeus.nginx.NginxOperator;
import com.ctrip.zeus.service.SlbException;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private NginxServerDao nginxServerDao;
    @Resource
    private SlbDao slbDao;
    @Resource
    private BuildInfoDao buildInfoDao;
    @Resource
    private NginxConfDao nginxConfDao;
    @Resource
    private NginxConfServerDao nginxConfServerDao;
    @Resource
    private NginxConfUpstreamDao nginxConfUpstreamDao;


    @Override
    public NginxResponse load() throws SlbException {
        String ip = S.getIp();
        try {
            NginxServerDo nginxServerDo = nginxServerDao.findByIp(ip, NginxServerEntity.READSET_FULL);
            SlbDo slbDo = slbDao.findByName(nginxServerDo.getSlbName(), SlbEntity.READSET_FULL);
            String slbName = slbDo.getName();
            BuildInfoDo buildInfoDo = buildInfoDao.findByName(slbName, BuildInfoEntity.READSET_FULL);
            int version = buildInfoDo.getCurrentTicket();

            NginxOperator nginxOperator = new NginxOperator(slbDo.getNginxConf(), slbDo.getNginxBin());
            writeConfToDisk(slbName, version, nginxOperator);
            // reload configuration
            NginxResponse response = reloadConf(nginxOperator);
            nginxServerDao.updateByPK(nginxServerDo.setVersion(version), NginxServerEntity.UPDATESET_FULL);
            return response;
        } catch (Exception e){
            LOGGER.error("Fail to update and reload configuration.",e);
            throw new SlbException(e);
        }
    }

    private NginxResponse reloadConf(NginxOperator nginxOperator) throws IOException {
        LOGGER.info("Start reloading nginx configuration");
        return nginxOperator.reloadConf();
    }

    private void writeConfToDisk(String slbName, int version, NginxOperator nginxOperator) throws DalException, IOException {
        LOGGER.info("Start writing nginx configuration.");
        // write nginx conf
        writeNginxConf(slbName, version, nginxOperator);
        // write server conf
        writeServerConf(slbName, version, nginxOperator);
        // write upstream conf
        writeUpstreamConf(slbName, version, nginxOperator);
    }

    @Override
    public NginxServerStatus getStatus() throws SlbException {
        try {
            String ip = S.getIp();
            NginxServerDo nginxServerDo = nginxServerDao.findByIp(ip, NginxServerEntity.READSET_FULL);
            SlbDo slbDo = slbDao.findByName(nginxServerDo.getSlbName(), SlbEntity.READSET_FULL);
            NginxOperator nginxOperator = new NginxOperator(slbDo.getNginxConf(), slbDo.getNginxBin());
            return nginxOperator.getRuntimeStatus();
        }catch (Exception e){
            LOGGER.error("Fail to get nginx status",e);
            throw new SlbException(e);
        }
    }

    @Override
    public List<NginxResponse> loadAll() throws SlbException {
        try {
            List<NginxResponse> result = new ArrayList<>();
            String ip = S.getIp();
            result.add(load());
            NginxServerDo nginxServerDo = nginxServerDao.findByIp(ip, NginxServerEntity.READSET_FULL);
            List<NginxServerDo> serverList = nginxServerDao.findAllBySlbName(nginxServerDo.getSlbName(), NginxServerEntity.READSET_FULL);
            for (NginxServerDo serverDo : serverList) {
                if (ip.equals(serverDo.getIp())) {
                    continue;
                }
                String responseStr = request(buildRemoteUrl(serverDo.getIp(), "/nginx/load"));
                NginxResponse response = NginxResponse.fromJson(responseStr);
                result.add(response);
            }
            return result;
        }catch (Exception e){
            LOGGER.error("error happens when load nginx conf",e);
            throw new SlbException(e);
        }
    }

    @Override
    public List<NginxServerStatus> getStatusAll() throws SlbException {
        try {
            List<NginxServerStatus> result = new ArrayList<>();
            String ip = S.getIp();
            result.add(getStatus());
            NginxServerDo nginxServerDo = nginxServerDao.findByIp(ip, NginxServerEntity.READSET_FULL);
            List<NginxServerDo> serverList = nginxServerDao.findAllBySlbName(nginxServerDo.getSlbName(), NginxServerEntity.READSET_FULL);
            for (NginxServerDo serverDo : serverList) {
                if (ip.equals(serverDo.getIp())) {
                    continue;
                }
                String responseStr = request(buildRemoteUrl(serverDo.getIp(), "/nginx/load"));
                NginxServerStatus response = NginxServerStatus.fromJson(responseStr);
                result.add(response);
            }
            return result;
        }catch (Exception e){
            LOGGER.error("error happens when load nginx conf",e);
            throw new SlbException(e);
        }
    }

    private String buildRemoteUrl(String ip, String uri) {
        return "http://" + ip + ":" + adminServerPort.get() + uri;
    }

    private String request(String urlStr) throws IOException {

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(1000 * 20);

        conn.setRequestMethod("GET");

        InputStream inputStream = conn.getInputStream();
        String res = consumeStream(inputStream);
        inputStream.close();
        return res;
    }

    private String consumeStream(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    private void writeNginxConf(String slbName, int version, NginxOperator nginxOperator) throws DalException, IOException {
        String nginxConf = nginxConfDao.findBySlbNameAndVersion(slbName,version, NginxConfEntity.READSET_FULL).getContent();
        nginxOperator.writeNginxConf(nginxConf);
    }

    private void writeServerConf(String slbName, int version, NginxOperator nginxOperator) throws DalException, IOException {
        List<NginxConfServerDo> nginxConfServerDoList = nginxConfServerDao.findAllBySlbNameAndVersion(slbName, version, NginxConfServerEntity.READSET_FULL);
        for (NginxConfServerDo d : nginxConfServerDoList) {
            nginxOperator.writeServerConf(d.getName(), d.getContent());
        }
    }

    private void writeUpstreamConf(String slbName, int version, NginxOperator nginxOperator) throws DalException, IOException {
        List<NginxConfUpstreamDo> nginxConfUpstreamDoList = nginxConfUpstreamDao.findAllBySlbNameAndVersion(slbName, version, NginxConfUpstreamEntity.READSET_FULL);
        for (NginxConfUpstreamDo d : nginxConfUpstreamDoList) {
            nginxOperator.writeUpstreamsConf(d.getName(), d.getContent());
        }
    }
}
