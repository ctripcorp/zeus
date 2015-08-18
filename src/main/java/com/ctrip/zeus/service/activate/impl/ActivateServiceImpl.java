package com.ctrip.zeus.service.activate.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.activate.ServerGroupService;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

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
    @Resource
    ServerGroupService serverGroupService;

    private Logger logger = LoggerFactory.getLogger(ActivateServiceImpl.class);

    @Override
    public void activeSlb(long slbId , int version) throws Exception {

        Archive archive = archiveService.getSlbArchive(slbId, version);
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
    public void activeGroup(long groupId , int version , Long slbId) throws Exception {
        Archive archive = archiveService.getGroupArchive(groupId, version);
        if (archive==null)
        {
            logger.info("getLatestAppArchive return Null! GroupID: "+groupId);
            AssertUtils.assertNotNull(archive, "[activate]getLatestAppArchive return Null! GroupID: " + groupId);
            return;
        }

        ConfGroupActiveDo c = new ConfGroupActiveDo().setCreatedTime(new Date());
        c.setGroupId(archive.getId()).setContent(archive.getContent()).setVersion(archive.getVersion()).setSlbId(slbId);
        confGroupActiveDao.insert(c);
        confGroupActiveDao.deleteByGroupIdAndSlbId(new ConfGroupActiveDo().setGroupId(groupId).setSlbId(0));

        logger.debug("Conf Group Active Inserted: [GroupId: "+c.getId()+",Content: "+c.getContent()+",Version: "+c.getVersion()+"]");


        Group group =  DefaultSaxParser.parseEntity(Group.class, c.getContent());

        AssertUtils.assertNotNull(group, "App_ctive.content XML is illegal!");

        confGroupSlbActiveDao.deleteByGroupId(new ConfGroupSlbActiveDo().setGroupId(groupId));

        for (GroupVirtualServer groupSlb:group.getGroupVirtualServers())
        {
            confGroupSlbActiveDao.insert(new ConfGroupSlbActiveDo().setGroupId(groupId)
                                            .setPriority(groupSlb.getPriority())
                                            .setSlbId(groupSlb.getVirtualServer().getSlbId()).setDataChangeLastTime(new Date())
                                            .setSlbVirtualServerId(groupSlb.getVirtualServer().getId()));
        }
        List<GroupServer> groupServers = group.getGroupServers();
        serverGroupService.deleteByGroupId(groupId);
        for (GroupServer gs : groupServers){
            serverGroupService.insertServerGroup(gs.getIp(),groupId);
        }
    }



    @Override
    public void deactiveGroup(Long groupId , Long slbId) throws Exception
    {
        confGroupActiveDao.deleteByGroupIdAndSlbId(new ConfGroupActiveDo().setGroupId(groupId).setSlbId(slbId));
        confGroupActiveDao.deleteByGroupIdAndSlbId(new ConfGroupActiveDo().setGroupId(groupId).setSlbId(0));
        confGroupSlbActiveDao.deleteByGroupIdSlbId(new ConfGroupSlbActiveDo().setGroupId(groupId).setSlbId(slbId ));
        serverGroupService.deleteByGroupId(groupId);
    }

    @Override
    public boolean isGroupActivated(Long groupId , Long slbId) throws Exception {
        List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByGroupIdsAndSlbId(new Long[]{groupId},slbId,ConfGroupActiveEntity.READSET_FULL);
        if (null == groupActiveDos || groupActiveDos.size() == 0)
        {
            groupActiveDos = confGroupActiveDao.findAllByGroupIdsAndSlbId(new Long[]{groupId},0,ConfGroupActiveEntity.READSET_FULL);
            if (groupActiveDos != null && groupActiveDos.size()==1){
                return true;
            }
            return false;
        }else {
            return true;
        }
    }
    @Override
    public HashMap<Long,Boolean> isGroupsActivated(Long[] groupIds,Long slbId) throws Exception {
        List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByGroupIdsAndSlbId(groupIds,slbId,ConfGroupActiveEntity.READSET_FULL);
        List<ConfGroupActiveDo> groupActiveDosOld = confGroupActiveDao.findAllByGroupIdsAndSlbId(groupIds,0,ConfGroupActiveEntity.READSET_FULL);
        if (groupActiveDos == null){
            groupActiveDos = groupActiveDosOld;
        }else if (groupActiveDosOld != null){
            groupActiveDos.addAll(groupActiveDosOld);
        }
        HashMap<Long,Boolean> result = new HashMap<>();
        for (Long groupId : groupIds){
            result.put(groupId,false);
        }
        if (null == groupActiveDos || groupActiveDos.size() == 0)
        {
            return result;
        }else {
            for (ConfGroupActiveDo groupActiveDo : groupActiveDos){
                result.put(groupActiveDo.getGroupId(),true);
            }
            return result;
        }
    }

    @Override
    public Group getActivatingGroup(Long groupId, int version) {
        try {
            Archive archive = archiveService.getGroupArchive(groupId,version);
            if (archive == null ){
                return null;
            }
            String content = archive.getContent();
            Group group = DefaultSaxParser.parseEntity(Group.class, content);
            if (group != null){
                return group;
            }
        } catch (Exception e) {
            logger.warn("Archive Parser Fail ! GroupId:"+groupId+" Version:"+version);
        }
        return null;
    }

    @Override
    public Slb getActivatingSlb(Long slbId , int version) {
        try {
            Archive archive = archiveService.getSlbArchive(slbId, version);
            if (archive == null ){
                logger.warn("Archive Not Found ! SlbId:"+slbId+" Version:"+version);
                return null;
            }
            String content = archive.getContent();
            Slb slb = DefaultSaxParser.parseEntity(Slb.class, content);
            if (slb == null){
                logger.warn("Archive Parser Fail ! SlbId:"+slbId+" Version:"+version);
            }
            return slb;
        } catch (Exception e) {
            logger.warn("Archive Parser Fail ! SlbId:"+slbId+" Version:"+version);
        }
        return null;
    }

    @Override
    public Group getActivatedGroup(Long groupId , Long slbId) throws Exception {
        List<ConfGroupActiveDo> list = confGroupActiveDao.findAllByGroupIdsAndSlbId(new Long[]{groupId},slbId,ConfGroupActiveEntity.READSET_FULL);
        if (list != null && list.size()==1){
            String content = list.get(0).getContent();
            return DefaultSaxParser.parseEntity(Group.class,content);
        }else {
            list = confGroupActiveDao.findAllByGroupIdsAndSlbId(new Long[]{groupId},0,ConfGroupActiveEntity.READSET_FULL);
            if (list != null && list.size()==1){
                String content = list.get(0).getContent();
                return DefaultSaxParser.parseEntity(Group.class,content);
            }
        }
        return null;
    }
    @Override
    public List<Group> getActivatedGroups(Long[] groupId , Long slbId) throws Exception {
        List<Group> result = new ArrayList<>();
        Group tmp = null;
        List<ConfGroupActiveDo> list = confGroupActiveDao.findAllByGroupIdsAndSlbId(groupId,slbId,ConfGroupActiveEntity.READSET_FULL);
        if (list != null && list.size()>0){
            for (ConfGroupActiveDo groupActiveDo : list)
            {
                tmp = DefaultSaxParser.parseEntity(Group.class,groupActiveDo.getContent());
                if (tmp!=null){
                    result.add(tmp);
                }
            }
        }
        return result;
    }

    @Override
    public Slb getActivatedSlb(Long slbId) throws Exception {
        ConfSlbActiveDo slbActiveDo = confSlbActiveDao.findBySlbId(slbId,ConfSlbActiveEntity.READSET_FULL);
        if (slbActiveDo!=null){
            String content = slbActiveDo.getContent();
            return DefaultSaxParser.parseEntity(Slb.class,content);
        }
        return null;
    }
}
