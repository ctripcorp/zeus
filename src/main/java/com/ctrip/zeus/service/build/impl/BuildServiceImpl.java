package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.dal.core.NginxServerDao;
import com.ctrip.zeus.dal.core.NginxServerDo;
import com.ctrip.zeus.dal.core.NginxServerEntity;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfService;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

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

    @Override
    public void build(String name) throws DalException, IOException, SAXException {
        int ticket = buildInfoService.getTicket(name);
        nginxConfService.build(name, ticket);
        buildInfoService.updateTicket(name, ticket);

//        List<NginxServerDo> list = nginxServerDao.findAllBySlbName(name, NginxServerEntity.READSET_FULL);
//        for (NginxServerDo d : list) {
//            System.out.println(d.getIp() + "###");
//            NginxClient nginxClient = new NginxClient("http://"+d.getIp() + ":8099");
//            try {
//                nginxClient.load();
//                System.out.println(d.getIp());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void build2(String name) throws DalException, IOException, SAXException {
        int ticket = buildInfoService.getTicket(name);
        nginxConfService.build(name, ticket);
        buildInfoService.updateTicket(name, ticket);
    }
}
