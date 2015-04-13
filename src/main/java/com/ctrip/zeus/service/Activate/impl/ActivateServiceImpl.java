package com.ctrip.zeus.service.Activate.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppSlb;
import com.ctrip.zeus.model.entity.Archive;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.Activate.ActivateService;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.io.IOException;
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
    @Resource
    ConfAppSlbActiveDao confAppSlbActiveDao;

    private Logger logger = LoggerFactory.getLogger(ActivateServiceImpl.class);

    @Override
    public void activeSlb(String name) throws Exception {

        Archive archive = archiveService.getLatestSlbArchive(name);
        if (archive==null)
        {
            logger.info("getLatestSlbArchive return Null! SlbName: "+name);
            AssertUtils.isNull(archive,"[Activate]getLatestSlbArchive return Null! SlbName: "+name);
            return;
        }

        ConfSlbActiveDo c = new ConfSlbActiveDo().setCreatedTime(new Date());
        c.setName(archive.getName()).setContent(archive.getContent()).setVersion(archive.getVersion());
        confSlbActiveDao.insert(c);

        logger.debug("Conf Slb Active Inserted: [name: "+c.getName()+",Content: "+c.getContent()+",Version: "+c.getVersion()+"]");

    }

    @Override
    public void activeApp(String name) throws Exception {
        Archive archive = archiveService.getLatestAppArchive(name);
        if (archive==null)
        {
            logger.info("getLatestAppArchive return Null! AppName: "+name);
            AssertUtils.isNull(archive,"[Activate]getLatestAppArchive return Null! AppName: "+name);
            return;
        }

        ConfAppActiveDo c = new ConfAppActiveDo().setCreatedTime(new Date());
        c.setName(archive.getName()).setContent(archive.getContent()).setVersion(archive.getVersion());
        confAppActiveDao.insert(c);

        logger.debug("Conf App Active Inserted: [name: "+c.getName()+",Content: "+c.getContent()+",Version: "+c.getVersion()+"]");


        App app =  DefaultSaxParser.parseEntity(App.class, c.getContent());

        AssertUtils.isNull(app,"App_ctive.content XML is illegal!");

        confAppSlbActiveDao.deleteByAppName(new ConfAppSlbActiveDo().setAppName(name));

        for (AppSlb appSlb:app.getAppSlbs())
        {
            confAppSlbActiveDao.insert(new ConfAppSlbActiveDo().setAppName(name)
                                            .setSlbVirtualServerName(appSlb.getVirtualServer().getName())
                                            .setSlbName(appSlb.getSlbName()).setLastModified(new Date()));
        }

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
