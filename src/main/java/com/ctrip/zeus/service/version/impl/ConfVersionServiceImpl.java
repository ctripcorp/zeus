package com.ctrip.zeus.service.version.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.version.ConfVersionService;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

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
    public Long getSlbServerCurrentVersion(Long sid, String ip) throws DalException {
        Long version = -1L;
        NginxServerDo nginxServer = nginxServerDao.findBySlbIdAndIp(ip, sid, NginxServerEntity.READSET_FULL);
        if (null != nginxServer)
            version = nginxServer.getVersion();
        return version;
    }

    @Override
    public void updateSlbServerCurrentVersion(Long slbId, String ip, Long version) throws DalException {
        nginxServerDao.updateVersionBySlbIdAndIp(new NginxServerDo().setIp(ip).setSlbId(slbId).setVersion(version), NginxServerEntity.UPDATESET_FULL);
    }

    @Override
    public Long getSlbCurrentVersion(Long slbId) throws DalException {
        Long currentVersion = -1L;
        ConfSlbVersionDo confSlbVersionDo = confSlbVersionDao.findBySlbId(slbId, ConfSlbVersionEntity.READSET_FULL);
        if (null != confSlbVersionDo)
            currentVersion = confSlbVersionDo.getCurrentVersion();
        return currentVersion;
    }

    @Override
    public Long getSlbPreviousVersion(Long slbId) throws DalException {
        Long perviousVersion = -1L;
        ConfSlbVersionDo confSlbVersionDo = confSlbVersionDao.findBySlbId(slbId, ConfSlbVersionEntity.READSET_FULL);
        if (null != confSlbVersionDo)
            perviousVersion = confSlbVersionDo.getPreviousVersion();
        return perviousVersion;
    }

    @Override
    public void updateSlbCurrentVersion(Long slbId, Long version) throws DalException {
        Long currentVersion = getSlbCurrentVersion(slbId);
        Long updateToPreviousVersion = 0L;
        Long updateToCurrentVersion = version;
        if (null != currentVersion) {
            if (version > currentVersion) {
                updateToPreviousVersion = currentVersion;
            }
        } else {
            addConfSlbVersion(new ConfSlbVersionDo().setSlbId(slbId).setCurrentVersion(version).setPreviousVersion(updateToPreviousVersion));
        }
        confSlbVersionDao.updateVersionBySlbId(new ConfSlbVersionDo().setSlbId(slbId).setPreviousVersion(updateToPreviousVersion).setCurrentVersion(updateToCurrentVersion), ConfSlbVersionEntity.UPDATESET_FULL);
    }

    @Override
    public void addConfSlbVersion(ConfSlbVersionDo confSlbVersionDo) throws DalException {
        confSlbVersionDao.insert(confSlbVersionDo);
    }
}
