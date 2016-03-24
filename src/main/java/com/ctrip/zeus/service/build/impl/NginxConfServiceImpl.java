package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.NginxConfServerData;
import com.ctrip.zeus.model.entity.NginxConfUpstreamData;
import com.ctrip.zeus.nginx.entity.VsConfData;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.status.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private BuildInfoService buildInfoService;
    @Resource
    ConfGroupSlbActiveDao confGroupSlbActiveDao;


    private Logger logger = LoggerFactory.getLogger(NginxConfServiceImpl.class);

    @Override
    public String getNginxConf(Long slbId, Long version) throws Exception {
        NginxConfDo nginxConfDo = nginxConfDao.findBySlbIdAndVersion(slbId, (int) (long) version, NginxConfEntity.READSET_FULL);
        if (nginxConfDo == null) {
            throw new ValidationException("Not found nginx conf by slbId and version. slbId:" + slbId + " version:" + version);
        }
        return nginxConfDo.getContent();
    }

    @Override
    public List<NginxConfServerData> getNginxConfServer(Long slbId, Long version) throws Exception {
        List<NginxConfServerData> result = new ArrayList<>();
        List<NginxConfServerDo> d = nginxConfServerDao.findAllBySlbIdAndVersion(slbId, (int) (long) version, NginxConfServerEntity.READSET_FULL);
        if (d == null || d.size() == 0) {
            logger.warn("Not found nginx server conf by slbId and version. slbId:" + slbId + " version:" + version);
            return result;
        }
        for (NginxConfServerDo t : d) {
            result.add(new NginxConfServerData().setVsId(t.getSlbVirtualServerId()).setContent(t.getContent()));
        }
        return result;
    }

    @Override
    public List<NginxConfUpstreamData> getNginxConfUpstream(Long slbId, Long version) throws Exception {
        List<NginxConfUpstreamData> result = new ArrayList<>();
        List<NginxConfUpstreamDo> d = nginxConfUpstreamDao.findAllBySlbIdAndVersion(slbId, (int) (long) version, NginxConfUpstreamEntity.READSET_FULL);
        if (d == null || d.size() == 0) {
            logger.warn("Not found nginx upstream conf by slbId and version. slbId:" + slbId + " version:" + version);
            return result;
        }
        for (NginxConfUpstreamDo t : d) {
            result.add(new NginxConfUpstreamData().setVsId(t.getSlbVirtualServerId()).setContent(t.getContent()));
        }
        return result;
    }

    @Override
    public Map<Long, VsConfData> getVsConfBySlbId(Long slbId, Long version) throws Exception {
        List<NginxConfServerData> confServerDataList = getNginxConfServer(slbId, version);
        List<NginxConfUpstreamData> confUpstreamDataList = getNginxConfUpstream(slbId, version);
        if (confServerDataList.size() != confUpstreamDataList.size()) {
            throw new ValidationException("Count of server conf and count of upstream conf is different.");
        }
        Map<Long, VsConfData> dataMap = new HashMap<>();
        for (NginxConfUpstreamData upstream : confUpstreamDataList) {
            VsConfData vsConfData = new VsConfData();
            Long vsId = upstream.getVsId();
            vsConfData.setUpstreamConf(upstream.getContent());
            dataMap.put(vsId, vsConfData);
        }
        for (NginxConfServerData sd : confServerDataList) {
            if (dataMap.containsKey(sd.getVsId())) {
                dataMap.get(sd.getVsId()).setVhostConf(sd.getContent());
            } else {
                throw new ValidationException("Not found upstream conf for server conf.");
            }
        }
        return dataMap;
    }

    @Override
    public Map<Long, VsConfData> getVsConfByVsIds(Long slbId, List<Long> vsIds, Long version) throws Exception {
        List<NginxConfServerData> confServerDataList = getNginxConfServer(slbId, version);
        List<NginxConfUpstreamData> confUpstreamDataList = getNginxConfUpstream(slbId, version);
        if (confServerDataList.size() != confUpstreamDataList.size()) {
            throw new ValidationException("Count of server conf and count of upstream conf is different.");
        }
        Map<Long, VsConfData> dataMap = new HashMap<>();
        for (NginxConfUpstreamData upstream : confUpstreamDataList) {
            if (vsIds.contains(upstream.getVsId())) {
                VsConfData vsConfData = new VsConfData();
                Long vsId = upstream.getVsId();
                vsConfData.setUpstreamConf(upstream.getContent());
                dataMap.put(vsId, vsConfData);
            }
        }
        for (NginxConfServerData sd : confServerDataList) {
            if (vsIds.contains(sd.getVsId())) {
                if (dataMap.containsKey(sd.getVsId())) {
                    dataMap.get(sd.getVsId()).setVhostConf(sd.getContent());
                } else {
                    throw new ValidationException("Not found upstream conf for server conf.");
                }
            }
        }
        return dataMap;
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
