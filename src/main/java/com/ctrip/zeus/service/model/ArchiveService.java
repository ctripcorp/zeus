package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Archive;
import com.ctrip.zeus.model.entity.Slb;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface ArchiveService {

    int archiveSlb(Slb slb) throws Exception;

    int archiveGroup(Group app) throws Exception;

    int deleteSlbArchive(Long slbId) throws Exception;

    int deleteGroupArchive(Long groupId) throws Exception;

    Slb getSlb(Long slbId, int version) throws Exception;

    Group getGroup(Long groupId, int version) throws Exception;

    Slb getLatestSlb(Long slbId) throws Exception;

    Group getLatestGroup(Long groupId) throws Exception;

    List<Slb> getLatestSlbs(Long[] slbIds) throws Exception;

    List<Group> getLatestGroups(Long[] groupIds) throws Exception;

    List<Slb> getAllSlb(Long slbId) throws Exception;

    List<Group> getAllGroup(Long groupId) throws Exception;

    Archive getLatestSlbArchive(Long slbId) throws Exception;

    Archive getLatestGroupArchive(Long groupId) throws Exception;

    Archive getSlbArchive(Long slbId, int version) throws Exception;

    Archive getGroupArchive(Long groupId, int version) throws Exception;
}
