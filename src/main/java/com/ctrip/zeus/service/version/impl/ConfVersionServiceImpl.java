package com.ctrip.zeus.service.version.impl;

import com.ctrip.zeus.commit.entity.ConfSlbVersion;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by lu.wang on 2016/3/15.
 */
@Service("confVersionService")
public class ConfVersionServiceImpl implements ConfVersionService {

    @Resource
    private NginxServerDao nginxServerDao;

    @Resource
    private ConfSlbVersionDao confSlbVersionDao;

    @Override
    public Long getSlbServerCurrentVersion(Long sid, String ip) throws Exception {
        NginxServerDo nginxServer = nginxServerDao.findBySlbIdAndIp(ip, sid, NginxServerEntity.READSET_FULL);
        if (null == nginxServer) {
            updateSlbServerCurrentVersion(sid, ip, 0L);
            return 0L;
        }
        return nginxServer.getVersion();
    }

    @Override
    public void updateSlbServerCurrentVersion(Long slbId, String ip, Long version) throws Exception {
        if (slbId == null || ip == null || version == null)
            throw new IllegalArgumentException(String.format("params can not be null, slbId:%s, ip:%s, version:%s", slbId, ip, version));
        NginxServerDo nginxServerDo = new NginxServerDo().setIp(ip).setSlbId(slbId).setVersion(version);
        nginxServerDao.updateVersionBySlbIdAndIp(nginxServerDo, NginxServerEntity.UPDATESET_FULL);
    }

    @Override
    public Long getSlbCurrentVersion(Long slbId) throws Exception {
        ConfSlbVersionDo confSlbVersion = confSlbVersionDao.findBySlbId(slbId, ConfSlbVersionEntity.READSET_FULL);
        if (null == confSlbVersion) {
            addConfSlbVersion(new ConfSlbVersion().setSlbId(slbId).setPreviousVersion(0L).setCurrentVersion(0L));
            return 0L;
        }
        return confSlbVersion.getCurrentVersion();
    }

    @Override
    public Long getSlbPreviousVersion(Long slbId) throws Exception {
        ConfSlbVersionDo confSlbVersion = confSlbVersionDao.findBySlbId(slbId, ConfSlbVersionEntity.READSET_FULL);
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
        confSlbVersionDao.updateVersionBySlbId(new ConfSlbVersionDo().setSlbId(slbId).setPreviousVersion(updateToPreviousVersion).setCurrentVersion(updateToCurrentVersion), ConfSlbVersionEntity.UPDATESET_FULL);
    }

    @Override
    public void addConfSlbVersion(ConfSlbVersion confSlbVersion) throws Exception {
        if (null == confSlbVersion)
            throw new IllegalArgumentException("param:confSlbVersion can not be null");
        ConfSlbVersionDo confSlbVersionDo = C.toConfSlbVersionDo(confSlbVersion);
        confSlbVersionDao.insert(confSlbVersionDo);
    }
}
