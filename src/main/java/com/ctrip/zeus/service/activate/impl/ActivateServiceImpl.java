package com.ctrip.zeus.service.activate.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.AutoFiller;
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
    ConfSlbVirtualServerActiveDao confSlbVirtualServerActiveDao;
    @Resource
    private AutoFiller autoFiller;

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
        logger.info("Conf Slb Active Inserted: [SlbID: " + c.getSlbId() + ",Content: " + c.getContent() + ",Version: " + c.getVersion() + "]");
    }

    @Override
    public void activeGroup(long groupId , int version , Long vsId , Long slbId) throws Exception {
        Archive archive = archiveService.getGroupArchive(groupId, version);
        if (archive==null)
        {
            logger.info("getLatestAppArchive return Null! GroupID: "+groupId);
            AssertUtils.assertNotNull(archive, "[activate]getLatestAppArchive return Null! GroupID: " + groupId);
            return;
        }
        Group group =  DefaultSaxParser.parseEntity(Group.class, archive.getContent());
        AssertUtils.assertNotNull(group, "group.content XML is illegal!");

        //while Group updated VsId, both vsIds belong to same slb, we need to delete the old version group active conf.
        ConfGroupActiveDo confGroupActiveDo = confGroupActiveDao.findByGroupIdAndVirtualServerId(groupId,vsId,ConfGroupActiveEntity.READSET_FULL);
        if (confGroupActiveDo == null){
            List<ConfGroupActiveDo> groups = confGroupActiveDao.findAllByGroupIds(new Long[]{groupId},ConfGroupActiveEntity.READSET_FULL);
            if (groups!=null && groups.size() > 0){
                List<Long> gvsIds = new ArrayList<>();
                for (ConfGroupActiveDo groupActiveDo : groups){
                    gvsIds.add(groupActiveDo.getSlbVirtualServerId());
                }
                List<ConfSlbVirtualServerActiveDo> vses = confSlbVirtualServerActiveDao.findBySlbIdAndSlbVirtualServerIds(gvsIds.toArray(new Long[]{}),slbId,ConfSlbVirtualServerActiveEntity.READSET_FULL);
                for (ConfSlbVirtualServerActiveDo confSlbVirtualServerActiveDo : vses){
                    confGroupActiveDao.deleteByGroupIdAndSlbVirtualServerId(new ConfGroupActiveDo().setGroupId(groupId).setSlbVirtualServerId(confSlbVirtualServerActiveDo.getSlbVirtualServerId()));
                }
            }
        }

        ConfGroupActiveDo c = new ConfGroupActiveDo().setCreatedTime(new Date());
        c.setGroupId(archive.getId()).setContent(archive.getContent()).setVersion(archive.getVersion()).setSlbVirtualServerId(vsId).setSlbId(slbId);
        confGroupActiveDao.insert(c);
        logger.info("Conf Group Active Inserted: [GroupId: " + c.getId() + ",Content: " + c.getContent() + ",Version: " + c.getVersion() + "]");
    }

    @Override
    public void activeVirtualServer(long vsId, int version, Long slbId) throws Exception {
        Archive archive = archiveService.getVsArchive(vsId, version);
        AssertUtils.assertNotNull(archive, "[activate]get Virtual Server Archive return Null! VsId: " + vsId);
        ConfSlbVirtualServerActiveDo confSlbVirtualServerActiveDo = new ConfSlbVirtualServerActiveDo();
        confSlbVirtualServerActiveDo.setContent(archive.getContent())
                .setSlbId(slbId).setVersion(version)
                .setSlbVirtualServerId(vsId)
                .setCreatedTime(new Date());
        confSlbVirtualServerActiveDao.insert(confSlbVirtualServerActiveDo);
    }


    @Override
    public void deactiveGroup(Long groupId , Long slbId) throws Exception
    {
        List<ConfSlbVirtualServerActiveDo> list = confSlbVirtualServerActiveDao.findBySlbId(slbId,ConfSlbVirtualServerActiveEntity.READSET_FULL);
        Long[]vsIds = new Long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            vsIds[i] = list.get(i).getSlbVirtualServerId();
        }
        List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByGroupIdsAndSlbVirtualServerIds(new Long[]{groupId},vsIds,ConfGroupActiveEntity.READSET_FULL);
        for (ConfGroupActiveDo confGroupActiveDo : groupActiveDos){
            confGroupActiveDao.deleteByGroupIdAndSlbVirtualServerId(new ConfGroupActiveDo().setGroupId(groupId).setSlbVirtualServerId(confGroupActiveDo.getSlbVirtualServerId()));
        }
    }

    @Override
    public void deactiveVirtualServer(Long vsId, Long slbId) throws Exception {
        confSlbVirtualServerActiveDao.deleteBySlbIdAndSlbVirtualServerId(new ConfSlbVirtualServerActiveDo()
                .setSlbId(slbId).setSlbVirtualServerId(vsId));
    }

    @Override
    public boolean isGroupActivated(Long groupId , Long vsId) throws Exception {
        if (vsId == null){
            List<ConfGroupActiveDo> groupActiveDos =  confGroupActiveDao.findAllByGroupIds(new Long[]{groupId},ConfGroupActiveEntity.READSET_FULL);
            if (groupActiveDos!=null && groupActiveDos.size()>0){
                return true;
            }
        }else{
            ConfGroupActiveDo groupActiveDo = confGroupActiveDao.findByGroupIdAndVirtualServerId(groupId,vsId,ConfGroupActiveEntity.READSET_FULL);
            if (groupActiveDo != null){
                return true;
            }
        }
        return false;
    }
    @Override
    public Map<Long,Boolean> isGroupsActivated(Long[] groupIds,Long vsId) throws Exception {
        Map<Long,Boolean> result = new HashMap<>();
        for (Long gid : groupIds){
            result.put(gid,false);
        }
        if (vsId == null){
            List<ConfGroupActiveDo> groupActiveDos =  confGroupActiveDao.findAllByGroupIds(groupIds,ConfGroupActiveEntity.READSET_FULL);
            for (ConfGroupActiveDo confGroupActiveDo : groupActiveDos){
                result.put(confGroupActiveDo.getGroupId(),true);
            }
        }else{
            List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByGroupIdsAndSlbVirtualServerId(groupIds,vsId,ConfGroupActiveEntity.READSET_FULL);
            for (ConfGroupActiveDo confGroupActiveDo : groupActiveDos){
                result.put(confGroupActiveDo.getGroupId(),true);
            }
        }
        return result;
    }

    @Override
    public boolean isVsActivated(Long vsId, Long slbId) throws Exception {
        List<ConfSlbVirtualServerActiveDo> c = confSlbVirtualServerActiveDao.findBySlbIdAndSlbVirtualServerIds(new Long[]{vsId},slbId,ConfSlbVirtualServerActiveEntity.READSET_FULL);
        return !(c == null || c.size() == 0 );
    }

    @Override
    public boolean hasActivatedGroupWithVsId(Long vsId) throws Exception {
        List<ConfGroupActiveDo> list = confGroupActiveDao.findAllByslbVirtualServerIds(new Long[]{vsId},ConfGroupActiveEntity.READSET_FULL);
        return !(list == null || list.size() == 0 );
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
                autoFiller.autofill(group);
                return group;
            }
        } catch (Exception e) {
            logger.warn("Archive Parser Fail ! GroupId:"+groupId+" Version:"+version);
        }
        return null;
    }

    @Override
    public VirtualServer getActivatingVirtualServer(Long vsId, int version) {
        try {
            Archive archive = archiveService.getVsArchive(vsId, version);
            if (archive == null ){
                return null;
            }
            String content = archive.getContent();
            return DefaultSaxParser.parseEntity(VirtualServer.class, content);
        } catch (Exception e) {
            logger.warn("[getActivatingVirtualServer] Archive Parser Fail ! VsId:"+vsId+" Version:"+version,e);
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
            autoFiller.autofill(slb);
            return slb;
        } catch (Exception e) {
            logger.warn("Archive Parser Fail ! SlbId:"+slbId+" Version:"+version);
        }
        return null;
    }

    @Override
    public Group getActivatedGroup(Long groupId , Long vsId) throws Exception {
        String content = null ;
        if (vsId == null){
            List<ConfGroupActiveDo> confGroupActiveDos = confGroupActiveDao.findAllByGroupIds(new Long[]{groupId},ConfGroupActiveEntity.READSET_FULL);
            if (confGroupActiveDos.size()>0){
                content = confGroupActiveDos.get(0).getContent();
            }
        }else {
            ConfGroupActiveDo confGroupActiveDo = confGroupActiveDao.findByGroupIdAndVirtualServerId(groupId,vsId,ConfGroupActiveEntity.READSET_FULL);
            content = confGroupActiveDo==null?null:confGroupActiveDo.getContent();
        }
        if (content == null){
            return null;
        }
        Group group = DefaultSaxParser.parseEntity(Group.class, content);
        if (group != null){
            autoFiller.autofill(group);
            return group;
        }
        return null;
    }

    @Override
    public List<Group> getActivatedGroups(Long[] groupIds, Long slbId) throws Exception {
        List<Group> groups = new ArrayList<>();
        List<ConfSlbVirtualServerActiveDo> vsDos = confSlbVirtualServerActiveDao.findBySlbId(slbId,ConfSlbVirtualServerActiveEntity.READSET_FULL);
        List<Long> vsid = new ArrayList<>();
        for (ConfSlbVirtualServerActiveDo c : vsDos){
            vsid.add(c.getSlbVirtualServerId());
        }
        List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByGroupIdsAndSlbVirtualServerIds(groupIds, vsid.toArray(new Long[]{}), ConfGroupActiveEntity.READSET_FULL);
        for (ConfGroupActiveDo c : groupActiveDos){
            Group group = DefaultSaxParser.parseEntity(Group.class, c.getContent());
            if (group != null){
                autoFiller.autofill(group);
                groups.add(group);
            }
        }
        return groups;
    }

    @Override
    public Map<Long,List<Group>> getActivatedGroupsByVses(Long[] vsIds) throws Exception {
        Map<Long,List<Group>> result = new HashMap<>();
        if (vsIds == null || vsIds.length==0){
            return result;
        }
        List<ConfGroupActiveDo> list = confGroupActiveDao.findAllByslbVirtualServerIds(vsIds , ConfGroupActiveEntity.READSET_FULL);
        for (ConfGroupActiveDo c : list ){
            Group group = DefaultSaxParser.parseEntity(Group.class, c.getContent());
            if (group != null){
                autoFiller.autofill(group);
                List<Group> groupList = result.get(c.getSlbVirtualServerId());
                if (groupList == null){
                    groupList = new ArrayList<>();
                    result.put(c.getSlbVirtualServerId(),groupList);
                }
                groupList.add(group);
            }
        }
        return result;
    }

    @Override
    public List<VirtualServer> getActivatedVirtualServer(Long vsId) throws Exception {
        List<VirtualServer>res = new ArrayList<>();
        List<ConfSlbVirtualServerActiveDo> confSlbVirtualServerActiveDos = confSlbVirtualServerActiveDao.findBySlbVirtualServerId(vsId,ConfSlbVirtualServerActiveEntity.READSET_FULL);
        for (ConfSlbVirtualServerActiveDo c : confSlbVirtualServerActiveDos){
            res.add(DefaultSaxParser.parseEntity(VirtualServer.class, c.getContent()));
        }
        return res;
    }

    @Override
    public Map<Long,VirtualServer> getActivatedVirtualServerBySlb(Long slbId) throws Exception {
        Map<Long,VirtualServer> result = new HashMap<>();
        List<ConfSlbVirtualServerActiveDo> vses = confSlbVirtualServerActiveDao.findBySlbId(slbId,ConfSlbVirtualServerActiveEntity.READSET_FULL);
        for (ConfSlbVirtualServerActiveDo c : vses ){
            VirtualServer vs = DefaultSaxParser.parseEntity(VirtualServer.class, c.getContent());
            if ( vs != null ){
                result.put(c.getSlbVirtualServerId(),vs);
            }
        }
        return result;
    }

    @Override
    public Slb getActivatedSlb(Long slbId) throws Exception {
        ConfSlbActiveDo slbActiveDo = confSlbActiveDao.findBySlbId(slbId,ConfSlbActiveEntity.READSET_FULL);
        if (slbActiveDo!=null){
            String content = slbActiveDo.getContent();
            Slb slb = DefaultSaxParser.parseEntity(Slb.class,content);
            if (slb == null){
                return null;
            }
            autoFiller.autofill(slb);
            return slb;
        }
        return null;
    }
}
