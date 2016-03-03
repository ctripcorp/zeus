package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.status.StatusService;
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

}
