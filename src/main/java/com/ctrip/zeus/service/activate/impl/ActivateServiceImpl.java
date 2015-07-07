package com.ctrip.zeus.service.activate.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Archive;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
    private ConfGroupActiveDao confGroupActiveDao;
    @Resource
    private ConfSlbActiveDao confSlbActiveDao;
    @Resource
    private ArchiveService archiveService;
    @Resource
    ConfGroupSlbActiveDao confGroupSlbActiveDao;

    private Logger logger = LoggerFactory.getLogger(ActivateServiceImpl.class);

    @Override
    public void activeSlb(long slbId) throws Exception {

        Archive archive = archiveService.getLatestSlbArchive(slbId);
        if (archive==null)
        {
            logger.info("getLatestSlbArchive return Null! SlbID: "+slbId);
            AssertUtils.assertNotNull(archive, "[activate]getLatestSlbArchive return Null! SlbID: " + slbId);
            return;
        }

        ConfSlbActiveDo c = new ConfSlbActiveDo().setCreatedTime(new Date());
        c.setSlbId(archive.getId()).setContent(archive.getContent()).setVersion(archive.getVersion());
        confSlbActiveDao.insert(c);

        logger.debug("Conf Slb Active Inserted: [SlbID: "+c.getSlbId()+",Content: "+c.getContent()+",Version: "+c.getVersion()+"]");

    }

    @Override
    public void activeGroup(long groupId) throws Exception {
        Archive archive = archiveService.getLatestGroupArchive(groupId);
        if (archive==null)
        {
            logger.info("getLatestAppArchive return Null! GroupID: "+groupId);
            AssertUtils.assertNotNull(archive, "[activate]getLatestAppArchive return Null! GroupID: " + groupId);
            return;
        }

        ConfGroupActiveDo c = new ConfGroupActiveDo().setCreatedTime(new Date());
        c.setGroupId(archive.getId()).setContent(archive.getContent()).setVersion(archive.getVersion());
        confGroupActiveDao.insert(c);

        logger.debug("Conf Group Active Inserted: [GroupId: "+c.getId()+",Content: "+c.getContent()+",Version: "+c.getVersion()+"]");


        Group group =  DefaultSaxParser.parseEntity(Group.class, c.getContent());

        AssertUtils.assertNotNull(group, "App_ctive.content XML is illegal!");

        confGroupSlbActiveDao.deleteByGroupId(new ConfGroupSlbActiveDo().setGroupId(groupId));

        for (GroupSlb groupSlb:group.getGroupSlbs())
        {
            confGroupSlbActiveDao.insert(new ConfGroupSlbActiveDo().setGroupId(groupId)
                                            .setPriority(groupSlb.getPriority())
                                            .setSlbId(groupSlb.getSlbId()).setDataChangeLastTime(new Date())
                                            .setSlbVirtualServerId(groupSlb.getVirtualServer().getId()));
        }

    }

    @Override
    public void activate(List<Long> slbIds, List<Long> groupIds) throws Exception {
        for (Long slbId : slbIds) {
            activeSlb(slbId);
        }
        for (Long groupId : groupIds) {
            activeGroup(groupId);
        }
    }

    @Override
    public void deactiveGroup(long groupId) throws Exception
    {
        confGroupActiveDao.deleteByGroupId(new ConfGroupActiveDo().setGroupId(groupId));
        confGroupSlbActiveDao.deleteByGroupId(new ConfGroupSlbActiveDo().setGroupId(groupId));
    }

    @Override
    public boolean isGroupActivated(Long groupId) throws Exception {
        List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByGroupIds(new Long[]{groupId},ConfGroupActiveEntity.READSET_FULL);
        if (null == groupActiveDos || groupActiveDos.size() == 0)
        {
            return false;
        }else {
            return true;
        }
    }
}
