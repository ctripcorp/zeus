package com.ctrip.zeus.service.version.impl;

import com.ctrip.zeus.dao.entity.NginxServer;
import com.ctrip.zeus.dao.entity.NginxServerExample;
import com.ctrip.zeus.dao.entity.SlbConfSlbVersion;
import com.ctrip.zeus.dao.entity.SlbConfSlbVersionExample;
import com.ctrip.zeus.dao.mapper.NginxServerMapper;
import com.ctrip.zeus.dao.mapper.SlbConfSlbVersionMapper;
import com.ctrip.zeus.model.commit.ConfSlbVersion;
import com.ctrip.zeus.service.version.ConfVersionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by lu.wang on 2016/3/15.
 */
@Service("confVersionService")
public class ConfVersionServiceImpl implements ConfVersionService {
    @Resource
    private NginxServerMapper nginxServerMapper;

    @Resource
    private SlbConfSlbVersionMapper slbConfSlbVersionMapper;

    @Override
    public Long getSlbServerCurrentVersion(Long sid, String ip) throws Exception {
        NginxServer nginxServer = nginxServerMapper.selectOneByExample(new NginxServerExample().createCriteria()
                .andIpEqualTo(ip).andSlbIdEqualTo(sid).example());
        if (null == nginxServer) {
            updateSlbServerCurrentVersion(sid, ip, 0L);
            return 0L;
        }
        return (long) nginxServer.getVersion();
    }

    @Override
    public void updateSlbServerCurrentVersion(Long slbId, String ip, Long version) throws Exception {
        if (slbId == null || ip == null || version == null) {
            throw new IllegalArgumentException(String.format("params can not be null, slbId:%s, ip:%s, version:%s", slbId, ip, version));
        }
        NginxServer nginxServer = new NginxServer();
        nginxServer.setIp(ip);
        nginxServer.setSlbId(slbId);
        nginxServer.setVersion(version.intValue());
        nginxServerMapper.updateByExampleSelective(nginxServer, new NginxServerExample().createCriteria().andIpEqualTo(ip).example());
    }

    @Override
    public Long getSlbCurrentVersion(Long slbId) throws Exception {
        SlbConfSlbVersion confSlbVersion = slbConfSlbVersionMapper.selectOneByExample(new SlbConfSlbVersionExample().createCriteria().andSlbIdEqualTo(slbId).example());
        if (null == confSlbVersion) {
            addConfSlbVersion(new ConfSlbVersion().setSlbId(slbId).setPreviousVersion(0L).setCurrentVersion(0L));
            return 0L;
        }
        return confSlbVersion.getCurrentVersion();
    }

    @Override
    public Long getSlbPreviousVersion(Long slbId) throws Exception {
        SlbConfSlbVersion confSlbVersion = slbConfSlbVersionMapper.selectOneByExample(new SlbConfSlbVersionExample().createCriteria().andSlbIdEqualTo(slbId).example());
        if (null == confSlbVersion) {
            addConfSlbVersion(new ConfSlbVersion().setSlbId(slbId).setPreviousVersion(0L).setCurrentVersion(0L));
            return 0L;
        }
        return confSlbVersion.getPreviousVersion();
    }

    @Override
    public void updateSlbCurrentVersion(Long slbId, Long version) throws Exception {
        if (null == slbId || null == version)
            throw new IllegalArgumentException(String.format("params can not be null, slbId:%s, version:%s", slbId, version));
        Long currentVersion = getSlbCurrentVersion(slbId);
        Long updateToPreviousVersion = 0L;
        Long updateToCurrentVersion = version;
        if (null != currentVersion) {
            if (version > currentVersion) {
                updateToPreviousVersion = currentVersion;
            }
        } else {
            addConfSlbVersion(new ConfSlbVersion().setSlbId(slbId).setPreviousVersion(updateToPreviousVersion).setCurrentVersion(updateToCurrentVersion));
        }
        slbConfSlbVersionMapper.updateByExampleSelective(
                SlbConfSlbVersion.
                        builder().
                        previousVersion(updateToPreviousVersion).
                        currentVersion(updateToCurrentVersion).
                        build(),
                new SlbConfSlbVersionExample().
                        createCriteria().
                        andSlbIdEqualTo(slbId).
                        example());
    }

    @Override
    public void addConfSlbVersion(ConfSlbVersion confSlbVersion) throws Exception {
        if (null == confSlbVersion)
            throw new IllegalArgumentException("param:confSlbVersion can not be null");
        if (confSlbVersion == null) return;
        slbConfSlbVersionMapper.insert(SlbConfSlbVersion.builder().slbId(confSlbVersion.getSlbId()).currentVersion(confSlbVersion.getCurrentVersion()).previousVersion(confSlbVersion.getPreviousVersion()).build());
    }
}
