package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.nginx.NginxOperator;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.util.S;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Service("nginxService")
public class NginxServiceImpl implements NginxService {
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

    @Resource
    private NginxOperator nginxOperator;

    @Override
    public String load() throws IOException, DalException {
        String ip = S.getIp();
        NginxServerDo nginxServerDo = nginxServerDao.findByIp(ip, NginxServerEntity.READSET_FULL);
        SlbDo slbDo = slbDao.findByName(nginxServerDo.getSlbName(), SlbEntity.READSET_FULL);
        String slbName = slbDo.getName();
        BuildInfoDo buildInfoDo = buildInfoDao.findByName(slbName, BuildInfoEntity.READSET_FULL);
        int version = buildInfoDo.getCurrentPendingTicket();

        String nginxConf = nginxConfDao.findBySlbNameAndVersion(slbName,version, NginxConfEntity.READSET_FULL).getContent();
        nginxOperator.writeNginxConf(slbDo.getNginxConf() + "/nginx.conf", nginxConf);

        List<NginxConfServerDo> nginxConfServerDoList = nginxConfServerDao.findAllBySlbNameAndVersion(slbName, version, NginxConfServerEntity.READSET_FULL);
        for (NginxConfServerDo d : nginxConfServerDoList) {
            nginxOperator.writeServerConf(slbDo.getNginxConf() + "/vhosts/" + d.getName() + ".conf", d.getContent());
        }

        List<NginxConfUpstreamDo> nginxConfUpstreamDoList = nginxConfUpstreamDao.findAllBySlbNameAndVersion(slbName, version, NginxConfUpstreamEntity.READSET_FULL);
        for (NginxConfUpstreamDo d : nginxConfUpstreamDoList) {
            nginxOperator.writeUpstreamsConf(slbDo.getNginxConf() + "/upstreams/" + d.getName() + ".conf", d.getContent());
        }
        String s = nginxOperator.reloadConf("sudo " + slbDo.getNginxBin() + "/nginx -s reload");

        nginxServerDao.updateByPK(nginxServerDo.setVersion(version), NginxServerEntity.UPDATESET_FULL);
        return s;
    }
}
