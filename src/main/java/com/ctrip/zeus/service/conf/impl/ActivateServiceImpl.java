package com.ctrip.zeus.service.conf.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.conf.ActivateService;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component("activateService")
public class ActivateServiceImpl implements ActivateService {
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private ArchiveAppDao archiveAppDao;

    @Resource
    private ConfAppActiveDao confAppActiveDao;
    @Resource
    private ConfSlbActiveDao confSlbActiveDao;

    @Override
    public void activeSlb(String name) throws DalException {
        //ToDo:
        List<ArchiveSlbDo> l = archiveSlbDao.findAllByName(name, ArchiveSlbEntity.READSET_FULL);
        ArchiveSlbDo d = new ArchiveSlbDo().setVersion(0);
        for (ArchiveSlbDo archiveSlbDo : l) {
            if (d.getVersion() <= archiveSlbDo.getVersion()) {
                d=archiveSlbDo;
            }
        }

        ConfSlbActiveDo c = new ConfSlbActiveDo().setCreatedTime(new Date());
        c.setName(d.getName()).setContent(d.getContent()).setVersion(d.getVersion());
        confSlbActiveDao.insert(c);
    }

    @Override
    public void activeApp(String name) throws DalException {
        List<ArchiveAppDo> l = archiveAppDao.findAllByName(name, ArchiveAppEntity.READSET_FULL);
        ArchiveAppDo d = new ArchiveAppDo().setVersion(0);
        for (ArchiveAppDo archiveAppDo : l) {
            if (d.getVersion() <= archiveAppDo.getVersion()) {
                d=archiveAppDo;
            }
        }

        ConfAppActiveDo c = new ConfAppActiveDo().setCreatedTime(new Date());
        c.setName(d.getName()).setContent(d.getContent()).setVersion(d.getVersion());
        confAppActiveDao.insert(c);
    }
}
