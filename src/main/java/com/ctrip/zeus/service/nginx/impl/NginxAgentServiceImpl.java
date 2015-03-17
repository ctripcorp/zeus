package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.dal.core.NginxServerDao;
import com.ctrip.zeus.dal.core.NginxServerDo;
import com.ctrip.zeus.dal.core.NginxServerEntity;
import com.ctrip.zeus.service.nginx.NginxAgentService;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/17/2015.
 */
@Service("nginxAgentService")
public class NginxAgentServiceImpl implements NginxAgentService {

    @Resource
    private NginxServerDao nginxServerDao;

    @Override
    public void reloadConf(String slbName) {
        try {
            List<NginxServerDo> list = nginxServerDao.findAllBySlbName(slbName, NginxServerEntity.READSET_FULL);
            for (NginxServerDo d : list) {
                System.out.println(d.getIp() + "###");
                NginxClient nginxClient = new NginxClient("http://"+d.getIp() + ":8099");
                try {
                    nginxClient.load();
                    System.out.println(d.getIp());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
