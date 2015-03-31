package com.ctrip.zeus.service.Activate.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Archive;
import com.ctrip.zeus.service.Activate.ActivateService;
import com.ctrip.zeus.service.model.ArchiveService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component("activateConfService")
public class ActivateServiceImpl implements ActivateService {

    @Resource
    private ConfAppActiveDao confAppActiveDao;
    @Resource
    private ConfSlbActiveDao confSlbActiveDao;
    @Resource
    private ArchiveService archiveService;

    @Override
    public void activeSlb(String name) throws Exception {

        Archive archive = archiveService.getLatestSlbArchive(name);

        ConfSlbActiveDo c = new ConfSlbActiveDo().setCreatedTime(new Date());
        c.setName(archive.getName()).setContent(archive.getContent()).setVersion(archive.getVersion());
        confSlbActiveDao.insert(c);
    }

    @Override
    public void activeApp(String name) throws Exception {
        Archive archive = archiveService.getLatestAppArchive(name);

        ConfAppActiveDo c = new ConfAppActiveDo().setCreatedTime(new Date());
        c.setName(archive.getName()).setContent(archive.getContent()).setVersion(archive.getVersion());
        confAppActiveDao.insert(c);
    }

    @Override
    public void activate(List<String> slbNames, List<String> appNames) throws Exception {
        for (String slbName : slbNames) {
            activeSlb(slbName);
        }
        for (String appName : appNames) {
            activeApp(appName);
        }
    }
}
